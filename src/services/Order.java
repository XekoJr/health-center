package services;

import users.Technician;
import java.util.ArrayList;
import java.util.Iterator;
import java.io.Serializable;

public class Order implements Serializable, Comparable<Order> {
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

    public int compareTo(Order other) {
        return Integer.compare(this.code, other.code);
    }

    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("-----------------------------------------\n");
        sb.append("Codigo: ").append(code).append("\n");
        sb.append("Fornecedor: ").append(supplier.getName()).append("\n");
        sb.append("Tecnico: ").append(technician.getName()).append("\n");
        sb.append("Data pedido: ").append(requestDate).append("\n");
        sb.append("Data entrega: ").append(deliveryDate.isEmpty() ? "Pendente" : deliveryDate).append("\n");
        sb.append("Estado: ").append(status).append("\n");
        sb.append("Componentes encomendados: ").append(items.size()).append("\n");
        
        if (!items.isEmpty()) {
            int index = 1;
            Iterator<ChemicalComponent> itemIterator = items.iterator();
            while (itemIterator.hasNext()) {
                ChemicalComponent item = itemIterator.next();
                sb.append("  ").append(index).append(". ")
                  .append(item.getName())
                  .append(" - Qtd: ").append(item.getStockQty())
                  .append("\n");
                index++;
            }
        }
        
        return sb.toString();
    }
}
