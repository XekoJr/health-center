package users;

import java.util.ArrayList;

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
            if (user.getEmail().equals(aUser.getEmail())) {
                return false;
            }
            
            // Check NIF and phone uniqueness for Client and Technician
            if (aUser instanceof Client && user instanceof Client) {
                Client newClient = (Client) aUser;
                Client existingClient = (Client) user;
                if (newClient.getNif().equals(existingClient.getNif())) {
                    return false;
                }
                if (newClient.getPhone().equals(existingClient.getPhone())) {
                    return false;
                }
            }
            
            if (aUser instanceof Technician && user instanceof Technician) {
                Technician newTech = (Technician) aUser;
                Technician existingTech = (Technician) user;
                if (newTech.getNif().equals(existingTech.getNif())) {
                    return false;
                }
                if (newTech.getPhone().equals(existingTech.getPhone())) {
                    return false;
                }
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
        for (int i = 0; i < users.size() - 1; i++) {
            for (int j = 0; j < users.size() - i - 1; j++) {
                String name1 = users.get(j).getName();
                String name2 = users.get(j + 1).getName();
                boolean shouldSwap = ascending ? name1.compareTo(name2) > 0 : name1.compareTo(name2) < 0;
                
                if (shouldSwap) {
                    User temp = users.get(j);
                    users.set(j, users.get(j + 1));
                    users.set(j + 1, temp);
                }
            }
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
