package Code;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.UUID;

/**
 * Customer-facing operations: register, deposit, withdraw, transfer, view history, monthly statement, delete account.
 */
public class CustomerService {
    public static final int MAX_ACCOUNTS_PER_CUSTOMER = 5;
    public static final double ZERO_BALANCE_EPSILON = 1e-6;
    private final DatabaseManager db;

    public CustomerService(DatabaseManager db) {
        this.db = db;
    }

    /**
     * Generate a unique account number (e.g. ACC + 10 digits). Checks DB for uniqueness.
     */
    public String generateUniqueAccountNumber() {
        String candidate;
        int attempts = 0;
        do {
            long n = Math.abs(UUID.randomUUID().getMostSignificantBits() % 10_000_000_000L);
            candidate = "ACC" + String.format("%010d", n);
            attempts++;
            if (attempts > 100) throw new IllegalStateException("Could not generate unique account number");
        } while (db.getAccountByNumber(candidate) != null);
        return candidate;
    }

    /**
     * Register a new customer: create User and one Account (status pending). Returns the new User or null on failure.
     */
    public User register(String name, String address, LocalDate dateOfBirth, String icNumber,
                         String occupation, String email, String phone, String password,
                         String accountType, double initialDeposit) {
        if (db.getUserByIc(icNumber) != null) return null;
        if (db.getUserByEmail(email) != null) return null;
        User user = new User(name, address, dateOfBirth, icNumber, occupation, email, phone, password, User.ROLE_CUSTOMER);
        db.insertUser(user);
        User created = db.getUserByIc(icNumber);
        if (created == null) return null;
        if (!openNewAccountForCustomer(created.getId(), accountType, initialDeposit)) return null;
        return created;
    }

    /**
     * Open an additional account for an existing customer.
     * Returns false if account type is invalid or the customer has reached the max account limit.
     */
    public boolean openNewAccountForCustomer(int userId, String accountType, double initialDeposit) {
        List<Account> existingAccounts = db.getAccountsByUserId(userId);
        if (existingAccounts.size() >= MAX_ACCOUNTS_PER_CUSTOMER) return false;
        if (!Account.TYPE_SAVINGS.equals(accountType) && !Account.TYPE_CURRENT.equals(accountType)) return false;

        String accountNumber = generateUniqueAccountNumber();
        Account account;
        if (Account.TYPE_CURRENT.equals(accountType)) {
            account = new Current(userId, accountNumber, initialDeposit > 0 ? initialDeposit : 0.0, 500.0, 10.0, Account.STATUS_PENDING);
        } else {
            account = new Savings(userId, accountNumber, initialDeposit > 0 ? initialDeposit : 0.0, 0.05, Account.STATUS_PENDING);
        }
        db.insertAccount(account);

        if (initialDeposit > 0) {
            Account createdAccount = db.getAccountByNumber(accountNumber);
            if (createdAccount != null) {
                String ref = "REF" + System.currentTimeMillis();
                Transaction t = new DepositTransaction(createdAccount.getId(), initialDeposit, "Initial deposit", ref, initialDeposit);
                db.insertTransaction(t);
            }
        }
        return true;
    }

    /**
     * Deposit to account. Returns true on success. Fails if account not active.
     */
    public boolean deposit(int accountId, double amount, String description) {
        if (amount <= 0) return false;
        Account acc = db.getAccountById(accountId);
        if (acc == null || !Account.STATUS_ACTIVE.equals(acc.getStatus())) return false;
        double newBalance = acc.getBalance() + amount;
        String ref = "REF" + System.currentTimeMillis();
        Transaction t = new DepositTransaction(accountId, amount, description, ref, newBalance);
        db.insertTransaction(t);
        db.updateAccountBalance(accountId, newBalance);
        return true;
    }

    /**
     * Withdraw from account. Enforces balance + overdraft (for current). Returns true on success.
     */
    public boolean withdraw(int accountId, double amount, String description) {
        if (amount <= 0) return false;
        Account acc = db.getAccountById(accountId);
        if (acc == null || !Account.STATUS_ACTIVE.equals(acc.getStatus())) return false;
        if (acc.getAvailableBalance() < amount) return false;
        double newBalance = acc.getBalance() - amount;
        String ref = "REF" + System.currentTimeMillis();
        Transaction t = new WithdrawTransaction(accountId, amount, description, ref, newBalance);
        db.insertTransaction(t);
        db.updateAccountBalance(accountId, newBalance);
        return true;
    }

    /**
     * Transfer amount from one account to another (by account number). Uses DB transaction for integrity.
     */
    public boolean transfer(int fromAccountId, String toAccountNumber, double amount) {
        if (amount <= 0) return false;
        Account to = db.getAccountByNumber(toAccountNumber);
        if (to == null) return false;
        return db.transfer(fromAccountId, to.getId(), amount, "REF" + System.currentTimeMillis());
    }

    public List<Transaction> getTransactionHistory(int accountId) {
        return db.getTransactionsByAccountId(accountId);
    }

    /**
     * Permanently deletes the customer's account after password confirmation.
     * The account balance must be zero (no funds). Returns false if validation fails or the delete does not complete.
     */
    public boolean deleteOwnAccount(int userId, int accountId, String password) {
        if (password == null || password.isBlank()) return false;
        User user = db.getUserById(userId);
        if (user == null || !db.validatePassword(password, user.getPasswordHash())) return false;
        Account acc = db.getAccountById(accountId);
        if (acc == null || acc.getUserId() != userId) return false;
        if (Math.abs(acc.getBalance()) >= ZERO_BALANCE_EPSILON) return false;
        return db.deleteAccountWithTransactions(accountId);
    }

    public List<Transaction> getMonthlyStatement(int accountId, int year, int month) {
        return db.getTransactionsByAccountIdAndMonth(accountId, year, month);
    }

    /**
     * Format monthly statement as text for display.
     */
    public String formatMonthlyStatement(int accountId, int year, int month) {
        Account acc = db.getAccountById(accountId);
        if (acc == null) return "Account not found.";
        List<Transaction> list = getMonthlyStatement(accountId, year, month);
        DateTimeFormatter fmt = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");
        StringBuilder sb = new StringBuilder();
        sb.append("\n──────────────────────────────────────────────────────────────────────────────────────────────────────\n");
        sb.append("Monthly Statement - ").append(acc.getAccountNumber()).append(" - ").append(year).append("-").append(String.format("%02d", month)).append("\n");
        sb.append("──────────────────────────────────────────────────────────────────────────────────────────────────────\n");
        sb.append("Account Type : ").append(acc.getAccountType().toUpperCase()).append("\n");
        sb.append("Balance      : ").append(String.format("RM %,.2f", acc.getBalance())).append("\n");
        sb.append("──────────────────────────────────────────────────────────────────────────────────────────────────────\n");
        if (list.isEmpty()) {
            sb.append("No transactions for this month.\n");
        }
        for (Transaction t : list) {
            sb.append(t.getTimestamp().format(fmt)).append(" | ").append(t.getType()).append(" | ");
            sb.append(String.format("RM %,.2f", t.getAmount())).append(" | ");
            sb.append(t.getDescription() != null ? t.getDescription() : "-").append(" | Balance after: ");
            sb.append(String.format("RM %,.2f", t.getBalanceAfter())).append("\n");
        }
        sb.append("──────────────────────────────────────────────────────────────────────────────────────────────────────\n");
        return sb.toString();
    }
}
