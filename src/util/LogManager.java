package util;

import java.io.*;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;

public class LogManager {
    private String filePath;
    private DateTimeFormatter formatter;

    public LogManager(String filePath) {
        this.filePath = filePath;
        this.formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
    }

    public boolean log(String username, String action) {
        try {
            FileWriter writer = new FileWriter(filePath, true);
            BufferedWriter bufferedWriter = new BufferedWriter(writer);
            
            String timestamp = LocalDateTime.now().format(formatter);
            String logEntry = String.format("[%s] User: %s | Action: %s%n", timestamp, username, action);
            
            bufferedWriter.write(logEntry);
            bufferedWriter.close();
            return true;
        } catch (IOException e) {
            System.err.println("Error writing to log: " + e.getMessage());
            return false;
        }
    }

    public ArrayList<String> readLog() {
        ArrayList<String> logs = new ArrayList<>();
        try {
            File file = new File(filePath);
            if (!file.exists()) {
                return logs;
            }

            FileReader reader = new FileReader(filePath);
            BufferedReader bufferedReader = new BufferedReader(reader);
            
            String line;
            while ((line = bufferedReader.readLine()) != null) {
                logs.add(line);
            }
            
            bufferedReader.close();
        } catch (IOException e) {
            System.err.println("Error reading log: " + e.getMessage());
        }
        return logs;
    }

    public String getFilePath() {
        return filePath;
    }

    public void setFilePath(String filePath) {
        this.filePath = filePath;
    }
}
