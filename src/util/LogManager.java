package util;

import java.io.*;
import java.util.ArrayList;

// Manages system log file - records all user actions
public class LogManager {
    private String filePath;

    public LogManager(String filePath) {
        this.filePath = filePath;
    }

    // Write new log entry - most recent entries go first
    public boolean log(String username, String action) {
        try {
            // Read existing log entries
            ArrayList<String> existingLogs = new ArrayList<>();
            File file = new File(filePath);
            
            if (file.exists()) {
                FileReader reader = new FileReader(filePath);
                BufferedReader bufferedReader = new BufferedReader(reader);
                String line;
                while ((line = bufferedReader.readLine()) != null) {
                    existingLogs.add(line);
                }
                bufferedReader.close();
            }
            
            // Write new log at top, then old logs below
            FileWriter writer = new FileWriter(filePath, false); // overwrite mode
            BufferedWriter bufferedWriter = new BufferedWriter(writer);
            
            String logEntry = username + " " + action;
            
            bufferedWriter.write(logEntry);
            bufferedWriter.newLine();
            
            // Write previous logs
            for (String oldLog : existingLogs) {
                bufferedWriter.write(oldLog);
                bufferedWriter.newLine();
            }
            
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
