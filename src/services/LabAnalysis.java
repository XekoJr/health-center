package services;

import java.util.ArrayList;
import java.util.Iterator;
import java.io.Serializable;

public class LabAnalysis implements Serializable, Comparable<LabAnalysis> {
    private int code;
    private String name;
    private Certification certification;
    private String methods;
    private float value;
    private ArrayList<ChemicalComponent> requiredComponents;
    private ArrayList<Test> tests;
    private MedicalArea[] areas = new MedicalArea[4];
    private int areaCount;
    private Supplier[] suppliers = new Supplier[6];
    private int supplierCount;

    public LabAnalysis(int code, String name, Certification certification, String methods) {
        this.code = code;
        this.name = name;
        this.certification = certification;
        this.methods = methods;
        this.value = 0.0f;
        this.requiredComponents = new ArrayList<>();
        this.tests = new ArrayList<>();
        this.areas = new MedicalArea[4];
        this.areaCount = 0;
        this.suppliers = new Supplier[6];
        this.supplierCount = 0;
    }

    public boolean addAreaToLabAnalysis(MedicalArea area) {
        if (area == null) {
            return false;
        }
        
        if (areaCount >= 4) {
            return false;
        }
        
        for (int i = 0; i < areaCount; i++) {
            if (areas[i].equals(area)) {
                return false;
            }
        }
        
        areas[areaCount] = area;
        areaCount++;
        return true;
    }

    public boolean addSupplierToLabAnalysis(Supplier aSupplier) {
        if (aSupplier == null) {
            return false;
        }
        
        if (supplierCount >= 6) {
            return false;
        }
        
        for (int i = 0; i < supplierCount; i++) {
            if (suppliers[i].equals(aSupplier)) {
                return false;
            }
        }
        
        suppliers[supplierCount] = aSupplier;
        supplierCount++;
        return true;
    }

    public boolean removeAreaFromLabAnalysis(MedicalArea area) {
        if (area == null) {
            return false;
        }
        
        for (int i = 0; i < areaCount; i++) {
            if (areas[i].equals(area)) {
                for (int j = i; j < areaCount - 1; j++) {
                    areas[j] = areas[j + 1];
                }
                areas[areaCount - 1] = null;
                areaCount--;
                return true;
            }
        }
        return false;
    }

    public boolean removeSupplierFromLabAnalysis(Supplier supplier) {
        if (supplier == null) {
            return false;
        }
        
        for (int i = 0; i < supplierCount; i++) {
            if (suppliers[i].equals(supplier)) {
                for (int j = i; j < supplierCount - 1; j++) {
                    suppliers[j] = suppliers[j + 1];
                }
                suppliers[supplierCount - 1] = null;
                supplierCount--;
                return true;
            }
        }
        return false;
    }

    public boolean addRequiredComponent(ChemicalComponent component) {
        if (component != null && !requiredComponents.contains(component)) {
            requiredComponents.add(component);
            return true;
        }
        return false;
    }

    public boolean addTest(Test test) {
        if (test != null && !tests.contains(test)) {
            tests.add(test);
            return true;
        }
        return false;
    }

    public boolean removeTest(String testDesignation) {
        Iterator<Test> iterator = tests.iterator();
        while (iterator.hasNext()) {
            Test test = iterator.next();
            if (test.getDesignation().equals(testDesignation)) {
                iterator.remove();
                return true;
            }
        }
        return false;
    }

    public boolean removeRequiredComponentFromCode(String code) {
        Iterator<ChemicalComponent> iterator = requiredComponents.iterator();
        while (iterator.hasNext()) {
            ChemicalComponent component = iterator.next();
            if (String.valueOf(component.getCode()).equals(code)) {
                iterator.remove();
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

    public Certification getCertification() {
        return certification;
    }

    public void setCertification(Certification certification) {
        this.certification = certification;
    }

    public String getMethods() {
        return methods;
    }

    public ArrayList<ChemicalComponent> getRequiredComponents() {
        return new ArrayList<>(requiredComponents);
    }

    public ArrayList<Test> getTests() {
        return new ArrayList<>(tests);
    }

    public ArrayList<MedicalArea> getAreas() {
        ArrayList<MedicalArea> areaList = new ArrayList<>();
        for (int i = 0; i < areaCount; i++) {
            areaList.add(areas[i]);
        }
        return areaList;
    }

    public ArrayList<Supplier> getSuppliers() {
        ArrayList<Supplier> supplierList = new ArrayList<>();
        for (int i = 0; i < supplierCount; i++) {
            supplierList.add(suppliers[i]);
        }
        return supplierList;
    }
    
    public float getValue() {
        return value;
    }
    
    public void setValue(float value) {
        this.value = value;
    }
    
    public int compareTo(LabAnalysis other) {
        return Integer.compare(this.code, other.code);
    }
    
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("-----------------------------------------\n");
        sb.append("Codigo: ").append(code).append("\n");
        sb.append("Nome: ").append(name).append("\n");
        sb.append("Certificacao: ").append(certification).append("\n");
        sb.append("Metodos: ").append(methods).append("\n");
        sb.append("Valor: ").append(value).append(" euros\n");
        sb.append("Componentes necessarios: ").append(requiredComponents.size()).append("\n");
        sb.append("Testes: ").append(tests.size()).append("\n");
        sb.append("Fornecedores: ").append(supplierCount).append("/6\n");
        sb.append("Areas medicas: ").append(areaCount).append("/4");
        return sb.toString();
    }
}
