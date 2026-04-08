package Code;
import java.sql.*;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class DatabaseManager {
    private static final String DB_URL = "jdbc:sqlite:bank.db";

    private Connection getConnection() throws SQLException {
        return DriverManager.getConnection(DB_URL);
    }

    public void createTables() {
        String createUsers = """
            CREATE TABLE IF NOT EXISTS Users (
                id INTEGER PRIMARY KEY AUTOINCREMENT,
                name TEXT NOT NULL,
                address TEXT,
                date_of_birth DATE,
                ic_number TEXT UNIQUE NOT NULL,
                occupation TEXT,
                email TEXT UNIQUE,
                phone TEXT,
                password_hash TEXT,
                role TEXT DEFAULT 'customer',
                status TEXT DEFAULT 'active',
                created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
            );
        """;

        String createAccounts = """
            CREATE TABLE IF NOT EXISTS Accounts (
                id INTEGER PRIMARY KEY AUTOINCREMENT,
                user_id INTEGER NOT NULL,
                account_number TEXT UNIQUE NOT NULL,
                account_type TEXT NOT NULL,
                balance REAL DEFAULT 0.0,
                interest_rate REAL DEFAULT 0.0,
                overdraft_limit REAL DEFAULT 0.0,
                overdraft_fee REAL DEFAULT 0.0,
                status TEXT DEFAULT 'pending',
                created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                FOREIGN KEY (user_id) REFERENCES Users(id)
            );
        """;

        String createTransactions = """
            CREATE TABLE IF NOT EXISTS Transactions (
                id INTEGER PRIMARY KEY AUTOINCREMENT,
                account_id INTEGER NOT NULL,
                type TEXT NOT NULL,
                amount REAL NOT NULL,
                description TEXT,
                reference_id TEXT,
                timestamp TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                balance_after REAL,
                FOREIGN KEY (account_id) REFERENCES Accounts(id)
            );
        """;

        try (Connection conn = getConnection(); Statement stmt = conn.createStatement()) {
            stmt.execute(createUsers);
            stmt.execute(createAccounts);
            stmt.execute(createTransactions);
            migrateSchema(conn);
            ensureAdminExists(conn);
            System.out.println("Tables created successfully.");
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private void migrateSchema(Connection conn) {
        String[] alterStatements = {
            "ALTER TABLE Users ADD COLUMN role TEXT DEFAULT 'customer'",
            "ALTER TABLE Accounts ADD COLUMN overdraft_limit REAL DEFAULT 0.0",
            "ALTER TABLE Accounts ADD COLUMN overdraft_fee REAL DEFAULT 0.0",
            "ALTER TABLE Transactions ADD COLUMN reference_id TEXT"
        };
        for (String sql : alterStatements) {
            try (Statement stmt = conn.createStatement()) {
                stmt.execute(sql);
            } catch (SQLException e) {
                if (e.getMessage() == null || !e.getMessage().toLowerCase().contains("duplicate column"))
                    e.printStackTrace();
            }
        }
    }

    private void ensureAdminExists(Connection conn) {
        User existing = getUserByIc("ADMIN001");
        if (existing != null) {
            if (!User.ROLE_ADMIN.equals(existing.getRole())) {
                updateUserRole(existing.getId(), User.ROLE_ADMIN);
            }
            return;
        }
        User admin = new User("Admin", "Bank", LocalDate.of(2000, 1, 1), "ADMIN001", "Administrator", "admin@bank.com", "0000000000", "admin123", User.ROLE_ADMIN);
        insertUser(admin);
    }

    public void updateUserRole(int userId, String role) {
        String sql = "UPDATE Users SET role = ? WHERE id = ?";
        try (Connection conn = getConnection(); PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, role);
            pstmt.setInt(2, userId);
            pstmt.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    // Hash password (simple SHA-256 example)
    public String hashPassword(String password) {
        try {
            MessageDigest md = MessageDigest.getInstance("SHA-256");
            byte[] hash = md.digest(password.getBytes());
            StringBuilder sb = new StringBuilder();
            for (byte b : hash) sb.append(String.format("%02x", b));
            return sb.toString();
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException(e);
        }
    }

    public void insertUser(User user) {
        String sql = "INSERT INTO Users (name, address, date_of_birth, ic_number, occupation, email, phone, password_hash, role) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)";
        try (Connection conn = getConnection(); PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, user.getName());
            pstmt.setString(2, user.getAddress());
            pstmt.setDate(3, Date.valueOf(user.getDateOfBirth()));
            pstmt.setString(4, user.getIcNumber());
            pstmt.setString(5, user.getOccupation());
            pstmt.setString(6, user.getEmail());
            pstmt.setString(7, user.getPhone());
            pstmt.setString(8, hashPassword(user.getPasswordHash()));
            pstmt.setString(9, user.getRole() != null ? user.getRole() : User.ROLE_CUSTOMER);
            pstmt.executeUpdate();
            System.out.println("User inserted.");
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    // Get user by IC number
    public User getUserByIc(String icNumber) {
        String sql = "SELECT * FROM Users WHERE ic_number = ?";
        try (Connection conn = getConnection(); PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, icNumber);
            ResultSet rs = pstmt.executeQuery();
            if (rs.next()) {
                User user = new User();
                user.setId(rs.getInt("id"));
                user.setName(rs.getString("name"));
                user.setAddress(rs.getString("address"));
                user.setDateOfBirth(rs.getDate("date_of_birth").toLocalDate());
                user.setOccupation(rs.getString("occupation"));
                user.setEmail(rs.getString("email"));
                user.setIcNumber(rs.getString("ic_number"));
                user.setPhone(rs.getString("phone"));
                user.setPasswordHash(rs.getString("password_hash"));
                try { user.setRole(rs.getString("role")); } catch (SQLException ignored) {}
                if (user.getRole() == null || user.getRole().isEmpty()) user.setRole(User.ROLE_CUSTOMER);
                return user;
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    public void DeleteUserByIc(String icNumber) {
        String sql = "DELETE FROM Users WHERE ic_number = ?";
        try (Connection conn = getConnection(); PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, icNumber);
            int affectedRows = pstmt.executeUpdate();
            if (affectedRows > 0) {
                System.out.println("User deleted.");
            } else {
                System.out.println("No user found with the given IC number.");
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public User getUserByEmail(String email) {
        String sql = "SELECT * FROM Users WHERE email = ?";
        try (Connection conn = getConnection(); PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, email);
            ResultSet rs = pstmt.executeQuery();
            if (rs.next()) {
                User user = new User();
                user.setId(rs.getInt("id"));
                user.setName(rs.getString("name"));
                user.setAddress(rs.getString("address"));
                user.setDateOfBirth(rs.getDate("date_of_birth").toLocalDate());
                user.setOccupation(rs.getString("occupation"));
                user.setEmail(rs.getString("email"));
                user.setIcNumber(rs.getString("ic_number"));
                user.setPhone(rs.getString("phone"));
                user.setPasswordHash(rs.getString("password_hash"));
                try { user.setRole(rs.getString("role")); } catch (SQLException ignored) {}
                if (user.getRole() == null || user.getRole().isEmpty()) user.setRole(User.ROLE_CUSTOMER);
                return user;
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    public User getUserById(int id) {
        String sql = "SELECT * FROM Users WHERE id = ?";
        try (Connection conn = getConnection(); PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, id);
            ResultSet rs = pstmt.executeQuery();
            if (rs.next()) {
                User user = new User();
                user.setId(rs.getInt("id"));
                user.setName(rs.getString("name"));
                user.setAddress(rs.getString("address"));
                user.setDateOfBirth(rs.getDate("date_of_birth").toLocalDate());
                user.setOccupation(rs.getString("occupation"));
                user.setEmail(rs.getString("email"));
                user.setIcNumber(rs.getString("ic_number"));
                user.setPhone(rs.getString("phone"));
                user.setPasswordHash(rs.getString("password_hash"));
                try { user.setRole(rs.getString("role")); } catch (SQLException ignored) {}
                if (user.getRole() == null || user.getRole().isEmpty()) user.setRole(User.ROLE_CUSTOMER);
                return user;
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    public boolean validatePassword(String plainPassword, String hashedStored) {
        return hashedStored != null && hashedStored.equals(hashPassword(plainPassword));
    }

    public void insertAccount(Account account) {
        String sql = "INSERT INTO Accounts (user_id, account_number, account_type, balance, interest_rate, overdraft_limit, overdraft_fee, status) VALUES (?, ?, ?, ?, ?, ?, ?, ?)";
        try (Connection conn = getConnection(); PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, account.getUserId());
            pstmt.setString(2, account.getAccountNumber());
            pstmt.setString(3, account.getAccountType());
            pstmt.setDouble(4, account.getBalance());
            pstmt.setDouble(5, account.getInterestRate());
            pstmt.setDouble(6, account.getOverdraftLimit());
            pstmt.setDouble(7, account.getOverdraftFee());
            pstmt.setString(8, account.getStatus() != null ? account.getStatus() : Account.STATUS_PENDING);
            pstmt.executeUpdate();
            System.out.println("Account inserted.");
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private Account mapAccount(ResultSet rs) throws SQLException {
        String accountType = rs.getString("account_type");
        Account account;
        if (Account.TYPE_CURRENT.equals(accountType)) {
            Current current = new Current();
            current.setOverdraftLimit(rs.getDouble("overdraft_limit"));
            current.setOverdraftFee(rs.getDouble("overdraft_fee"));
            account = current;
        } else {
            Savings savings = new Savings();
            savings.setInterestRate(rs.getDouble("interest_rate"));
            account = savings;
        }
        account.setId(rs.getInt("id"));
        account.setUserId(rs.getInt("user_id"));
        account.setAccountNumber(rs.getString("account_number"));
        account.setAccountType(accountType);
        account.setBalance(rs.getDouble("balance"));
        try {
            account.setStatus(rs.getString("status"));
            Timestamp ts = rs.getTimestamp("created_at");
            if (ts != null) account.setCreatedAt(ts.toLocalDateTime());
        } catch (SQLException ignored) {}
        if (account.getStatus() == null) account.setStatus(Account.STATUS_ACTIVE);
        return account;
    }

    public List<Account> getAccountsByUserId(int userId) {
        List<Account> accounts = new ArrayList<>();
        String sql = "SELECT * FROM Accounts WHERE user_id = ?";
        try (Connection conn = getConnection(); PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, userId);
            ResultSet rs = pstmt.executeQuery();
            while (rs.next()) accounts.add(mapAccount(rs));
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return accounts;
    }

    public Account getAccountById(int accountId) {
        String sql = "SELECT * FROM Accounts WHERE id = ?";
        try (Connection conn = getConnection(); PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, accountId);
            ResultSet rs = pstmt.executeQuery();
            if (rs.next()) return mapAccount(rs);
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    public Account getAccountByNumber(String accountNumber) {
        String sql = "SELECT * FROM Accounts WHERE account_number = ?";
        try (Connection conn = getConnection(); PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, accountNumber);
            ResultSet rs = pstmt.executeQuery();
            if (rs.next()) return mapAccount(rs);
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    public List<Account> getAccountsByStatus(String status) {
        List<Account> accounts = new ArrayList<>();
        String sql = "SELECT * FROM Accounts WHERE status = ?";
        try (Connection conn = getConnection(); PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, status);
            ResultSet rs = pstmt.executeQuery();
            while (rs.next()) accounts.add(mapAccount(rs));
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return accounts;
    }

    public void updateAccountBalance(int accountId, double newBalance) {
        String sql = "UPDATE Accounts SET balance = ? WHERE id = ?";
        try (Connection conn = getConnection(); PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setDouble(1, newBalance);
            pstmt.setInt(2, accountId);
            pstmt.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public void updateAccountStatus(int accountId, String status) {
        String sql = "UPDATE Accounts SET status = ? WHERE id = ?";
        try (Connection conn = getConnection(); PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, status);
            pstmt.setInt(2, accountId);
            pstmt.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public void updateAccountInterestRate(int accountId, double rate) {
        String sql = "UPDATE Accounts SET interest_rate = ? WHERE id = ?";
        try (Connection conn = getConnection(); PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setDouble(1, rate);
            pstmt.setInt(2, accountId);
            pstmt.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public void insertTransaction(Transaction transaction) {
        String sql = "INSERT INTO Transactions (account_id, type, amount, description, reference_id, balance_after) VALUES (?, ?, ?, ?, ?, ?)";
        try (Connection conn = getConnection(); PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, transaction.getAccountId());
            pstmt.setString(2, transaction.getType());
            pstmt.setDouble(3, transaction.getAmount());
            pstmt.setString(4, transaction.getDescription());
            pstmt.setString(5, transaction.getReferenceId());
            pstmt.setDouble(6, transaction.getBalanceAfter());
            pstmt.executeUpdate();
            System.out.println("Transaction inserted.");
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private Transaction mapTransaction(ResultSet rs) throws SQLException {
        Transaction t = new Transaction();
        t.setId(rs.getInt("id"));
        t.setAccountId(rs.getInt("account_id"));
        t.setType(rs.getString("type"));
        t.setAmount(rs.getDouble("amount"));
        t.setDescription(rs.getString("description"));
        try { t.setReferenceId(rs.getString("reference_id")); } catch (SQLException ignored) {}
        Timestamp ts = rs.getTimestamp("timestamp");
        if (ts != null) t.setTimestamp(ts.toLocalDateTime());
        t.setBalanceAfter(rs.getDouble("balance_after"));
        return t;
    }

    public List<Transaction> getTransactionsByAccountId(int accountId) {
        List<Transaction> list = new ArrayList<>();
        String sql = "SELECT * FROM Transactions WHERE account_id = ? ORDER BY timestamp DESC";
        try (Connection conn = getConnection(); PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, accountId);
            ResultSet rs = pstmt.executeQuery();
            while (rs.next()) list.add(mapTransaction(rs));
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return list;
    }

    public List<Transaction> getTransactionsByAccountIdAndMonth(int accountId, int year, int month) {
        List<Transaction> list = new ArrayList<>();
        String sql = "SELECT * FROM Transactions WHERE account_id = ? AND strftime('%Y', timestamp) = ? AND strftime('%m', timestamp) = ? ORDER BY timestamp ASC";
        try (Connection conn = getConnection(); PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, accountId);
            pstmt.setString(2, String.format("%04d", year));
            pstmt.setString(3, String.format("%02d", month));
            ResultSet rs = pstmt.executeQuery();
            while (rs.next()) list.add(mapTransaction(rs));
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return list;
    }

    private Account getAccountById(Connection conn, int accountId) throws SQLException {
        String sql = "SELECT * FROM Accounts WHERE id = ?";
        try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, accountId);
            ResultSet rs = pstmt.executeQuery();
            if (rs.next()) return mapAccount(rs);
        }
        return null;
    }

    /** Performs transfer with a single DB transaction for integrity. Returns true on success. */
    public boolean transfer(int fromAccountId, int toAccountId, double amount, String referenceId) {
        if (amount <= 0) return false;
        Connection conn = null;
        try {
            conn = getConnection();
            conn.setAutoCommit(false);
            Account from = getAccountById(conn, fromAccountId);
            Account to = getAccountById(conn, toAccountId);
            if (from == null || to == null || from.getAvailableBalance() < amount) {
                conn.rollback();
                return false;
            }
            if (!Account.STATUS_ACTIVE.equals(from.getStatus()) || !Account.STATUS_ACTIVE.equals(to.getStatus())) {
                conn.rollback();
                return false;
            }
            double fromNewBalance = from.getBalance() - amount;
            double toNewBalance = to.getBalance() + amount;
            String ref = referenceId != null ? referenceId : UUID.randomUUID().toString();
            String sql = "INSERT INTO Transactions (account_id, type, amount, description, reference_id, balance_after) VALUES (?, ?, ?, ?, ?, ?)";
            try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
                pstmt.setInt(1, fromAccountId);
                pstmt.setString(2, Transaction.TYPE_TRANSFER_OUT);
                pstmt.setDouble(3, amount);
                pstmt.setString(4, "Transfer to " + to.getAccountNumber());
                pstmt.setString(5, ref);
                pstmt.setDouble(6, fromNewBalance);
                pstmt.executeUpdate();
                pstmt.setInt(1, toAccountId);
                pstmt.setString(2, Transaction.TYPE_TRANSFER_IN);
                pstmt.setDouble(3, amount);
                pstmt.setString(4, "Transfer from " + from.getAccountNumber());
                pstmt.setString(5, ref);
                pstmt.setDouble(6, toNewBalance);
                pstmt.executeUpdate();
            }
            try (PreparedStatement up = conn.prepareStatement("UPDATE Accounts SET balance = ? WHERE id = ?")) {
                up.setDouble(1, fromNewBalance);
                up.setInt(2, fromAccountId);
                up.executeUpdate();
                up.setDouble(1, toNewBalance);
                up.setInt(2, toAccountId);
                up.executeUpdate();
            }
            conn.commit();
            return true;
        } catch (SQLException e) {
            e.printStackTrace();
            if (conn != null) try { conn.rollback(); } catch (SQLException ignored) {}
            return false;
        } finally {
            if (conn != null) try { conn.setAutoCommit(true); conn.close(); } catch (SQLException ignored) {}
        }
    }

    
}