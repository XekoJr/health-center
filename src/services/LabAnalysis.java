package services;

import java.util.ArrayList;

public class LabAnalysis {
    private int code;
    private String name;
    private String certification;
    private String methods;
    private ArrayList<ChemicalComponent> requiredComponents;
    private ArrayList<MedicalArea> areas;
    private ArrayList<Supplier> suppliers;

    public LabAnalysis(int code, String name, String certification, String methods) {
        this.code = code;
        this.name = name;
        this.certification = certification;
        this.methods = methods;
        this.requiredComponents = new ArrayList<>();
        this.areas = new ArrayList<>();
        this.suppliers = new ArrayList<>();
    }

    public boolean addTestToMedicalArea(Test aTest) {
        // To-Do
        return true;
    }

    public boolean addAreaToLabAnalysis(MedicalArea area) {
        if (area != null && !areas.contains(area)) {
            areas.add(area);
            return true;
        }
        return false;
    }

    public boolean addSupplierToLabAnalysis(Supplier aSupplier) {
        if (aSupplier != null && !suppliers.contains(aSupplier)) {
            suppliers.add(aSupplier);
            return true;
        }
        return false;
    }

    public boolean removeAreaFromLabAnalysis(MedicalArea area) {
        return areas.remove(area);
    }

    public boolean addRequiredComponent(ChemicalComponent component) {
        if (component != null && !requiredComponents.contains(component)) {
            requiredComponents.add(component);
            return true;
        }
        return false;
    }

    public boolean removeRequiredComponentFromCode(String code) {
        for (int i = 0; i < requiredComponents.size(); i++) {
            if (String.valueOf(requiredComponents.get(i).getCode()).equals(code)) {
                requiredComponents.remove(i);
                return true;
            }
        }
        return false;
    }

    public int getCode() {
        return code;
    }

    public String getName() {
        return name;
    }

    public String getCertification() {
        return certification;
    }

    public String getMethods() {
        return methods;
    }

    public ArrayList<ChemicalComponent> getRequiredComponents() {
        return new ArrayList<>(requiredComponents);
    }

    public ArrayList<MedicalArea> getAreas() {
        return new ArrayList<>(areas);
    }

    public ArrayList<Supplier> getSuppliers() {
        return new ArrayList<>(suppliers);
    }
}
