package util;

import java.io.*;

public class SystemInfo implements Serializable {
    private int executionCount;
    private String lastUsername;
    private transient String filePath;

    public SystemInfo(String filePath) {
        this.filePath = filePath;
        this.executionCount = 0;
        this.lastUsername = "";
        loadFromFile();
    }

    // Load system info from file using object deserialization
    private void loadFromFile() {
        try {
            File file = new File(filePath);
            if (file.exists()) {
                FileInputStream fileIn = new FileInputStream(filePath);
                ObjectInputStream objectIn = new ObjectInputStream(fileIn);
                
                SystemInfo loaded = (SystemInfo) objectIn.readObject();
                this.executionCount = loaded.executionCount;
                this.lastUsername = loaded.lastUsername;
                
                objectIn.close();
                fileIn.close();
            }
        } catch (IOException | ClassNotFoundException e) {
            System.err.println("Error loading system info: " + e.getMessage());
        }
    }

    // Save system info to file using object serialization
    private void saveToFile() {
        try {
            FileOutputStream fileOut = new FileOutputStream(filePath);
            ObjectOutputStream objectOut = new ObjectOutputStream(fileOut);
            
            objectOut.writeObject(this);
            
            objectOut.close();
            fileOut.close();
        } catch (IOException e) {
            System.err.println("Error saving system info: " + e.getMessage());
        }
    }

    public boolean incrementExecutionCount() {
        this.executionCount++;
        saveToFile();
        return true;
    }

    public boolean setLastUsername(String username) {
        this.lastUsername = username;
        saveToFile();
        return true;
    }

    public int getExecutionCount() {
        return executionCount;
    }

    public String getLastUsername() {
        return lastUsername;
    }

    public String getFilePath() {
        return filePath;
    }
    
    public void setFilePath(String filePath) {
        this.filePath = filePath;
    }
}
