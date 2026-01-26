package data;

import users.User;
import java.io.*;
import java.util.ArrayList;
import java.util.Iterator;

public class CredentialsManager {
    private String filePath;

    public CredentialsManager(String filePath) {
        this.filePath = filePath;
    }

    public boolean saveCredentials(ArrayList<User> users) {
        try {
            FileWriter writer = new FileWriter(filePath);
            BufferedWriter bufferedWriter = new BufferedWriter(writer);
            
            Iterator<User> iterator = users.iterator();
            while (iterator.hasNext()) {
                User user = iterator.next();
                String line = user.getUsername() + ":" + user.getPassword() + ":" + 
                             user.getType() + ":" + user.getStatus();
                bufferedWriter.write(line);
                bufferedWriter.newLine();
            }
            
            bufferedWriter.close();
            return true;
        } catch (IOException e) {
            System.err.println("Error saving credentials: " + e.getMessage());
            return false;
        }
    }

    public ArrayList<String> loadCredentials() {
        ArrayList<String> credentials = new ArrayList<>();
        try {
            File file = new File(filePath);
            if (!file.exists()) {
                return credentials;
            }

            FileReader reader = new FileReader(filePath);
            BufferedReader bufferedReader = new BufferedReader(reader);
            
            String line;
            while ((line = bufferedReader.readLine()) != null) {
                credentials.add(line);
            }
            
            bufferedReader.close();
        } catch (IOException e) {
            System.err.println("Error loading credentials: " + e.getMessage());
        }
        return credentials;
    }

    public String getFilePath() {
        return filePath;
    }
}
