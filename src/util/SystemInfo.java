package util;

import java.io.*;
import java.util.Properties;

public class SystemInfo {
    private int executionCount;
    private String lastUsername;
    private String filePath;
    private Properties properties;

    public SystemInfo(String filePath) {
        this.filePath = filePath;
        this.properties = new Properties();
        this.executionCount = 0;
        this.lastUsername = "";
        loadFromFile();
    }

    private void loadFromFile() {
        try {
            File file = new File(filePath);
            if (file.exists()) {
                FileInputStream input = new FileInputStream(filePath);
                properties.load(input);
                input.close();

                this.executionCount = Integer.parseInt(properties.getProperty("executionCount", "0"));
                this.lastUsername = properties.getProperty("lastUsername", "");
            }
        } catch (IOException | NumberFormatException e) {
            System.err.println("Error loading system info: " + e.getMessage());
        }
    }

    private void saveToFile() {
        try {
            FileOutputStream output = new FileOutputStream(filePath);
            properties.setProperty("executionCount", String.valueOf(executionCount));
            properties.setProperty("lastUsername", lastUsername);
            properties.store(output, "System Information");
            output.close();
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
}
