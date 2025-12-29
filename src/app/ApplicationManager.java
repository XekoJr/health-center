package app;

import users.ManageUsers;
import services.ManageServices;
import services.ManageCatalog;
import data.AppData;
import data.DataStorage;

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
        // Components would need to be collected from catalog
        return data;
    }

    public boolean applyAppData(AppData data) {
        if (data == null) {
            return false;
        }

        // Apply users
        for (users.User user : data.getUsers()) {
            manageUsers.register(user);
        }

        // Apply services (would need access to internal list)
        // Apply catalog items
        for (services.LabAnalysis analysis : data.getAnalyses()) {
            manageCatalog.addAnalysis(analysis);
        }
        for (services.Category category : data.getCategories()) {
            manageCatalog.addCategory(category);
        }
        for (services.Test test : data.getTests()) {
            manageCatalog.addTest(test);
        }
        for (services.MedicalArea area : data.getAreas()) {
            manageCatalog.addArea(area);
        }
        for (services.Supplier supplier : data.getSuppliers()) {
            manageCatalog.addSupplier(supplier);
        }

        return true;
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
