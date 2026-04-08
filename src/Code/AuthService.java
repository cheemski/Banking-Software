package Code;

/**
 * Handles login and password validation. Uses DatabaseManager for user lookup and hashing.
 */
public class AuthService {
    private final DatabaseManager db;

    public AuthService(DatabaseManager db) {
        this.db = db;
    }

    /**
     * Login by IC number and password. Returns the User if valid, null otherwise.
     */
    public User loginByIc(String icNumber, String password) {
        if (icNumber == null || password == null || icNumber.isBlank()) return null;
        User user = db.getUserByIc(icNumber.trim());
        if (user == null) return null;
        if (!db.validatePassword(password, user.getPasswordHash())) return null;
        return user;
    }

    /**
     * Login by email and password. Returns the User if valid, null otherwise.
     */
    public User loginByEmail(String email, String password) {
        if (email == null || password == null || email.isBlank()) return null;
        User user = db.getUserByEmail(email.trim());
        if (user == null) return null;
        if (!db.validatePassword(password, user.getPasswordHash())) return null;
        return user;
    }
}
