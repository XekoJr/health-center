package users;

import java.util.ArrayList;
import java.util.Comparator;

public class ManageUsers {
    private ArrayList<User> users;

    public ManageUsers() {
        this.users = new ArrayList<>();
    }

    public User login(String aUsername, String aPassword) {
        for (User user : users) {
            if (user.getUsername().equals(aUsername) && user.verifyPassword(aPassword)) {
                return user;
            }
        }
        return null;
    }

    public ArrayList<User> searchUser(String attribute, String value) {
        ArrayList<User> results = new ArrayList<>();
        for (User user : users) {
            switch (attribute.toLowerCase()) {
                case "username":
                    if (user.getUsername().contains(value)) {
                        results.add(user);
                    }
                    break;
                case "name":
                    if (user.getName().contains(value)) {
                        results.add(user);
                    }
                    break;
                case "email":
                    if (user.getEmail().contains(value)) {
                        results.add(user);
                    }
                    break;
                case "type":
                    if (user.getType().equals(value)) {
                        results.add(user);
                    }
                    break;
                case "status":
                    if (user.getStatus().equals(value)) {
                        results.add(user);
                    }
                    break;
            }
        }
        return results;
    }

    public boolean validateUnique(User aUser) {
        for (User user : users) {
            if (user.getUsername().equals(aUser.getUsername())) {
                return false;
            }
        }
        return true;
    }

    public boolean register(User aUser) {
        if (validateUnique(aUser)) {
            users.add(aUser);
            return true;
        }
        return false;
    }

    public boolean aproveUser(User aUser, boolean approved) {
        for (User user : users) {
            if (user.getUsername().equals(aUser.getUsername())) {
                user.setReviewed(approved);
                if (approved) {
                    user.setStatus("approved");
                } else {
                    user.setStatus("rejected");
                }
                return true;
            }
        }
        return false;
    }

    public boolean isAdmin(User aUser) {
        return aUser instanceof Admin;
    }

    public boolean isTechnician(User aUser) {
        return aUser instanceof Technician;
    }

    public boolean isClient(User aUser) {
        return aUser instanceof Client;
    }

    public ArrayList<User> listUsers() {
        return new ArrayList<>(users);
    }

    public ArrayList<User> listUsersByType(String aType) {
        ArrayList<User> results = new ArrayList<>();
        for (User user : users) {
            if (user.getType().equals(aType)) {
                results.add(user);
            }
        }
        return results;
    }

    public boolean sortUsersByName(boolean ascending) {
        if (ascending) {
            users.sort(Comparator.comparing(User::getName));
        } else {
            users.sort(Comparator.comparing(User::getName).reversed());
        }
        return true;
    }

    public ArrayList<User> searchUsersByUsername(String aUsername) {
        ArrayList<User> results = new ArrayList<>();
        for (User user : users) {
            if (user.getUsername().toLowerCase().contains(aUsername.toLowerCase())) {
                results.add(user);
            }
        }
        return results;
    }

    public ArrayList<User> searchUsersByName(String aName) {
        ArrayList<User> results = new ArrayList<>();
        for (User user : users) {
            if (user.getName().toLowerCase().contains(aName.toLowerCase())) {
                results.add(user);
            }
        }
        return results;
    }

    public ArrayList<User> searchUsersAdvanced(String aName) {
        ArrayList<User> results = new ArrayList<>();
        for (User user : users) {
            if (user.getName().toLowerCase().contains(aName.toLowerCase()) ||
                    user.getUsername().toLowerCase().contains(aName.toLowerCase()) ||
                    user.getEmail().toLowerCase().contains(aName.toLowerCase())) {
                results.add(user);
            }
        }
        return results;
    }
}
