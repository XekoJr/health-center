package services;

import java.io.Serializable;

public class Test implements Serializable {
    private String designation;
    private String measuredValue;
    private String referenceValue;
    private String unit;

    public Test(String designation, String referenceValue, String unit) {
        this.designation = designation;
        this.referenceValue = referenceValue;
        this.unit = unit;
        this.measuredValue = "";
    }

    public boolean setMeasuredValue(String aValue) {
        this.measuredValue = aValue;
        return true;
    }

    public boolean setReferenceValue(String aValue) {
        this.referenceValue = aValue;
        return true;
    }

    public boolean setUnit(String aUnit) {
        this.unit = aUnit;
        return true;
    }

    public String getDesignation() {
        return designation;
    }

    public String getMeasuredValue() {
        return measuredValue;
    }

    public String getReferenceValue() {
        return referenceValue;
    }

    public String getUnit() {
        return unit;
    }
}
