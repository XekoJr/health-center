package users;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.Collections;

public class ManageUsers {
    private ArrayList<User> users;

    public ManageUsers() {
        this.users = new ArrayList<>();
    }

    // Find user by username, returns null if not found
    private User findUserByUsername(String username) {
        Iterator<User> iterator = users.iterator();
        while (iterator.hasNext()) {
            User user = iterator.next();
            if (user.getUsername().equals(username)) {
                return user;
            }
        }
        return null;
    }

    // Login user with username and password
    public User login(String aUsername, String aPassword) {
        User user = findUserByUsername(aUsername);
        if (user != null && user.verifyPassword(aPassword)) {
            return user;
        }
        return null;
    }

    public ArrayList<User> searchUser(String attribute, String value) {
        ArrayList<User> results = new ArrayList<>();
        Iterator<User> iterator = users.iterator();
        while (iterator.hasNext()) {
            User user = iterator.next();
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

    // Check if username, email, NIF and phone are unique
    public boolean validateUnique(User aUser) {
        Iterator<User> iterator = users.iterator();
        while (iterator.hasNext()) {
            User user = iterator.next();
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

    // Approve or reject a user registration
    public boolean aproveUser(User aUser, boolean approved) {
        User user = findUserByUsername(aUser.getUsername());
        if (user != null) {
            user.setReviewed(approved);
            if (approved) {
                user.setStatus("approved");
            } else {
                user.setStatus("rejected");
            }
            return true;
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
        Iterator<User> iterator = users.iterator();
        while (iterator.hasNext()) {
            User user = iterator.next();
            if (user.getType().equals(aType)) {
                results.add(user);
            }
        }
        return results;
    }

    public boolean sortUsersByName(boolean ascending) {
        Collections.sort(users);
        if (!ascending) {
            Collections.reverse(users);
        }
        return true;
    }

    public ArrayList<User> searchUsersByUsername(String aUsername) {
        ArrayList<User> results = new ArrayList<>();
        Iterator<User> iterator = users.iterator();
        while (iterator.hasNext()) {
            User user = iterator.next();
            if (user.getUsername().toLowerCase().contains(aUsername.toLowerCase())) {
                results.add(user);
            }
        }
        return results;
    }

    public ArrayList<User> searchUsersByName(String aName) {
        ArrayList<User> results = new ArrayList<>();
        Iterator<User> iterator = users.iterator();
        while (iterator.hasNext()) {
            User user = iterator.next();
            if (user.getName().toLowerCase().contains(aName.toLowerCase())) {
                results.add(user);
            }
        }
        return results;
    }

    public ArrayList<User> searchUsersAdvanced(String aName) {
        ArrayList<User> results = new ArrayList<>();
        Iterator<User> iterator = users.iterator();
        while (iterator.hasNext()) {
            User user = iterator.next();
            if (user.getName().toLowerCase().contains(aName.toLowerCase()) ||
                    user.getUsername().toLowerCase().contains(aName.toLowerCase()) ||
                    user.getEmail().toLowerCase().contains(aName.toLowerCase())) {
                results.add(user);
            }
        }
        return results;
    }

    public void displayUserList(ArrayList<User> userList) {
        if (userList.isEmpty()) {
            System.out.println("Nenhum utilizador encontrado.");
            return;
        }
        
        Iterator<User> iterator = userList.iterator();
        while (iterator.hasNext()) {
            User user = iterator.next();
            System.out.println(user.toString());
        }
    }

    public void displayUserListIndexed(ArrayList<User> userList) {
        if (userList.isEmpty()) {
            System.out.println("Nenhum utilizador encontrado.");
            return;
        }

        int index = 1;
        Iterator<User> iterator = userList.iterator();
        while (iterator.hasNext()) {
            User user = iterator.next();
            System.out.println(index + ". " + user.getName());
            index++;
        }
    }

    public void displayUserListDetailed(ArrayList<User> userList) {
        if (userList.isEmpty()) {
            System.out.println("Nenhum utilizador encontrado.");
            return;
        }
        
        int index = 1;
        Iterator<User> iterator = userList.iterator();
        while (iterator.hasNext()) {
            User user = iterator.next();
            System.out.println(index + ". " + user.getName() + " (" + user.getUsername() + ")");
            index++;
        }
    }

    public ArrayList<Technician> listApprovedTechnicians() {
        ArrayList<Technician> approvedTechs = new ArrayList<>();
        Iterator<User> iterator = users.iterator();
        while (iterator.hasNext()) {
            User user = iterator.next();
            if (user instanceof Technician && "approved".equals(user.getStatus())) {
                approvedTechs.add((Technician) user);
            }
        }
        return approvedTechs;
    }
}
