package services;

import users.Technician;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.Collections;

public class ManageCatalog {
    private ArrayList<LabAnalysis> analyses;
    private ArrayList<Category> categories;
    private ArrayList<Test> tests;
    private ArrayList<MedicalArea> areas;
    private ArrayList<Supplier> suppliers;
    private ArrayList<Order> orders;
    private ArrayList<ChemicalComponent> components;
    private int nextAnalysisCode;
    private int nextComponentCode;
    private int nextSupplierCode;
    private int nextAreaCode;
    private int nextOrderCode;

    public ManageCatalog() {
        this.analyses = new ArrayList<>();
        this.categories = new ArrayList<>();
        this.tests = new ArrayList<>();
        this.areas = new ArrayList<>();
        this.suppliers = new ArrayList<>();
        this.orders = new ArrayList<>();
        this.components = new ArrayList<>();
        this.nextAnalysisCode = 1;
        this.nextComponentCode = 1;
        this.nextSupplierCode = 1;
        this.nextAreaCode = 1;
        this.nextOrderCode = 1;
    }
    
    public int generateAnalysisCode() {
        return nextAnalysisCode++;
    }
    
    public int generateComponentCode() {
        return nextComponentCode++;
    }
    
    public int generateSupplierCode() {
        return nextSupplierCode++;
    }
    
    public int generateAreaCode() {
        return nextAreaCode++;
    }
    
    public int generateOrderCode() {
        return nextOrderCode++;
    }

    public boolean addSupplierToAnalysis(int analysisCode, Supplier aSupplier) {
        Iterator<LabAnalysis> iterator = analyses.iterator();
        while (iterator.hasNext()) {
            LabAnalysis analysis = iterator.next();
            if (analysis.getCode() == analysisCode) {
                return analysis.addSupplierToLabAnalysis(aSupplier);
            }
        }
        return false;
    }

    public boolean addAreaToAnalysis(int analysisCode, MedicalArea aArea) {
        Iterator<LabAnalysis> iterator = analyses.iterator();
        while (iterator.hasNext()) {
            LabAnalysis analysis = iterator.next();
            if (analysis.getCode() == analysisCode) {
                return analysis.addAreaToLabAnalysis(aArea);
            }
        }
        return false;
    }

    public boolean addComponentToAnalysis(int analysisCode, ChemicalComponent aChemicalComponent) {
        Iterator<LabAnalysis> iterator = analyses.iterator();
        while (iterator.hasNext()) {
            LabAnalysis analysis = iterator.next();
            if (analysis.getCode() == analysisCode) {
                return analysis.addRequiredComponent(aChemicalComponent);
            }
        }
        return false;
    }

    public boolean createOrder(Technician aTechnician, Supplier aSupplier, Order aOrder) {
        if (aOrder != null) {
            orders.add(aOrder);
            return true;
        }
        return false;
    }

    public boolean addOrder(Order order) {
        if (order != null) {
            orders.add(order);
            // Update nextOrderCode if needed
            if (order.getCode() >= nextOrderCode) {
                nextOrderCode = order.getCode() + 1;
            }
            return true;
        }
        return false;
    }

    public boolean deliverOrder(int orderCode) {
        Iterator<Order> iterator = orders.iterator();
        while (iterator.hasNext()) {
            Order order = iterator.next();
            if (order.getCode() == orderCode) {
                order.setStatus("delivered");
                order.setDeliveryDate(java.time.LocalDate.now().toString());
                return true;
            }
        }
        return false;
    }

    public boolean sortAnalysisByCode(boolean ascending) {
        Collections.sort(analyses);
        if (!ascending) {
            Collections.reverse(analyses);
        }
        return true;
    }

    public ArrayList<LabAnalysis> listAnalysis() {
        return new ArrayList<>(analyses);
    }

    public ArrayList<LabAnalysis> searchAnalysisByCode(String code) {
        ArrayList<LabAnalysis> results = new ArrayList<>();
        Iterator<LabAnalysis> iterator = analyses.iterator();
        while (iterator.hasNext()) {
            LabAnalysis analysis = iterator.next();
            if (String.valueOf(analysis.getCode()).contains(code)) {
                results.add(analysis);
            }
        }
        return results;
    }

    public ArrayList<LabAnalysis> searchAnalysisByComponent(String componentCode) {
        ArrayList<LabAnalysis> results = new ArrayList<>();
        Iterator<LabAnalysis> iterator = analyses.iterator();
        while (iterator.hasNext()) {
            LabAnalysis analysis = iterator.next();
            Iterator<ChemicalComponent> compIterator = analysis.getRequiredComponents().iterator();
            while (compIterator.hasNext()) {
                ChemicalComponent component = compIterator.next();
                if (String.valueOf(component.getCode()).equals(componentCode)) {
                    results.add(analysis);
                    break;
                }
            }
        }
        return results;
    }

    public ArrayList<LabAnalysis> searchAnalysisAdvanced(String keyword) {
        ArrayList<LabAnalysis> results = new ArrayList<>();
        Iterator<LabAnalysis> iterator = analyses.iterator();
        while (iterator.hasNext()) {
            LabAnalysis analysis = iterator.next();
            if (String.valueOf(analysis.getCode()).contains(keyword) ||
                analysis.getName().toLowerCase().contains(keyword.toLowerCase()) ||
                analysis.getCertification().toLowerCase().contains(keyword.toLowerCase())) {
                results.add(analysis);
            }
        }
        return results;
    }

    public boolean sortOrdersByCode(boolean ascending) {
        Collections.sort(orders);
        if (!ascending) {
            Collections.reverse(orders);
        }
        return true;
    }

    public ArrayList<Order> listOrders() {
        return new ArrayList<>(orders);
    }

    public ArrayList<ChemicalComponent> listChemicalComponent() {
        return new ArrayList<>();
    }

    public ArrayList<ChemicalComponent> searchChemicalComponentByName(String name) {
        return new ArrayList<>();
    }

    public ArrayList<Order> listDeliveredOrders() {
        ArrayList<Order> results = new ArrayList<>();
        Iterator<Order> iterator = orders.iterator();
        while (iterator.hasNext()) {
            Order order = iterator.next();
            if (order.getStatus().equals("delivered")) {
                results.add(order);
            }
        }
        return results;
    }

    // Getters
    public ArrayList<LabAnalysis> getAnalyses() {
        return new ArrayList<>(analyses);
    }

    public ArrayList<Category> getCategories() {
        return new ArrayList<>(categories);
    }

    public ArrayList<Test> getTests() {
        return new ArrayList<>(tests);
    }

    public ArrayList<MedicalArea> getAreas() {
        return new ArrayList<>(areas);
    }

    public ArrayList<Supplier> getSuppliers() {
        return new ArrayList<>(suppliers);
    }

    public ArrayList<Order> getOrders() {
        return new ArrayList<>(orders);
    }

    public ArrayList<ChemicalComponent> getComponents() {
        return new ArrayList<>(components);
    }

    public boolean addAnalysis(LabAnalysis analysis) {
        if (analysis != null) {
            analyses.add(analysis);
            if (analysis.getCode() >= nextAnalysisCode) {
                nextAnalysisCode = analysis.getCode() + 1;
            }
            return true;
        }
        return false;
    }

    public boolean addCategory(Category category) {
        if (category != null) {
            categories.add(category);
            return true;
        }
        return false;
    }

    public boolean addTest(Test test) {
        if (test != null) {
            tests.add(test);
            return true;
        }
        return false;
    }

    public boolean addArea(MedicalArea area) {
        if (area != null) {
            areas.add(area);
            if (area.getCode() >= nextAreaCode) {
                nextAreaCode = area.getCode() + 1;
            }
            return true;
        }
        return false;
    }

    public boolean addSupplier(Supplier supplier) {
        if (supplier != null) {
            suppliers.add(supplier);
            if (supplier.getCode() >= nextSupplierCode) {
                nextSupplierCode = supplier.getCode() + 1;
            }
            return true;
        }
        return false;
    }

    public boolean addComponent(ChemicalComponent component) {
        if (component != null) {
            components.add(component);
            // Update nextComponentCode if needed
            if (component.getCode() >= nextComponentCode) {
                nextComponentCode = component.getCode() + 1;
            }
            return true;
        }
        return false;
    }

    public ArrayList<ChemicalComponent> listChemicalComponents() {
        return new ArrayList<>(components);
    }

    // Search/list methods with unified approach (empty term = list all)
    public ArrayList<LabAnalysis> searchAnalyses(String keyword) {
        if (keyword == null || keyword.trim().isEmpty()) {
            sortAnalysisByCode(true);
            return listAnalysis();
        }
        return searchAnalysisAdvanced(keyword.trim());
    }

    public ArrayList<ChemicalComponent> searchComponents(String keyword) {
        if (keyword == null || keyword.trim().isEmpty()) {
            ArrayList<ChemicalComponent> allComponents = new ArrayList<>(components);
            sortComponentsByCode(allComponents, true);
            return allComponents;
        }
        
        ArrayList<ChemicalComponent> results = new ArrayList<>();
        String searchTerm = keyword.trim().toLowerCase();
        Iterator<ChemicalComponent> iterator = components.iterator();
        while (iterator.hasNext()) {
            ChemicalComponent component = iterator.next();
            if (String.valueOf(component.getCode()).contains(searchTerm) ||
                component.getName().toLowerCase().contains(searchTerm)) {
                results.add(component);
            }
        }
        return results;
    }

    public ArrayList<Supplier> searchSuppliers(String keyword) {
        if (keyword == null || keyword.trim().isEmpty()) {
            sortSuppliersByCode(true);
            return new ArrayList<>(suppliers);
        }
        
        ArrayList<Supplier> results = new ArrayList<>();
        String searchTerm = keyword.trim().toLowerCase();
        Iterator<Supplier> iterator = suppliers.iterator();
        while (iterator.hasNext()) {
            Supplier supplier = iterator.next();
            if (String.valueOf(supplier.getCode()).contains(searchTerm) ||
                supplier.getName().toLowerCase().contains(searchTerm) ||
                supplier.getEmail().toLowerCase().contains(searchTerm)) {
                results.add(supplier);
            }
        }
        return results;
    }

    public ArrayList<MedicalArea> searchAreas(String keyword) {
        if (keyword == null || keyword.trim().isEmpty()) {
            sortAreasByCode(true);
            return new ArrayList<>(areas);
        }
        
        ArrayList<MedicalArea> results = new ArrayList<>();
        String searchTerm = keyword.trim().toLowerCase();
        Iterator<MedicalArea> iterator = areas.iterator();
        while (iterator.hasNext()) {
            MedicalArea area = iterator.next();
            if (String.valueOf(area.getCode()).contains(searchTerm) ||
                area.getDesignation().toLowerCase().contains(searchTerm) ||
                area.getFamily().toLowerCase().contains(searchTerm)) {
                results.add(area);
            }
        }
        return results;
    }

    public ArrayList<Order> searchOrders(String keyword) {
        if (keyword == null || keyword.trim().isEmpty()) {
            sortOrdersByCode(true);
            return listOrders();
        }
        
        ArrayList<Order> results = new ArrayList<>();
        String searchTerm = keyword.trim().toLowerCase();
        Iterator<Order> iterator = orders.iterator();
        while (iterator.hasNext()) {
            Order order = iterator.next();
            if (String.valueOf(order.getCode()).contains(searchTerm) ||
                order.getSupplier().getName().toLowerCase().contains(searchTerm) ||
                order.getStatus().toLowerCase().contains(searchTerm)) {
                results.add(order);
            }
        }
        return results;
    }

    // Sorting methods for new entities
    private void sortComponentsByCode(ArrayList<ChemicalComponent> components, boolean ascending) {
        Collections.sort(components);
        if (!ascending) {
            Collections.reverse(components);
        }
    }

    public boolean sortSuppliersByCode(boolean ascending) {
        Collections.sort(suppliers);
        if (!ascending) {
            Collections.reverse(suppliers);
        }
        return true;
    }

    public boolean sortAreasByCode(boolean ascending) {
        Collections.sort(areas);
        if (!ascending) {
            Collections.reverse(areas);
        }
        return true;
    }

    // Remove methods
    public boolean removeAnalysis(int code) {
        Iterator<LabAnalysis> iterator = analyses.iterator();
        while (iterator.hasNext()) {
            LabAnalysis analysis = iterator.next();
            if (analysis.getCode() == code) {
                iterator.remove();
                return true;
            }
        }
        return false;
    }

    public boolean removeComponent(int code) {
        Iterator<ChemicalComponent> iterator = components.iterator();
        while (iterator.hasNext()) {
            ChemicalComponent component = iterator.next();
            if (component.getCode() == code) {
                iterator.remove();
                // Also remove from all analyses
                Iterator<LabAnalysis> analysisIterator = analyses.iterator();
                while (analysisIterator.hasNext()) {
                    LabAnalysis analysis = analysisIterator.next();
                    analysis.removeRequiredComponentFromCode(String.valueOf(code));
                }
                return true;
            }
        }
        return false;
    }

    public boolean removeSupplier(int code) {
        Iterator<Supplier> iterator = suppliers.iterator();
        while (iterator.hasNext()) {
            Supplier supplier = iterator.next();
            if (supplier.getCode() == code) {
                iterator.remove();
                return true;
            }
        }
        return false;
    }

    public boolean removeArea(int code) {
        Iterator<MedicalArea> iterator = areas.iterator();
        while (iterator.hasNext()) {
            MedicalArea area = iterator.next();
            if (area.getCode() == code) {
                iterator.remove();
                return true;
            }
        }
        return false;
    }

    public boolean removeOrder(int code) {
        Iterator<Order> iterator = orders.iterator();
        while (iterator.hasNext()) {
            Order order = iterator.next();
            if (order.getCode() == code) {
                iterator.remove();
                return true;
            }
        }
        return false;
    }

    // Find methods for editing
    public LabAnalysis findAnalysis(int code) {
        Iterator<LabAnalysis> iterator = analyses.iterator();
        while (iterator.hasNext()) {
            LabAnalysis analysis = iterator.next();
            if (analysis.getCode() == code) {
                return analysis;
            }
        }
        return null;
    }

    public ChemicalComponent findComponent(int code) {
        Iterator<ChemicalComponent> iterator = components.iterator();
        while (iterator.hasNext()) {
            ChemicalComponent component = iterator.next();
            if (component.getCode() == code) {
                return component;
            }
        }
        return null;
    }

    public Supplier findSupplier(int code) {
        Iterator<Supplier> iterator = suppliers.iterator();
        while (iterator.hasNext()) {
            Supplier supplier = iterator.next();
            if (supplier.getCode() == code) {
                return supplier;
            }
        }
        return null;
    }

    public MedicalArea findArea(int code) {
        Iterator<MedicalArea> iterator = areas.iterator();
        while (iterator.hasNext()) {
            MedicalArea area = iterator.next();
            if (area.getCode() == code) {
                return area;
            }
        }
        return null;
    }

    public Order findOrder(int code) {
        Iterator<Order> iterator = orders.iterator();
        while (iterator.hasNext()) {
            Order order = iterator.next();
            if (order.getCode() == code) {
                return order;
            }
        }
        return null;
    }

    public ArrayList<Order> listPendingOrders() {
        ArrayList<Order> results = new ArrayList<>();
        Iterator<Order> iterator = orders.iterator();
        while (iterator.hasNext()) {
            Order order = iterator.next();
            if (!order.getStatus().equals("delivered")) {
                results.add(order);
            }
        }
        return results;
    }
}
