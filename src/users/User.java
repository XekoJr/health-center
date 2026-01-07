package users;

import java.io.Serializable;

public class User implements Serializable {
    private static final long serialVersionUID = 1L;
    private String username;
    private String email;
    private String password;
    private String name;
    private String status;
    private String type;
    private boolean reviewed;

    public User(String username, String email, String password, String name, String status, String type, boolean reviewed) {
        this.username = username;
        this.email = email;
        this.password = password;
        this.name = name;
        this.status = status;
        this.type = type;
        this.reviewed = reviewed;
    }

    public boolean verifyPassword(String aPassword) {
        return this.password.equals(aPassword);
    }

    public boolean setPassword(String aPassword) {
        this.password = aPassword;
        return true;
    }

    public boolean setEmail(String aEmail) {
        this.email = aEmail;
        return true;
    }

    public boolean setName(String aName) {
        this.name = aName;
        return true;
    }

    public boolean setStatus(String aState) {
        this.status = aState;
        return true;
    }

    public boolean setReviewed(boolean aReviewed) {
        this.reviewed = aReviewed;
        return true;
    }

    public String getEmail() {
        return email;
    }

    public String getUsername() {
        return username;
    }

    public String getType() {
        return type;
    }

    public boolean getReviewed() {
        return reviewed;
    }

    public String getName() {
        return name;
    }

    public String getStatus() {
        return status;
    }
}
