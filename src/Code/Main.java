package Code;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Scanner;
import java.util.stream.Collectors;

public class Main {
    private static DatabaseManager db;
    private static AuthService authService;
    private static CustomerService customerService;
    private static TransactionService transactionService;
    private static AdminService adminService;
    private static Session session;
    private static final Scanner scanner = new Scanner(System.in);
    private static final String DIVIDER = "------------------------------------------------------------";
    private static final DateTimeFormatter DATE_TIME_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
    private static final Map<String, Double> MYR_EXCHANGE_RATES = new LinkedHashMap<>();

    static {
        MYR_EXCHANGE_RATES.put("MYR", 1.0000);
        MYR_EXCHANGE_RATES.put("USD", 0.2120);
        MYR_EXCHANGE_RATES.put("CAD", 0.2910);
        MYR_EXCHANGE_RATES.put("CNY", 1.5300);
        MYR_EXCHANGE_RATES.put("JPY", 32.1300);
        MYR_EXCHANGE_RATES.put("KRW", 291.4800);
        MYR_EXCHANGE_RATES.put("SGD", 0.2860);
        MYR_EXCHANGE_RATES.put("EUR", 0.1950);
    }

    private static final class NavigateBack extends RuntimeException {
        private static final long serialVersionUID = 1L;
    }

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
        System.out.println("\n\n============================================================");
        System.out.println("                      Fluxx Banking");
        System.out.println("============================================================");
        System.out.println();
        System.out.println("╔════════════════════════════════════════════════════════╗");
        System.out.println("║                    MAIN MENU                           ║");
        System.out.println("╠════════════════════════════════════════════════════════╣");
        System.out.println("║  1. Login                                              ║");
        System.out.println("║  2. Register as new customer                           ║");
        System.out.println("║  3. Exit                                               ║");
        System.out.println("╚════════════════════════════════════════════════════════╝");
        String input = readMainMenuChoice("Choice: ", 1, 3);
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
        try {
            System.out.println("\n┌────────────────────────────────────────────────────────┐");
            System.out.println("│                         LOGIN                          │");
            System.out.println("└────────────────────────────────────────────────────────┘");
            System.out.println("Press 'N' at any time to return to main menu");
            String idOrEmail = readNonEmpty("Enter IC number or email: ");
            String password = readNonEmpty("Password: ");
            User user = idOrEmail.contains("@") ? authService.loginByEmail(idOrEmail, password) : authService.loginByIc(idOrEmail, password);
            if (user != null) {
                session.setCurrentUser(user);
                System.out.println("Welcome, " + user.getName() + (user.isAdmin() ? " (Admin)" : "") + ".");
            } else {
                System.out.println("Invalid IC/email or password.");
            }
        } catch (NavigateBack ignored) {}
    }

    private static void doRegister() {
        try {
            System.out.println("\n\n┌────────────────────────────────────────────────────────┐");
            System.out.println("│                 NEW CUSTOMER REGISTRATION              │");
            System.out.println("└────────────────────────────────────────────────────────┘");
            System.out.println("Press 'N' at any time to return to main menu");
            String name = readNonEmpty("Full name: ");
            String address = readNonEmpty("Address: ");
            LocalDate dob = readDate("Date of birth (YYYY-MM-DD): ");
            String ic = readIcNumber("IC number(XXXXXX-XX-XXXX): ");
            String occupation = readNonEmpty("Occupation: ");
            String email = readEmail("Email: ");
            String phone = readPhoneNumber("Phone: ");
            String password = readNonEmpty("Password: ");
            String accountType = readAccountType();
            double initialDeposit = readNonNegativeDouble("Initial deposit amount: ");
            User created = customerService.register(name, address, dob, ic, occupation, email, phone, password, accountType, initialDeposit);
            if (created != null) {
                System.out.println("Registration successful. Your Customer ID is linked to your account. Please wait for admin approval of your account.");
            } else {
                System.out.println("Registration failed (IC or email may already exist).");
            }
        } catch (NavigateBack ignored) {}
    }

    private static void runCustomerMenu() {
        List<Account> accounts = db.getAccountsByUserId(session.getCurrentUser().getId());
        List<Account> activeAccounts = accounts.stream().filter(a -> Account.STATUS_ACTIVE.equals(a.getStatus())).collect(Collectors.toList());
        if (activeAccounts.isEmpty()) {
            System.out.println("\n╔════════════════════════════════════════════════════════╗");
            System.out.println("║                     CUSTOMER MENU                      ║");
            System.out.println("╠════════════════════════════════════════════════════════╣");
            System.out.println("║ You have no active accounts yet (pending approval).    ║");
            System.out.println("║ 1. Open new account (max 5)                            ║");
            System.out.println("║ 2. View my accounts                                    ║");
            System.out.println("║ 3. Currency exchange calculator                        ║");
            System.out.println("║ 4. Delete an account                                   ║");
            System.out.println("║ 5. Logout                                              ║");
            System.out.println("╚════════════════════════════════════════════════════════╝");
            String choice = readLoggedInRootMenuChoice("Choice: ", 1, 5);
            if (choice == null) return;
            try {
                switch (choice) {
                    case "1" -> doOpenAdditionalAccount();
                    case "2" -> doViewAccounts(accounts);
                    case "3" -> doCurrencyExchangeCalculator();
                    case "4" -> doDeleteAccount(accounts);
                    case "5" -> session.logout();
                    default -> System.out.println("Invalid choice.");
                }
            } catch (NavigateBack ignored) {}
            return;
        }
        printHeader("Customer Menu");
        System.out.println("1. Open new account (max 5)");
        System.out.println("2. Deposit");
        System.out.println("3. Withdraw");
        System.out.println("4. Transfer");
        System.out.println("5. View transaction history");
        System.out.println("6. Generate monthly statement");
        System.out.println("7. View my accounts");
        System.out.println("8. Currency exchange calculator");
        System.out.println("9. Delete an account");
        System.out.println("10. Logout");
        String choice = readLoggedInRootMenuChoice("Choice: ", 1, 10);
        if (choice == null) return;
        try {
            switch (choice) {
                case "1" -> doOpenAdditionalAccount();
                case "2" -> doDeposit(activeAccounts);
                case "3" -> doWithdraw(activeAccounts);
                case "4" -> doTransfer(activeAccounts);
                case "5" -> doViewHistory(activeAccounts);
                case "6" -> doMonthlyStatement(activeAccounts);
                case "7" -> doViewAccounts(accounts);
                case "8" -> doCurrencyExchangeCalculator();
                case "9" -> doDeleteAccount(accounts);
                case "10" -> session.logout();
                default -> System.out.println("Invalid choice.");
            }
        } catch (NavigateBack ignored) {}
    }

    private static void doViewAccounts(List<Account> accounts) {
        if (accounts.isEmpty()) {
            System.out.println("You do not have any accounts yet.");
            return;
        }

        System.out.println("\n┌────────────────────────────────────────────────────────┐");
        System.out.println("│                  ACCOUNT DETAILS                       │");
        System.out.println("└────────────────────────────────────────────────────────┘");
        for (Account account : accounts) {
            System.out.println("Account ID        : " + account.getAccountId());
            System.out.println("Account Number    : " + account.getAccountNumber());
            System.out.println("Type              : " + account.getAccountType().toUpperCase());
            System.out.println("Status            : " + account.getStatus().toUpperCase());
            System.out.println("Current Balance   : " + formatMoney(account.getBalance()));
            System.out.println("Available Balance : " + formatMoney(account.getAvailableBalance()));
            System.out.println("Created At        : " + formatDateTime(account.getCreatedAt()));
            if (Account.TYPE_SAVINGS.equals(account.getAccountType())) {
                System.out.println("Interest Rate     : " + formatPercent(account.getInterestRate()));
            } else if (Account.TYPE_CURRENT.equals(account.getAccountType())) {
                System.out.println("Overdraft Limit   : " + formatMoney(account.getOverdraftLimit()));
                System.out.println("Overdraft Fee     : " + formatMoney(account.getOverdraftFee()));
            }
            System.out.println(DIVIDER);
        }
    }

    private static void doCurrencyExchangeCalculator() {
        System.out.println("\n┌────────────────────────────────────────────────────────┐");
        System.out.println("│              Currency Exchange Calculator              │");
        System.out.println("└────────────────────────────────────────────────────────┘");
        System.out.println("Press 'N' at any time to return to previous menu");
        System.out.println("Supported currencies: " + String.join(", ", MYR_EXCHANGE_RATES.keySet()));
        System.out.println("Base rates (per 1 MYR):");
        for (Map.Entry<String, Double> entry : MYR_EXCHANGE_RATES.entrySet()) {
            System.out.println("  1 MYR = " + String.format("%,.2f", entry.getValue()) + " " + entry.getKey());
        }

        String fromCurrency = readSupportedCurrency("Convert FROM currency code: ");
        String toCurrency = readSupportedCurrency("Convert TO currency code: ");
        double amount = readPositiveDouble("Amount to convert: ");

        double convertedAmount = convertCurrency(amount, fromCurrency, toCurrency);
        double oneUnitRate = convertCurrency(1.0, fromCurrency, toCurrency);

        System.out.println(DIVIDER);
        System.out.println(String.format("Converted amount : %,.2f %s = %,.2f %s", amount, fromCurrency, convertedAmount, toCurrency));
        System.out.println(String.format("Exchange rate    : 1 %s = %,.2f %s", fromCurrency, oneUnitRate, toCurrency));
        System.out.println(DIVIDER);
    }

    private static double convertCurrency(double amount, String fromCurrency, String toCurrency) {
        double fromRatePerMyr = MYR_EXCHANGE_RATES.get(fromCurrency);
        double toRatePerMyr = MYR_EXCHANGE_RATES.get(toCurrency);
        double amountInMyr = amount / fromRatePerMyr;
        return amountInMyr * toRatePerMyr;
    }

    private static String readSupportedCurrency(String prompt) {
        while (true) {
            System.out.print(prompt);
            String currency = readLineOrBack().toUpperCase();
            if (MYR_EXCHANGE_RATES.containsKey(currency)) return currency;
            System.out.println("Unsupported currency. Please enter one of: " + String.join(", ", MYR_EXCHANGE_RATES.keySet()));
        }
    }

    private static void doOpenAdditionalAccount() {
        List<Account> existingAccounts = db.getAccountsByUserId(session.getCurrentUser().getId());
        if (existingAccounts.size() >= CustomerService.MAX_ACCOUNTS_PER_CUSTOMER) {
            System.out.println("You already have the maximum of " + CustomerService.MAX_ACCOUNTS_PER_CUSTOMER + " accounts.");
            return;
        }
        printHeader("Open Additional Account (Press 'N' at any time to return to previous menu)");
        String accountType = readAccountType();
        double initialDeposit = readNonNegativeDouble("Initial deposit amount: ");

        boolean created = customerService.openNewAccountForCustomer(session.getCurrentUser().getId(), accountType, initialDeposit);
        if (created) {
            System.out.println("Account request submitted successfully.");
            System.out.println("Status: PENDING ADMIN APPROVAL");
        } else {
            System.out.println("Could not open a new account (invalid type or max accounts reached).");
        }
    }

    private static Account selectAccount(List<Account> accounts) {
        while (true) {
            System.out.println("\n┌────────────────────────────────────────────────────────┐");
            System.out.println("│                    Select an account                   │");
            System.out.println("└────────────────────────────────────────────────────────┘");
            System.out.println("Press 'N' at any time to return to previous menu");
            for (int i = 0; i < accounts.size(); i++) {
                Account a = accounts.get(i);
                System.out.println((i + 1) + ". " + a.getAccountNumber() + " (" + a.getAccountType().toUpperCase() + ") | Balance: " + formatMoney(a.getBalance()));
            }
            printHeader("Select an account (Press 'N' to return to previous menu)");
            int choice = readIntInRange("Select account (1-" + accounts.size() + "): ", 1, accounts.size());
            return accounts.get(choice - 1);
        }
    }

    private static void doDeposit(List<Account> accounts) {
        Account acc = selectAccount(accounts);
        double amount = readPositiveDouble("Amount to deposit: ");
        System.out.print("Description (optional): ");
        String desc = readLineOrBack();
        if (transactionService.deposit(acc.getAccountId(), amount, desc.isEmpty() ? "Deposit" : desc))
            System.out.println("Deposit successful.");
        else
            System.out.println("Deposit failed.");
    }

    private static void doWithdraw(List<Account> accounts) {
        Account acc = selectAccount(accounts);
        System.out.println("Available: " + formatMoney(acc.getAvailableBalance()));
        double amount = readPositiveDouble("Amount to withdraw: ");
        if (transactionService.withdraw(acc.getAccountId(), amount, "Withdrawal"))
            System.out.println("Withdrawal successful.");
        else
            System.out.println("Withdrawal failed (insufficient funds or invalid amount).");
    }

    private static void doTransfer(List<Account> accounts) {
        Account from = selectAccount(accounts);
        String toNumber = readNonEmpty("Destination account number: ");
        double amount = readPositiveDouble("Amount: ");
        if (transactionService.transfer(from.getAccountId(), toNumber, amount))
            System.out.println("Transfer successful.");
        else
            System.out.println("Transfer failed (check balance, account number, or status).");
    }

    private static void doViewHistory(List<Account> accounts) {
        Account acc = selectAccount(accounts);
        List<Transaction> list = transactionService.getTransactionHistory(acc.getAccountId());
        System.out.println("\n┌────────────────────────────────────────────────────────────────────────────────────────────────────────┐");
        System.out.println("│                                          Transaction History                                           │");
        System.out.println("│                                         Account: " + acc.getAccountNumber() + "                                         │");
        System.out.println("└────────────────────────────────────────────────────────────────────────────────────────────────────────┘");
        if (list.isEmpty()) {
            System.out.println("No transactions found.");
            return;
        }
        for (Transaction t : list) {
            System.out.println(formatDateTime(t.getTimestamp()) + " | " + t.getType() + " | " + formatMoney(t.getAmount()) + " | " + (t.getDescription() != null ? t.getDescription() : "-") + " | Balance after: " + formatMoney(t.getBalanceAfter()));
        }
    }

    private static void doMonthlyStatement(List<Account> accounts) {
        Account acc = selectAccount(accounts);
        int year = readIntInRange("Year (e.g. 2025): ", 1900, 3000);
        int month = readIntInRange("Month (1-12): ", 1, 12);
        System.out.println(customerService.formatMonthlyStatement(acc.getAccountId(), year, month));
    }

    private static void doDeleteAccount(List<Account> accounts) {
        if (accounts.isEmpty()) {
            System.out.println("You have no accounts to delete.");
            return;
        }
        List<Account> zeroBalance = accounts.stream()
                .filter(a -> Math.abs(a.getBalance()) < CustomerService.ZERO_BALANCE_EPSILON)
                .collect(Collectors.toList());
        if (zeroBalance.isEmpty()) {
            System.out.println("You can only delete an account with zero balance. Withdraw or transfer your funds first.");
            return;
        }
        printHeader("Delete account (Press 'N' at any time to return to previous menu)");
        Account acc = selectAccount(zeroBalance);
        System.out.println("You are about to permanently delete account " + acc.getAccountNumber()
                + " (" + acc.getAccountType() + ", status: " + acc.getStatus() + "). This cannot be undone.");
        String password = readNonEmpty("Enter your password to confirm deletion: ");
        if (customerService.deleteOwnAccount(session.getCurrentUser().getId(), acc.getAccountId(), password)) {
            System.out.println("Account deleted.");
        } else {
            System.out.println("Deletion failed. Check your password, or the account may no longer have a zero balance.");
        }
    }

    private static void runAdminMenu() {
        clearScreen();
        System.out.println("\n╔════════════════════════════════════════════════════════╗");
        System.out.println("║                    ADMIN MENU                          ║");
        System.out.println("╠════════════════════════════════════════════════════════╣");
        System.out.println("║  1. List pending account requests                      ║");
        System.out.println("║  2. List all accounts (database)                       ║");
        System.out.println("║  3. Approve account                                    ║");
        System.out.println("║  4. Reject account                                     ║");
        System.out.println("║  5. Set interest rate for an accoun                    ║");
        System.out.println("║  6. Freeze account                                     ║");
        System.out.println("║  7. Unfreeze account                                   ║");
        System.out.println("║  8. Logout                                             ║");
        System.out.println("╚════════════════════════════════════════════════════════╝");
        String choice = readLoggedInRootMenuChoice("Choice: ", 1, 8);
        if (choice == null) return;
        try {
            switch (choice) {
                case "1" -> listPendingAccounts();
                case "2" -> listAllAccountsInDatabase();
                case "3" -> approveAccount();
                case "4" -> rejectAccount();
                case "5" -> setInterestRate();
                case "6" -> freezeAccount();
                case "7" -> unfreezeAccount();
                case "8" -> session.logout();
                default -> System.out.println("Invalid choice.");
            }
        } catch (NavigateBack ignored) {}
    }

    private static void listPendingAccounts() {
        List<Account> pending = adminService.listPendingAccounts();
        if (pending.isEmpty()) {
            System.out.println("No pending accounts.");
            return;
        }
        printHeader("Pending Account Requests");
        for (Account a : pending) {
            User u = a.getUser();
            System.out.println("Account ID: " + a.getAccountId() + " | Number: " + a.getAccountNumber() + " | Type: " + a.getAccountType().toUpperCase() + " | User: " + (u != null ? u.getName() : "?"));
        }
    }

    private static void listAllAccountsInDatabase() {
        List<Account> accounts = adminService.listAllAccounts();
        if (accounts.isEmpty()) {
            System.out.println("No accounts in the database.");
            return;
        }
        System.out.println("\n┌────────────────────────────────────────────────────────┐");
        System.out.println("│                        All Accounts                    │");
        System.out.println("└────────────────────────────────────────────────────────┘");
        for (Account a : accounts) {
            User u = a.getUser();
            System.out.println("Account ID:      " + a.getAccountId());
            System.out.println("  User ID:     " + (u != null ? u.getId() : 0));
            System.out.println("  Customer:    " + (u != null ? u.getName() : "?")
                    + (u != null && u.getEmail() != null && !u.getEmail().isEmpty() ? " (" + u.getEmail() + ")" : ""));
            System.out.println("  Number:      " + a.getAccountNumber());
            System.out.println("  Type:        " + a.getAccountType());
            System.out.println("  Status:      " + a.getStatus());
            System.out.println("  Balance:     " + formatMoney(a.getBalance()));
            System.out.println("  Created:     " + formatDateTime(a.getCreatedAt()));
            if (Account.TYPE_SAVINGS.equals(a.getAccountType())) {
                System.out.println("  Interest:    " + formatPercent(a.getInterestRate()) + " p.a.");
            } else if (Account.TYPE_CURRENT.equals(a.getAccountType())) {
                System.out.println("  Overdraft:   limit " + formatMoney(a.getOverdraftLimit()) + ", fee " + formatMoney(a.getOverdraftFee()));
            }
            System.out.println();
        }
        System.out.println("Total accounts: " + accounts.size());
    }

    private static void approveAccount() {
        System.out.println("\n┌────────────────────────────────────────────────────────┐");
        System.out.println("│                  Approve Account Request               │");
        System.out.println("└────────────────────────────────────────────────────────┘");
        System.out.println("Press 'N' to return to previous menu");
        int id = readPositiveInt("Account ID to approve: ");
        if (adminService.approveAccount(id)) System.out.println("\nAccount approved.");
        else System.out.println("Account not found or not pending.");
    }

    private static void rejectAccount() {
        System.out.println("\n┌────────────────────────────────────────────────────────┐");
        System.out.println("│                  Reject Account Request                │");
        System.out.println("└────────────────────────────────────────────────────────┘");
        System.out.println("Press 'N' to return to previous menu");
        int id = readPositiveInt("Account ID to reject: ");
        if (adminService.rejectAccount(id)) System.out.println("\nAccount rejected.");
        else System.out.println("Account not found or not pending.");
    }

    private static void setInterestRate() {
        System.out.println("\n┌────────────────────────────────────────────────────────┐");
        System.out.println("│                     Set Interest Rate                  │");
        System.out.println("└────────────────────────────────────────────────────────┘");
        System.out.println("Press 'N' to return to previous menu");
        int id = readPositiveInt("Account ID: ");
        double rate = readNonNegativeDouble("Interest rate (e.g. 0.05 for 5%): ");
        if (adminService.setInterestRate(id, rate)) {
            System.out.println("Interest rate updated to " + formatPercent(rate) + ".");
        } else {
            System.out.println("Only savings accounts support interest rate updates.");
        }
    }

    private static void freezeAccount() {
        System.out.println("\n┌────────────────────────────────────────────────────────┐");
        System.out.println("│                       Freeze Account                   │");
        System.out.println("└────────────────────────────────────────────────────────┘");
        System.out.println("Press 'N' to return to previous menu");
        int id = readPositiveInt("Account ID to freeze: ");
        if (adminService.freezeAccount(id)) System.out.println("\nAccount frozen.");
        else System.out.println("Account not found.");
    }

    private static void unfreezeAccount() {
        System.out.println("\n┌────────────────────────────────────────────────────────┐");
        System.out.println("│                     Unfreeze Account                   │");
        System.out.println("└────────────────────────────────────────────────────────┘");
        System.out.println("Press 'N' to return to previous menu");
        int id = readPositiveInt("Account ID to unfreeze: ");
        if (adminService.unfreezeAccount(id)) System.out.println("\nAccount unfrozen.");
        else System.out.println("Account not found or not frozen.");
    }

    private static String readLineOrBack() {
        String line = scanner.nextLine();
        String trimmed = line.trim();
        if ("N".equals(trimmed)) throw new NavigateBack();
        return trimmed;
    }

    private static void printHeader(String title) {
        System.out.println();
        System.out.println(DIVIDER);
        System.out.println(title);
        System.out.println(DIVIDER);
    }

    private static String formatMoney(double amount) {
        return String.format("RM %,.2f", amount);
    }

    private static String formatPercent(double value) {
        return String.format("%.2f%%", value * 100);
    }

    private static String formatDateTime(LocalDateTime dateTime) {
        if (dateTime == null) return "-";
        return dateTime.format(DATE_TIME_FORMATTER);
    }


    private static String readMainMenuChoice(String prompt, int min, int max) {
        while (true) {
            System.out.print(prompt);
            String input = scanner.nextLine().trim();
            if ("N".equals(input)) {
                System.out.println("You are already at the main menu.");
                continue;
            }
            try {
                int value = Integer.parseInt(input);
                if (value >= min && value <= max) return String.valueOf(value);
            } catch (NumberFormatException ignored) {}
            System.out.println("Invalid choice. Please enter a number from " + min + " to " + max + ".");
        }
    }

    private static String readLoggedInRootMenuChoice(String prompt, int min, int max) {
        while (true) {
            System.out.print(prompt);
            String input = scanner.nextLine().trim();
            if ("N".equals(input)) {
                session.logout();
                return null;
            }
            try {
                int value = Integer.parseInt(input);
                if (value >= min && value <= max) return String.valueOf(value);
            } catch (NumberFormatException ignored) {}
            System.out.println("Invalid choice. Please enter a number from " + min + " to " + max + ".");
        }
    }

    private static String readMenuChoice(String prompt, int min, int max) {
        while (true) {
            System.out.print(prompt);
            try {
                String input = readLineOrBack();
                int value = Integer.parseInt(input);
                if (value >= min && value <= max) return String.valueOf(value);
            } catch (NumberFormatException ignored) {}
            System.out.println("Invalid choice. Please enter a number from " + min + " to " + max + ".");
        }
    }

    private static String readNonEmpty(String prompt) {
        while (true) {
            System.out.print(prompt);
            String value = readLineOrBack();
            if (!value.isEmpty()) return value;
            System.out.println("Input cannot be empty.");
        }
    }

    //For birthdate only
    private static LocalDate readDate(String prompt) {
        while (true) {
            System.out.print(prompt);
            String input = readLineOrBack();
            try {
                LocalDate date = LocalDate.parse(input);
                if (date.isAfter(LocalDate.now().minusYears(18))) {
                    System.out.println("You must be at least 18 years old to register.");
                } else {
                    return date;
                }
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
            System.out.println("\n┌────────────────────────────────────────────────────────┐");
            System.out.println("│                   Choose account type                  │");
            System.out.println("├────────────────────────────────────────────────────────┤");
            System.out.println("│  1. Savings                                            │");
            System.out.println("│  2. Current                                            │");
            System.out.println("└────────────────────────────────────────────────────────┘");
            String input = readMenuChoice("Choice: ", 1, 2);
            if ("1".equals(input)) return Account.TYPE_SAVINGS;
            if ("2".equals(input)) return Account.TYPE_CURRENT;
        }
    }

    private static int readIntInRange(String prompt, int min, int max) {
        while (true) {
            System.out.print(prompt);
            String input = readLineOrBack();
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
            String input = readLineOrBack();
            try {
                int value = Integer.parseInt(input);
                if (value > 0) return value;
            } catch (NumberFormatException ignored) {}
            System.out.println("Please enter a positive whole number.");
        }
    }

    //0 is not allowed for this function
    private static double readPositiveDouble(String prompt) {
        while (true) {
            System.out.print(prompt);
            String input = readLineOrBack();
            try {
                double value = Double.parseDouble(input);
                if (value > 0) return value;
            } catch (NumberFormatException ignored) {}
            System.out.println("Please enter a number greater than 0.");
        }
    }

    //0 is allowed for this function, used for the interest rates where 0% interest rate exists
    private static double readNonNegativeDouble(String prompt) {
        while (true) {
            System.out.print(prompt);
            String input = readLineOrBack();
            try {
                double value = Double.parseDouble(input);
                if (value >= 0) return value;
            } catch (NumberFormatException ignored) {}
            System.out.println("Please enter a number that is 0 or greater.");
        }
    }

    private static String readPhoneNumber(String prompt){
        while (true) {
            String regex = "^(\\+?60|0)(1[0-46-9]\\d{7,8}|[3-9]\\d{7,8})$";
            String phone = readNonEmpty(prompt);
            if (phone.matches(regex)) return phone;
            System.out.println("Invalid Phone Number.");
        }
    }

    private static String readIcNumber(String prompt){
            while (true) {
            String icRegex = "^(\\d{2})(0[1-9]|1[0-2])(0[1-9]|[12]\\d|3[01])(\\d{2})(\\d{4})$";
            String icNumber = readNonEmpty(prompt);
            if (icNumber.matches(icRegex)) return icNumber;
            System.out.println("Invalid IC Number.");
        }
    }

    public static void clearScreen() {  
        System.out.print("\033[H\033[2J");
        System.out.flush();
    }  
}
