package app;

import users.User;

public class Session {
    private User currentUser;

    public Session() {
        this.currentUser = null;
    }

    public boolean setCurrentUser(User user) {
        this.currentUser = user;
        return true;
    }

    public User getCurrentUser() {
        return currentUser;
    }

    public boolean logout() {
        this.currentUser = null;
        return true;
    }

    public boolean isLoggedIn() {
        return currentUser != null;
    }
}
