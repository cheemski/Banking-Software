package Code;

import java.util.Objects;

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

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Session)) return false;
        Session session = (Session) o;
        return Objects.equals(currentUser, session.currentUser);
    }

    @Override
    public String toString() {
        return "Session{currentUser=" + currentUser + "}";
    }
}
