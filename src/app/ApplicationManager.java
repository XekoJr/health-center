package app;

import users.ManageUsers;
import services.ManageServices;
import services.ManageCatalog;
import data.AppData;
import data.DataStorage;
import util.LogManager;
import java.util.Iterator;

public class ApplicationManager {
    private ManageUsers manageUsers;
    private ManageServices manageServices;
    private ManageCatalog manageCatalog;
    private DataStorage storage;
    private Session session;

    public ApplicationManager(String dataFilePath) {
        this.manageUsers = new ManageUsers();
        this.manageServices = new ManageServices();
        this.manageCatalog = new ManageCatalog();
        this.storage = new DataStorage(dataFilePath);
        this.session = new Session();
    }
    
    public void setLogManager(LogManager logManager) {
        this.storage.setLogManager(logManager);
        this.manageServices.setLogManager(logManager);
    }

    // Load all data from file on startup
    public boolean startup() {
        AppData data = new AppData();
        if (storage.loadData(data)) {
            return applyAppData(data);
        }
        return true;
    }

    // Save all data to file on shutdown
    public boolean shutdown() {
        AppData data = buildAppData();
        return storage.saveData(data) != null;
    }

    // Collect all current data from managers into one object
    public AppData buildAppData() {
        AppData data = new AppData();
        data.setUsers(manageUsers.listUsers());
        data.setServices(manageServices.listAllServices());
        data.setOrders(manageCatalog.getOrders());
        data.setAnalyses(manageCatalog.getAnalyses());
        data.setCategories(manageCatalog.getCategories());
        data.setTests(manageCatalog.getTests());
        data.setAreas(manageCatalog.getAreas());
        data.setSuppliers(manageCatalog.getSuppliers());
        data.setComponents(manageCatalog.getComponents());
        return data;
    }

    // Load data object into all managers
    public boolean applyAppData(AppData data) {
        if (data == null) {
            return false;
        }

        applyUsers(data.getUsers());
        manageServices.loadServices(data.getServices());
        applyCatalogItems(data);

        return true;
    }

    private void applyUsers(java.util.ArrayList<users.User> usersList) {
        Iterator<users.User> iterator = usersList.iterator();
        while (iterator.hasNext()) {
            users.User user = iterator.next();
            manageUsers.register(user);
        }
    }

    private void applyCatalogItems(AppData data) {
        Iterator<services.LabAnalysis> analysisIterator = data.getAnalyses().iterator();
        while (analysisIterator.hasNext()) {
            manageCatalog.addAnalysis(analysisIterator.next());
        }
        
        Iterator<services.Category> categoryIterator = data.getCategories().iterator();
        while (categoryIterator.hasNext()) {
            manageCatalog.addCategory(categoryIterator.next());
        }
        
        Iterator<services.Test> testIterator = data.getTests().iterator();
        while (testIterator.hasNext()) {
            manageCatalog.addTest(testIterator.next());
        }
        
        Iterator<services.MedicalArea> areaIterator = data.getAreas().iterator();
        while (areaIterator.hasNext()) {
            manageCatalog.addArea(areaIterator.next());
        }
        
        Iterator<services.Supplier> supplierIterator = data.getSuppliers().iterator();
        while (supplierIterator.hasNext()) {
            manageCatalog.addSupplier(supplierIterator.next());
        }
        
        Iterator<services.ChemicalComponent> componentIterator = data.getComponents().iterator();
        while (componentIterator.hasNext()) {
            manageCatalog.addComponent(componentIterator.next());
        }
        
        Iterator<services.Order> orderIterator = data.getOrders().iterator();
        while (orderIterator.hasNext()) {
            manageCatalog.addOrder(orderIterator.next());
        }
    }

    public ManageUsers getManageUsers() {
        return manageUsers;
    }

    public ManageServices getManageServices() {
        return manageServices;
    }

    public ManageCatalog getManageCatalog() {
        return manageCatalog;
    }

    public DataStorage getStorage() {
        return storage;
    }

    public Session getSession() {
        return session;
    }
}
