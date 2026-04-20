package Code;

import java.util.List;

public class TransactionService {
    private final DatabaseManager db;
    public static final double DEFAULT_DEPOSIT_MINIMUM = 0.0;

    public TransactionService(DatabaseManager db) {
        this.db = db;
    }

    /**
     * Deposit to account. Returns true on success. Fails if account not active.
     */
    public boolean deposit(int accountId, double amount, String description) {
        if (amount <= 0 || amount < DEFAULT_DEPOSIT_MINIMUM) return false;
        Account acc = db.getAccountById(accountId);
        if (acc == null || !Account.STATUS_ACTIVE.equals(acc.getStatus())) return false;
        double newBalance = acc.getBalance() + amount;
        String ref = "REF" + System.currentTimeMillis();
        Transaction t = new DepositTransaction(acc, amount, description, ref, newBalance, DEFAULT_DEPOSIT_MINIMUM);
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
        double withdrawalFee = 0.0;
        if (acc instanceof CurrentAccount currentAccount && (acc.getBalance() - amount) < 0) {
            withdrawalFee = currentAccount.getOverdraftFee();
        }
        double totalDebit = amount + withdrawalFee;
        if (acc.getAvailableBalance() < totalDebit) return false;
        double newBalance = acc.getBalance() - totalDebit;
        String ref = "REF" + System.currentTimeMillis();
        Transaction t = new WithdrawTransaction(acc, amount, description, ref, newBalance, withdrawalFee);
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
        return db.transfer(fromAccountId, to.getAccountId(), amount, "REF" + System.currentTimeMillis());
    }

    public List<Transaction> getTransactionHistory(int accountId) {
        return db.getTransactionsByAccountId(accountId);
    }
}
