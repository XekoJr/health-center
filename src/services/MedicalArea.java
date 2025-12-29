package services;

public class MedicalArea {
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
}
