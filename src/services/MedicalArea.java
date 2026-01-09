package services;

import java.io.Serializable;

public class MedicalArea implements Serializable, Comparable<MedicalArea> {
    private static final long serialVersionUID = 1L;
    private int code;
    private String designation;
    private String family;

    public MedicalArea(int code, String designation, String family) {
        this.code = code;
        this.designation = designation;
        this.family = family;
    }

    public boolean setFamily(String aFamily) {
        this.family = aFamily;
        return true;
    }

    public boolean setDesignation(String aName) {
        this.designation = aName;
        return true;
    }

    public int getCode() {
        return code;
    }

    public String getDesignation() {
        return designation;
    }

    public String getFamily() {
        return family;
    }

    public int compareTo(MedicalArea other) {
        return Integer.compare(this.code, other.code);
    }

    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("-----------------------------------------\n");
        sb.append("Codigo: ").append(code).append("\n");
        sb.append("Designacao: ").append(designation).append("\n");
        sb.append("Familia: ").append(family);
        return sb.toString();
    }
}
