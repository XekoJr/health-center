package data;

import util.LogManager;
import java.io.*;

public class DataStorage {
    private String filePath;
    private LogManager logManager;

    public DataStorage(String filePath) {
        this.filePath = filePath;
        this.logManager = null;
    }
    
    public void setLogManager(LogManager logManager) {
        this.logManager = logManager;
    }

    // Load data from file - returns false if file doesn't exist
    public boolean loadData(AppData data) {
        try {
            if (!fileExists()) {
                return false;
            }
            
            FileInputStream fileIn = new FileInputStream(filePath);
            ObjectInputStream objectIn = new ObjectInputStream(fileIn);
            AppData loadedData = (AppData) objectIn.readObject();
            objectIn.close();
            fileIn.close();

            if (loadedData != null) {
                data.setUsers(loadedData.getUsers());
                data.setServices(loadedData.getServices());
                data.setOrders(loadedData.getOrders());
                data.setAnalyses(loadedData.getAnalyses());
                data.setCategories(loadedData.getCategories());
                data.setTests(loadedData.getTests());
                data.setAreas(loadedData.getAreas());
                data.setSuppliers(loadedData.getSuppliers());
                data.setComponents(loadedData.getComponents());
                return true;
            }
            return false;
        } catch (IOException | ClassNotFoundException e) {
            String errorMsg = "Error loading data: " + e.getMessage();
            System.err.println(errorMsg);
            if (logManager != null) {
                logManager.log("SYSTEM", errorMsg);
            }
            return false;
        }
    }

    // Save data to file - returns null if error occurs
    public AppData saveData(AppData data) {
        try {
            FileOutputStream fileOut = new FileOutputStream(filePath);
            ObjectOutputStream objectOut = new ObjectOutputStream(fileOut);
            objectOut.writeObject(data);
            objectOut.close();
            fileOut.close();
            return data;
        } catch (IOException e) {
            String errorMsg = "Error saving data: " + e.getMessage();
            System.err.println(errorMsg);
            if (logManager != null) {
                logManager.log("SYSTEM", errorMsg);
            }
            return null;
        }
    }

    public boolean fileExists() {
        File file = new File(filePath);
        return file.exists();
    }

    public String getFilePath() {
        return filePath;
    }

    public void setFilePath(String filePath) {
        this.filePath = filePath;
    }
}
