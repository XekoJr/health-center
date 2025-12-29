package data;

import users.User;
import services.Service;
import services.LabAnalysis;
import services.Category;
import services.Test;
import services.MedicalArea;
import services.Supplier;
import services.Order;
import services.ChemicalComponent;
import java.util.ArrayList;

public class AppData {
    private ArrayList<User> users;
    private ArrayList<Service> services;
    private ArrayList<LabAnalysis> analyses;
    private ArrayList<Category> categories;
    private ArrayList<Test> tests;
    private ArrayList<MedicalArea> areas;
    private ArrayList<Supplier> suppliers;
    private ArrayList<Order> orders;
    private ArrayList<ChemicalComponent> components;

    public AppData() {
        this.users = new ArrayList<>();
        this.services = new ArrayList<>();
        this.analyses = new ArrayList<>();
        this.categories = new ArrayList<>();
        this.tests = new ArrayList<>();
        this.areas = new ArrayList<>();
        this.suppliers = new ArrayList<>();
        this.orders = new ArrayList<>();
        this.components = new ArrayList<>();
    }

    public boolean setUsers(ArrayList<User> users) {
        this.users = users;
        return true;
    }

    public boolean setServices(ArrayList<Service> services) {
        this.services = services;
        return true;
    }

    public boolean setOrders(ArrayList<Order> orders) {
        this.orders = orders;
        return true;
    }

    public ArrayList<User> getUsers() {
        return users;
    }

    public ArrayList<Service> getServices() {
        return services;
    }

    public ArrayList<Order> getOrders() {
        return orders;
    }

    public ArrayList<LabAnalysis> getAnalyses() {
        return analyses;
    }

    public void setAnalyses(ArrayList<LabAnalysis> analyses) {
        this.analyses = analyses;
    }

    public ArrayList<Category> getCategories() {
        return categories;
    }

    public void setCategories(ArrayList<Category> categories) {
        this.categories = categories;
    }

    public ArrayList<Test> getTests() {
        return tests;
    }

    public void setTests(ArrayList<Test> tests) {
        this.tests = tests;
    }

    public ArrayList<MedicalArea> getAreas() {
        return areas;
    }

    public void setAreas(ArrayList<MedicalArea> areas) {
        this.areas = areas;
    }

    public ArrayList<Supplier> getSuppliers() {
        return suppliers;
    }

    public void setSuppliers(ArrayList<Supplier> suppliers) {
        this.suppliers = suppliers;
    }

    public ArrayList<ChemicalComponent> getComponents() {
        return components;
    }

    public void setComponents(ArrayList<ChemicalComponent> components) {
        this.components = components;
    }
}
