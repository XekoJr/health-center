package data;

import java.io.*;

public class DataStorage {
    private String filePath;

    public DataStorage(String filePath) {
        this.filePath = filePath;
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
            System.err.println("Error loading data: " + e.getMessage());
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
            System.err.println("Error saving data: " + e.getMessage());
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
