package services;

import users.Client;
import users.Technician;
import java.util.ArrayList;
import java.util.Iterator;
import java.io.Serializable;

public class Service implements Serializable, Comparable<Service> {
    private static final long serialVersionUID = 1L;
    private int code;
    private Client client;
    private Technician technician;
    private String status;
    private String description;
    private ArrayList<ServiceAnalysis> analyses;
    private float totalValue;
    private String requestDate;
    private String finishDate;

    public Service(int code, Client client, String description, String requestDate) {
        this.code = code;
        this.client = client;
        this.description = description;
        this.requestDate = requestDate;
        this.analyses = new ArrayList<>();
        this.status = "pending";
        this.totalValue = 0.0f;
        this.finishDate = "";
    }

    public boolean setTechnician(Technician aTechnician) {
        this.technician = aTechnician;
        return true;
    }

    public boolean setStatus(String aStatus) {
        this.status = aStatus;
        return true;
    }

    public Client getClient() {
        return client;
    }

    public Technician getTechnician() {
        return technician;
    }

    public String getStatus() {
        return status;
    }

    public boolean addAnalysis(ServiceAnalysis aAnalysis) {
        if (aAnalysis != null) {
            analyses.add(aAnalysis);
            calculateTotalValue();
            return true;
        }
        return false;
    }

    public boolean removeAnalysis(String aCode) {
        Iterator<ServiceAnalysis> iterator = analyses.iterator();
        while (iterator.hasNext()) {
            ServiceAnalysis analysis = iterator.next();
            if (String.valueOf(analysis.getCode()).equals(aCode)) {
                iterator.remove();
                calculateTotalValue();
                return true;
            }
        }
        return false;
    }

    public float calculateTotalValue() {
        float sum = 0.0f;
        Iterator<ServiceAnalysis> iterator = analyses.iterator();
        while (iterator.hasNext()) {
            ServiceAnalysis serviceAnalysis = iterator.next();
            sum += serviceAnalysis.getAnalysis().getValue();
        }
        this.totalValue = sum;
        return totalValue;
    }

    public int getCode() {
        return code;
    }

    public String getDescription() {
        return description;
    }

    public ArrayList<ServiceAnalysis> getAnalyses() {
        return new ArrayList<>(analyses);
    }

    public float getTotalValue() {
        return totalValue;
    }

    public void setTotalValue(float totalValue) {
        this.totalValue = totalValue;
    }

    public String getRequestDate() {
        return requestDate;
    }

    public String getFinishDate() {
        return finishDate;
    }

    public void setFinishDate(String finishDate) {
        this.finishDate = finishDate;
    }

    public int compareTo(Service other) {
        return Integer.compare(this.code, other.code);
    }

    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("-----------------------------------------\n");
        sb.append("Codigo: ").append(code).append("\n");
        sb.append("Cliente: ").append(client.getName()).append("\n");
        sb.append("Descricao: ").append(description).append("\n");
        sb.append("Estado: ").append(status).append("\n");
        sb.append("Data pedido: ").append(requestDate).append("\n");
        
        if (technician != null) {
            sb.append("Tecnico: ").append(technician.getName()).append("\n");
        }
        
        if (!finishDate.isEmpty()) {
            sb.append("Data conclusao: ").append(finishDate).append("\n");
        }
        
        sb.append("Valor total: ").append(totalValue).append(" euros\n");
        sb.append("Analises: ").append(analyses.size());
        return sb.toString();
    }
}
