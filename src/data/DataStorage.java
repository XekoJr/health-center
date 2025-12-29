package data;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import java.io.*;

public class DataStorage {
    private String filePath;
    private Gson gson;

    public DataStorage(String filePath) {
        this.filePath = filePath;
        this.gson = new GsonBuilder().setPrettyPrinting().create();
    }

    public boolean loadData(AppData data) {
        try {
            if (!fileExists()) {
                return false;
            }
            
            FileReader reader = new FileReader(filePath);
            AppData loadedData = gson.fromJson(reader, AppData.class);
            reader.close();

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
        } catch (IOException e) {
            System.err.println("Error loading data: " + e.getMessage());
            return false;
        }
    }

    public AppData saveData(AppData data) {
        try {
            FileWriter writer = new FileWriter(filePath);
            gson.toJson(data, writer);
            writer.close();
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
