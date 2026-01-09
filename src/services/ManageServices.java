package services;

import users.Admin;
import users.Client;
import users.Technician;
import users.User;
import java.util.ArrayList;

public class ManageServices {
    private ArrayList<Service> services;
    private int nextServiceCode;

    public ManageServices() {
        this.services = new ArrayList<>();
        this.nextServiceCode = 1;
    }
    
    public int generateServiceCode() {
        return nextServiceCode++;
    }

    public boolean requestService(Client aClient, Service aService) {
        if (aService != null && aClient != null) {
            services.add(aService);
            return true;
        }
        return false;
    }

    public boolean assignTechnician(Admin aAdmin, int serviceId, Technician aTechnician) {
        for (Service service : services) {
            if (service.getCode() == serviceId) {
                return service.setTechnician(aTechnician);
            }
        }
        return false;
    }

    public boolean approveService(Technician aTechnician, int serviceId, boolean approved) {
        for (Service service : services) {
            if (service.getCode() == serviceId) {
                service.setStatus(approved ? "approved" : "rejected");
                return true;
            }
        }
        return false;
    }

    public ArrayList<Service> searchService(String attribute, String value) {
        ArrayList<Service> results = new ArrayList<>();
        for (Service service : services) {
            switch (attribute.toLowerCase()) {
                case "code":
                    if (String.valueOf(service.getCode()).contains(value)) {
                        results.add(service);
                    }
                    break;
                case "status":
                    if (service.getStatus().equalsIgnoreCase(value)) {
                        results.add(service);
                    }
                    break;
                case "description":
                    if (service.getDescription().toLowerCase().contains(value.toLowerCase())) {
                        results.add(service);
                    }
                    break;
            }
        }
        return results;
    }

    public boolean existsService(String attribute, String value) {
        return !searchService(attribute, value).isEmpty();
    }

    public ArrayList<Service> listServices(User aUser) {
        if (aUser instanceof Client) {
            return listServicesByClient((Client) aUser);
        }
        return new ArrayList<>(services);
    }

    public boolean approveService(Admin aAdmin, int serviceId, Technician aTechnician, boolean approved) {
        for (Service service : services) {
            if (service.getCode() == serviceId) {
                if (approved) {
                    service.setTechnician(aTechnician);
                    service.setStatus("approved");
                } else {
                    service.setStatus("rejected");
                }
                return true;
            }
        }
        return false;
    }

    public boolean startExecution(Technician aTechnician, int serviceId) {
        for (Service service : services) {
            if (service.getCode() == serviceId && service.getTechnician() != null
                    && service.getTechnician().equals(aTechnician)) {
                service.setStatus("in_progress");
                return true;
            }
        }
        return false;
    }

    public boolean finishService(Technician aTechnician, int serviceId) {
        for (Service service : services) {
            if (service.getCode() == serviceId && service.getTechnician() != null
                    && service.getTechnician().equals(aTechnician)) {
                service.setStatus("completed");
                return true;
            }
        }
        return false;
    }

    public boolean sortServicesByCode(boolean ascending) {
        // Bubble sort - no lambdas as per requirements
        for (int i = 0; i < services.size() - 1; i++) {
            for (int j = 0; j < services.size() - i - 1; j++) {
                int code1 = services.get(j).getCode();
                int code2 = services.get(j + 1).getCode();
                boolean shouldSwap = ascending ? code1 > code2 : code1 < code2;
                
                if (shouldSwap) {
                    Service temp = services.get(j);
                    services.set(j, services.get(j + 1));
                    services.set(j + 1, temp);
                }
            }
        }
        return true;
    }

    public boolean sortServicesByTotalValue(boolean ascending) {
        // Bubble sort - no lambdas as per requirements
        for (int i = 0; i < services.size() - 1; i++) {
            for (int j = 0; j < services.size() - i - 1; j++) {
                float value1 = services.get(j).getTotalValue();
                float value2 = services.get(j + 1).getTotalValue();
                boolean shouldSwap = ascending ? value1 > value2 : value1 < value2;
                
                if (shouldSwap) {
                    Service temp = services.get(j);
                    services.set(j, services.get(j + 1));
                    services.set(j + 1, temp);
                }
            }
        }
        return true;
    }

    public ArrayList<Service> listAllServices() {
        return new ArrayList<>(services);
    }

    public ArrayList<Service> listServicesByClient(Client aClient) {
        ArrayList<Service> results = new ArrayList<>();
        for (Service service : services) {
            if (service.getClient().equals(aClient)) {
                results.add(service);
            }
        }
        return results;
    }

    public ArrayList<Service> listServicesByStatus(String aStatus) {
        ArrayList<Service> results = new ArrayList<>();
        for (Service service : services) {
            if (service.getStatus().equalsIgnoreCase(aStatus)) {
                results.add(service);
            }
        }
        return results;
    }

    public ArrayList<Service> listServicesWithAnalysis(String aAnalysisCode) {
        ArrayList<Service> results = new ArrayList<>();
        for (Service service : services) {
            for (ServiceAnalysis analysis : service.getAnalyses()) {
                if (String.valueOf(analysis.getCode()).equals(aAnalysisCode)) {
                    results.add(service);
                    break;
                }
            }
        }
        return results;
    }

    public ArrayList<Service> listServicesWithChemicalComponent(String aComponentCode) {
        ArrayList<Service> results = new ArrayList<>();
        for (Service service : services) {
            boolean found = false;
            for (ServiceAnalysis serviceAnalysis : service.getAnalyses()) {
                LabAnalysis analysis = serviceAnalysis.getAnalysis();
                for (ChemicalComponent component : analysis.getRequiredComponents()) {
                    if (String.valueOf(component.getCode()).equals(aComponentCode)) {
                        results.add(service);
                        found = true;
                        break;
                    }
                }
                if (found) break;
            }
        }
        return results;
    }

    public ArrayList<Service> searchServicesByCode(String aCode) {
        ArrayList<Service> results = new ArrayList<>();
        for (Service service : services) {
            if (String.valueOf(service.getCode()).contains(aCode)) {
                results.add(service);
            }
        }
        return results;
    }

    public ArrayList<Service> searchServicesByDescription(String keyword) {
        ArrayList<Service> results = new ArrayList<>();
        for (Service service : services) {
            if (service.getDescription().toLowerCase().contains(keyword.toLowerCase())) {
                results.add(service);
            }
        }
        return results;
    }

    public ArrayList<Service> searchServicesAdvanced(String keyword) {
        ArrayList<Service> results = new ArrayList<>();
        for (Service service : services) {
            if (String.valueOf(service.getCode()).contains(keyword) ||
                    service.getDescription().toLowerCase().contains(keyword.toLowerCase()) ||
                    service.getStatus().toLowerCase().contains(keyword.toLowerCase())) {
                results.add(service);
            }
        }
        return results;
    }

    public ArrayList<Service> searchServicesForUser(User aUser, String keyword) {
        ArrayList<Service> userServices = listServices(aUser);
        ArrayList<Service> results = new ArrayList<>();
        for (Service service : userServices) {
            if (String.valueOf(service.getCode()).contains(keyword) ||
                    service.getDescription().toLowerCase().contains(keyword.toLowerCase()) ||
                    service.getStatus().toLowerCase().contains(keyword.toLowerCase())) {
                results.add(service);
            }
        }
        return results;
    }

    public ArrayList<Test> listTestsByAnalysis(String aAnalysisCode) {
        for (Service service : services) {
            for (ServiceAnalysis analysis : service.getAnalyses()) {
                if (String.valueOf(analysis.getCode()).equals(aAnalysisCode)) {
                    return analysis.getTests();
                }
            }
        }
        return new ArrayList<>();
    }

    public void loadServices(ArrayList<Service> loadedServices) {
        if (loadedServices != null) {
            this.services = new ArrayList<>(loadedServices);
            // Update nextServiceCode to be higher than any loaded code
            for (Service service : services) {
                if (service.getCode() >= nextServiceCode) {
                    nextServiceCode = service.getCode() + 1;
                }
            }
        }
    }

    public boolean exportServicesToCSV(String filename) {
        try {
            java.io.FileWriter writer = new java.io.FileWriter(filename);
            java.io.BufferedWriter bufferedWriter = new java.io.BufferedWriter(writer);
            
            // Header
            bufferedWriter.write("Data,Valor,Cliente,Analises");
            bufferedWriter.newLine();
            
            // Sort services by date (most recent first)
            ArrayList<Service> completedServices = new ArrayList<>();
            for (Service service : services) {
                if ("completed".equals(service.getStatus())) {
                    completedServices.add(service);
                }
            }
            
            // Sort by finish date (most recent first)
            for (int i = 0; i < completedServices.size() - 1; i++) {
                for (int j = i + 1; j < completedServices.size(); j++) {
                    String date1 = completedServices.get(i).getFinishDate();
                    String date2 = completedServices.get(j).getFinishDate();
                    if (date1.compareTo(date2) < 0) {
                        Service temp = completedServices.get(i);
                        completedServices.set(i, completedServices.get(j));
                        completedServices.set(j, temp);
                    }
                }
            }
            
            // Write data
            for (Service service : completedServices) {
                String date = service.getFinishDate().isEmpty() ? service.getRequestDate() : service.getFinishDate();
                String value = String.valueOf(service.getTotalValue());
                String client = service.getClient().getName();
                
                // Build analyses list
                StringBuilder analysesStr = new StringBuilder();
                ArrayList<ServiceAnalysis> analyses = service.getAnalyses();
                for (int i = 0; i < analyses.size(); i++) {
                    if (i > 0) analysesStr.append(";");
                    analysesStr.append(analyses.get(i).getAnalysis().getName());
                }
                
                bufferedWriter.write(date + "," + value + "," + client + "," + analysesStr.toString());
                bufferedWriter.newLine();
            }
            
            bufferedWriter.close();
            return true;
        } catch (java.io.IOException e) {
            System.err.println("Error exporting to CSV: " + e.getMessage());
            return false;
        }
    }
}
