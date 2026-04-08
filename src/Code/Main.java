package Code;

import java.time.LocalDate;
import java.util.List;
import java.util.Scanner;
import java.util.stream.Collectors;

public class Main {
    private static DatabaseManager db;
    private static AuthService authService;
    private static CustomerService customerService;
    private static AdminService adminService;
    private static Session session;
    private static final Scanner scanner = new Scanner(System.in);

    public static void main(String[] args) {
        db = new DatabaseManager();
        db.createTables();
        authService = new AuthService(db);
        customerService = new CustomerService(db);
        adminService = new AdminService(db);
        session = new Session();

        while (true) {
            if (!session.isLoggedIn()) {
                showMainMenu();
            } else {
                if (session.isAdmin()) {
                    runAdminMenu();
                } else {
                    runCustomerMenu();
                }
            }
        }
    }

    private static void showMainMenu() {
        System.out.println("\n--- Digital Banking Platform ---");
        System.out.println("1. Login");
        System.out.println("2. Register as new customer");
        System.out.println("3. Exit");
        String input = readMenuChoice("Choice: ", 1, 3);
        switch (input) {
            case "1" -> doLogin();
            case "2" -> doRegister();
            case "3" -> {
                System.out.println("Goodbye.");
                System.exit(0);
            }
            default -> System.out.println("Invalid choice.");
        }
    }

    private static void doLogin() {
        System.out.println("\n--- Login ---");
        String idOrEmail = readNonEmpty("Enter IC number or email: ");
        String password = readNonEmpty("Password: ");
        User user = idOrEmail.contains("@") ? authService.loginByEmail(idOrEmail, password) : authService.loginByIc(idOrEmail, password);
        if (user != null) {
            session.setCurrentUser(user);
            System.out.println("Welcome, " + user.getName() + (user.isAdmin() ? " (Admin)" : "") + ".");
        } else {
            System.out.println("Invalid IC/email or password.");
        }
    }

    private static void doRegister() {
        System.out.println("\n--- New Customer Registration ---");
        String name = readNonEmpty("Full name: ");
        String address = readNonEmpty("Address: ");
        LocalDate dob = readDate("Date of birth (YYYY-MM-DD): ");
        String ic = readNonEmpty("IC number: ");
        String occupation = readNonEmpty("Occupation: ");
        String email = readEmail("Email: ");
        String phone = readNonEmpty("Phone: ");
        String password = readNonEmpty("Password: ");
        String accountType = readAccountType();
        double initialDeposit = readNonNegativeDouble("Initial deposit amount: ");
        User created = customerService.register(name, address, dob, ic, occupation, email, phone, password, accountType, initialDeposit);
        if (created != null) {
            System.out.println("Registration successful. Your Customer ID is linked to your account. Please wait for admin approval of your account.");
        } else {
            System.out.println("Registration failed (IC or email may already exist).");
        }
    }

    private static void runCustomerMenu() {
        List<Account> accounts = db.getAccountsByUserId(session.getCurrentUser().getId());
        List<Account> activeAccounts = accounts.stream().filter(a -> Account.STATUS_ACTIVE.equals(a.getStatus())).collect(Collectors.toList());
        if (activeAccounts.isEmpty()) {
            System.out.println("\nYou have no active accounts yet (pending approval).");
            System.out.println("1. Logout");
            readMenuChoice("Choice: ", 1, 1);
            session.logout();
            return;
        }
        System.out.println("\n--- Customer Menu ---");
        System.out.println("1. Open new account (max 5)");
        System.out.println("2. Deposit");
        System.out.println("3. Withdraw");
        System.out.println("4. Transfer");
        System.out.println("5. View transaction history");
        System.out.println("6. Generate monthly statement");
        System.out.println("7. Logout");
        String choice = readMenuChoice("Choice: ", 1, 7);
        switch (choice) {
            case "1" -> doOpenAdditionalAccount();
            case "2" -> doDeposit(activeAccounts);
            case "3" -> doWithdraw(activeAccounts);
            case "4" -> doTransfer(activeAccounts);
            case "5" -> doViewHistory(activeAccounts);
            case "6" -> doMonthlyStatement(activeAccounts);
            case "7" -> session.logout();
            default -> System.out.println("Invalid choice.");
        }
    }

    private static void doOpenAdditionalAccount() {
        List<Account> existingAccounts = db.getAccountsByUserId(session.getCurrentUser().getId());
        if (existingAccounts.size() >= CustomerService.MAX_ACCOUNTS_PER_CUSTOMER) {
            System.out.println("You already have the maximum of " + CustomerService.MAX_ACCOUNTS_PER_CUSTOMER + " accounts.");
            return;
        }

        String accountType = readAccountType();
        double initialDeposit = readNonNegativeDouble("Initial deposit amount: ");

        boolean created = customerService.openNewAccountForCustomer(session.getCurrentUser().getId(), accountType, initialDeposit);
        if (created) {
            System.out.println("Account request submitted successfully. Pending admin approval.");
        } else {
            System.out.println("Could not open a new account (invalid type or max accounts reached).");
        }
    }

    private static Account selectAccount(List<Account> accounts) {
        while (true) {
            for (int i = 0; i < accounts.size(); i++) {
                Account a = accounts.get(i);
                System.out.println((i + 1) + ". " + a.getAccountNumber() + " (" + a.getAccountType() + ") Balance: " + a.getBalance());
            }
            int choice = readIntInRange("Select account (1-" + accounts.size() + "): ", 1, accounts.size());
            return accounts.get(choice - 1);
        }
    }

    private static void doDeposit(List<Account> accounts) {
        Account acc = selectAccount(accounts);
        double amount = readPositiveDouble("Amount to deposit: ");
        System.out.print("Description (optional): ");
        String desc = scanner.nextLine().trim();
        if (customerService.deposit(acc.getId(), amount, desc.isEmpty() ? "Deposit" : desc))
            System.out.println("Deposit successful.");
        else
            System.out.println("Deposit failed.");
    }

    private static void doWithdraw(List<Account> accounts) {
        Account acc = selectAccount(accounts);
        System.out.println("Available: " + acc.getAvailableBalance());
        double amount = readPositiveDouble("Amount to withdraw: ");
        if (customerService.withdraw(acc.getId(), amount, "Withdrawal"))
            System.out.println("Withdrawal successful.");
        else
            System.out.println("Withdrawal failed (insufficient funds or invalid amount).");
    }

    private static void doTransfer(List<Account> accounts) {
        Account from = selectAccount(accounts);
        String toNumber = readNonEmpty("Destination account number: ");
        double amount = readPositiveDouble("Amount: ");
        if (customerService.transfer(from.getId(), toNumber, amount))
            System.out.println("Transfer successful.");
        else
            System.out.println("Transfer failed (check balance, account number, or status).");
    }

    private static void doViewHistory(List<Account> accounts) {
        Account acc = selectAccount(accounts);
        List<Transaction> list = customerService.getTransactionHistory(acc.getId());
        System.out.println("--- Transaction History: " + acc.getAccountNumber() + " ---");
        for (Transaction t : list) {
            System.out.println(t.getTimestamp() + " | " + t.getType() + " | " + t.getAmount() + " | " + (t.getDescription() != null ? t.getDescription() : "") + " | Balance after: " + t.getBalanceAfter());
        }
    }

    private static void doMonthlyStatement(List<Account> accounts) {
        Account acc = selectAccount(accounts);
        int year = readIntInRange("Year (e.g. 2025): ", 1900, 3000);
        int month = readIntInRange("Month (1-12): ", 1, 12);
        System.out.println(customerService.formatMonthlyStatement(acc.getId(), year, month));
    }

    private static void runAdminMenu() {
        System.out.println("\n--- Admin Menu ---");
        System.out.println("1. List pending account requests");
        System.out.println("2. Approve account");
        System.out.println("3. Reject account");
        System.out.println("4. Set interest rate for an account");
        System.out.println("5. Freeze account");
        System.out.println("6. Unfreeze account");
        System.out.println("7. Logout");
        String choice = readMenuChoice("Choice: ", 1, 7);
        switch (choice) {
            case "1" -> listPendingAccounts();
            case "2" -> approveAccount();
            case "3" -> rejectAccount();
            case "4" -> setInterestRate();
            case "5" -> freezeAccount();
            case "6" -> unfreezeAccount();
            case "7" -> session.logout();
            default -> System.out.println("Invalid choice.");
        }
    }

    private static void listPendingAccounts() {
        List<Account> pending = adminService.listPendingAccounts();
        if (pending.isEmpty()) {
            System.out.println("No pending accounts.");
            return;
        }
        for (Account a : pending) {
            User u = db.getUserById(a.getUserId());
            System.out.println("Account ID: " + a.getId() + " | Number: " + a.getAccountNumber() + " | Type: " + a.getAccountType() + " | User: " + (u != null ? u.getName() : "?"));
        }
    }

    private static void approveAccount() {
        int id = readPositiveInt("Account ID to approve: ");
        if (adminService.approveAccount(id)) System.out.println("Account approved.");
        else System.out.println("Account not found or not pending.");
    }

    private static void rejectAccount() {
        int id = readPositiveInt("Account ID to reject: ");
        if (adminService.rejectAccount(id)) System.out.println("Account rejected.");
        else System.out.println("Account not found or not pending.");
    }

    private static void setInterestRate() {
        int id = readPositiveInt("Account ID: ");
        double rate = readNonNegativeDouble("Interest rate (e.g. 0.05 for 5%): ");
        if (adminService.setInterestRate(id, rate)) {
            System.out.println("Interest rate updated.");
        } else {
            System.out.println("Only savings accounts support interest rate updates.");
        }
    }

    private static void freezeAccount() {
        int id = readPositiveInt("Account ID to freeze: ");
        if (adminService.freezeAccount(id)) System.out.println("Account frozen.");
        else System.out.println("Account not found.");
    }

    private static void unfreezeAccount() {
        int id = readPositiveInt("Account ID to unfreeze: ");
        if (adminService.unfreezeAccount(id)) System.out.println("Account unfrozen.");
        else System.out.println("Account not found or not frozen.");
    }

    private static String readMenuChoice(String prompt, int min, int max) {
        while (true) {
            System.out.print(prompt);
            String input = scanner.nextLine().trim();
            try {
                int value = Integer.parseInt(input);
                if (value >= min && value <= max) return String.valueOf(value);
            } catch (NumberFormatException ignored) {}
            System.out.println("Invalid choice. Please enter a number from " + min + " to " + max + ".");
        }
    }

    private static String readNonEmpty(String prompt) {
        while (true) {
            System.out.print(prompt);
            String value = scanner.nextLine().trim();
            if (!value.isEmpty()) return value;
            System.out.println("Input cannot be empty.");
        }
    }

    private static LocalDate readDate(String prompt) {
        while (true) {
            System.out.print(prompt);
            String input = scanner.nextLine().trim();
            try {
                return LocalDate.parse(input);
            } catch (Exception ignored) {
                System.out.println("Invalid date format. Use YYYY-MM-DD.");
            }
        }
    }

    private static String readEmail(String prompt) {
        while (true) {
            String email = readNonEmpty(prompt);
            if (email.contains("@") && email.contains(".")) return email;
            System.out.println("Invalid email format.");
        }
    }

    private static String readAccountType() {
        while (true) {
            System.out.println("Choose account type:");
            System.out.println("1. Savings");
            System.out.println("2. Current");
            String input = readMenuChoice("Choice: ", 1, 2);
            if ("1".equals(input)) return Account.TYPE_SAVINGS;
            if ("2".equals(input)) return Account.TYPE_CURRENT;
        }
    }

    private static int readIntInRange(String prompt, int min, int max) {
        while (true) {
            System.out.print(prompt);
            String input = scanner.nextLine().trim();
            try {
                int value = Integer.parseInt(input);
                if (value >= min && value <= max) return value;
            } catch (NumberFormatException ignored) {}
            System.out.println("Please enter a number between " + min + " and " + max + ".");
        }
    }

    private static int readPositiveInt(String prompt) {
        while (true) {
            System.out.print(prompt);
            String input = scanner.nextLine().trim();
            try {
                int value = Integer.parseInt(input);
                if (value > 0) return value;
            } catch (NumberFormatException ignored) {}
            System.out.println("Please enter a positive whole number.");
        }
    }

    private static double readPositiveDouble(String prompt) {
        while (true) {
            System.out.print(prompt);
            String input = scanner.nextLine().trim();
            try {
                double value = Double.parseDouble(input);
                if (value > 0) return value;
            } catch (NumberFormatException ignored) {}
            System.out.println("Please enter a number greater than 0.");
        }
    }

    private static double readNonNegativeDouble(String prompt) {
        while (true) {
            System.out.print(prompt);
            String input = scanner.nextLine().trim();
            try {
                double value = Double.parseDouble(input);
                if (value >= 0) return value;
            } catch (NumberFormatException ignored) {}
            System.out.println("Please enter a number that is 0 or greater.");
        }
    }
}
