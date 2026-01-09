package app;

import users.ManageUsers;
import services.ManageServices;
import services.ManageCatalog;
import data.AppData;
import data.DataStorage;
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

    public boolean startup() {
        AppData data = new AppData();
        if (storage.loadData(data)) {
            return applyAppData(data);
        }
        // If no data exists, start with empty collections
        return true;
    }

    public boolean shutdown() {
        AppData data = buildAppData();
        return storage.saveData(data) != null;
    }

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

    public boolean applyAppData(AppData data) {
        if (data == null) {
            return false;
        }

        // Apply users
        applyUsers(data.getUsers());

        // Apply services
        manageServices.loadServices(data.getServices());
        
        // Apply catalog items
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
