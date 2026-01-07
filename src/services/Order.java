package services;

import users.Technician;
import java.util.ArrayList;
import java.io.Serializable;

public class Order implements Serializable {
    private static final long serialVersionUID = 1L;
    private int code;
    private Supplier supplier;
    private Technician technician;
    private ArrayList<ChemicalComponent> items;
    private String requestDate;
    private String deliveryDate;
    private String status;

    public Order(int code, Supplier supplier, Technician technician, String requestDate) {
        this.code = code;
        this.supplier = supplier;
        this.technician = technician;
        this.requestDate = requestDate;
        this.items = new ArrayList<>();
        this.deliveryDate = "";
        this.status = "pending";
    }

    public boolean setStatus(String aStatus) {
        this.status = aStatus;
        return true;
    }

    public boolean setItemReturn(String orderItem) {
        // To-Do
        return true;
    }

    public boolean setDeliveryDate(String aDate) {
        this.deliveryDate = aDate;
        return true;
    }

    public int getCode() {
        return code;
    }

    public Supplier getSupplier() {
        return supplier;
    }

    public Technician getTechnician() {
        return technician;
    }

    public ArrayList<ChemicalComponent> getItems() {
        return new ArrayList<>(items);
    }

    public String getRequestDate() {
        return requestDate;
    }

    public String getDeliveryDate() {
        return deliveryDate;
    }

    public String getStatus() {
        return status;
    }

    public void addItem(ChemicalComponent item) {
        items.add(item);
    }

    public void removeItem(ChemicalComponent item) {
        items.remove(item);
    }
}
