package services;

import users.Technician;
import java.util.ArrayList;

public class ManageCatalog {
    private ArrayList<LabAnalysis> analyses;
    private ArrayList<Category> categories;
    private ArrayList<Test> tests;
    private ArrayList<MedicalArea> areas;
    private ArrayList<Supplier> suppliers;
    private ArrayList<Order> orders;
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

    public boolean searchItem(String item, Object aTechnician) {
        // Generic search - implementation depends on what "item" represents
        return false;
    }

    public ArrayList<Object> searchItemType(String type, String attribute, String value, Object object) {
        // Generic search by type
        return new ArrayList<>();
    }

    public boolean removeItemType(String item, Object aTechnician) {
        // Generic remove
        return false;
    }

    public boolean addSupplierToAnalysis(int analysisCode, Supplier aSupplier) {
        for (LabAnalysis analysis : analyses) {
            if (analysis.getCode() == analysisCode) {
                return analysis.addSupplierToLabAnalysis(aSupplier);
            }
        }
        return false;
    }

    public boolean addAreaToAnalysis(int analysisCode, MedicalArea aArea) {
        for (LabAnalysis analysis : analyses) {
            if (analysis.getCode() == analysisCode) {
                return analysis.addAreaToLabAnalysis(aArea);
            }
        }
        return false;
    }

    public boolean addComponentToAnalysis(int analysisCode, ChemicalComponent aChemicalComponent) {
        for (LabAnalysis analysis : analyses) {
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

    public boolean deliverOrder(int orderCode) {
        for (Order order : orders) {
            if (order.getCode() == orderCode) {
                order.setStatus("delivered");
                order.setDeliveryDate(java.time.LocalDate.now().toString());
                return true;
            }
        }
        return false;
    }

    public boolean sortAnalysisByCode(boolean ascending) {
        for (int i = 0; i < analyses.size() - 1; i++) {
            for (int j = 0; j < analyses.size() - i - 1; j++) {
                int code1 = analyses.get(j).getCode();
                int code2 = analyses.get(j + 1).getCode();
                boolean shouldSwap = ascending ? code1 > code2 : code1 < code2;
                
                if (shouldSwap) {
                    LabAnalysis temp = analyses.get(j);
                    analyses.set(j, analyses.get(j + 1));
                    analyses.set(j + 1, temp);
                }
            }
        }
        return true;
    }

    public ArrayList<LabAnalysis> listAnalysis() {
        return new ArrayList<>(analyses);
    }

    public ArrayList<LabAnalysis> searchAnalysisByCode(String code) {
        ArrayList<LabAnalysis> results = new ArrayList<>();
        for (LabAnalysis analysis : analyses) {
            if (String.valueOf(analysis.getCode()).contains(code)) {
                results.add(analysis);
            }
        }
        return results;
    }

    public ArrayList<LabAnalysis> searchAnalysisByComponent(String componentCode) {
        ArrayList<LabAnalysis> results = new ArrayList<>();
        for (LabAnalysis analysis : analyses) {
            for (ChemicalComponent component : analysis.getRequiredComponents()) {
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
        for (LabAnalysis analysis : analyses) {
            if (String.valueOf(analysis.getCode()).contains(keyword) ||
                analysis.getName().toLowerCase().contains(keyword.toLowerCase()) ||
                analysis.getCertification().toLowerCase().contains(keyword.toLowerCase())) {
                results.add(analysis);
            }
        }
        return results;
    }

    public boolean sortOrdersByCode(boolean ascending) {
        // Bubble sort - no lambdas as per requirements
        for (int i = 0; i < orders.size() - 1; i++) {
            for (int j = 0; j < orders.size() - i - 1; j++) {
                int code1 = orders.get(j).getCode();
                int code2 = orders.get(j + 1).getCode();
                boolean shouldSwap = ascending ? code1 > code2 : code1 < code2;
                
                if (shouldSwap) {
                    Order temp = orders.get(j);
                    orders.set(j, orders.get(j + 1));
                    orders.set(j + 1, temp);
                }
            }
        }
        return true;
    }

    public ArrayList<Order> listOrders() {
        return new ArrayList<>(orders);
    }

    public ArrayList<ChemicalComponent> listChemicalComponent() {
        // Would need a separate collection for all components
        return new ArrayList<>();
    }

    public ArrayList<ChemicalComponent> searchChemicalComponentByName(String name) {
        // Would need a separate collection for all components
        return new ArrayList<>();
    }

    public ArrayList<Order> listDeliveredOrders() {
        ArrayList<Order> results = new ArrayList<>();
        for (Order order : orders) {
            if (order.getStatus().equals("delivered")) {
                results.add(order);
            }
        }
        return results;
    }

    // Getters for collections
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

    // Add methods for other collections
    public boolean addAnalysis(LabAnalysis analysis) {
        if (analysis != null) {
            analyses.add(analysis);
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
            return true;
        }
        return false;
    }

    public boolean addSupplier(Supplier supplier) {
        if (supplier != null) {
            suppliers.add(supplier);
            return true;
        }
        return false;
    }

    public boolean addComponent(ChemicalComponent component) {
        if (component != null) {
            // Components would need to be stored in a separate collection
            // For now, return true as placeholder
            return true;
        }
        return false;
    }

    public ArrayList<ChemicalComponent> listChemicalComponents() {
        ArrayList<ChemicalComponent> allComponents = new ArrayList<>();
        // Collect all components from all analyses
        for (LabAnalysis analysis : analyses) {
            for (ChemicalComponent component : analysis.getRequiredComponents()) {
                boolean exists = false;
                for (ChemicalComponent existing : allComponents) {
                    if (existing.getCode() == component.getCode()) {
                        exists = true;
                        break;
                    }
                }
                if (!exists) {
                    allComponents.add(component);
                }
            }
        }
        return allComponents;
    }
}
