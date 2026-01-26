package util;

import java.io.*;
import java.util.ArrayList;

public class LogManager {
    private String filePath;

    public LogManager(String filePath) {
        this.filePath = filePath;
    }

    public boolean log(String username, String action) {
        try {
            // Lê conteúdo existente do ficheiro
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
            
            // Escreve novo log no início, seguido dos logs antigos
            FileWriter writer = new FileWriter(filePath, false); // false = sobrescrever
            BufferedWriter bufferedWriter = new BufferedWriter(writer);
            
            String logEntry = username + " " + action;
            
            bufferedWriter.write(logEntry);
            bufferedWriter.newLine();
            
            // Escreve logs anteriores
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
