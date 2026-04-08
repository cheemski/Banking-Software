package Code;

/**
 * Holds the current logged-in user. Used by CLI to know who is acting.
 */
public class Session {
    private User currentUser;

    public User getCurrentUser() {
        return currentUser;
    }

    public void setCurrentUser(User currentUser) {
        this.currentUser = currentUser;
    }

    public boolean isLoggedIn() {
        return currentUser != null;
    }

    public boolean isAdmin() {
        return currentUser != null && currentUser.isAdmin();
    }

    public void logout() {
        this.currentUser = null;
    }
}
