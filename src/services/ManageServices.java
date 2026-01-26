package services;

import users.Admin;
import users.Client;
import users.Technician;
import users.User;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.Collections;
import java.util.Comparator;

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

    // Helper method to find a service by ID
    private Service findServiceById(int serviceId) {
        Iterator<Service> iterator = services.iterator();
        while (iterator.hasNext()) {
            Service service = iterator.next();
            if (service.getCode() == serviceId) {
                return service;
            }
        }
        return null;
    }

    // Helper method to find a service by ID for a specific technician
    private Service findServiceByIdAndTechnician(int serviceId, Technician aTechnician) {
        Service service = findServiceById(serviceId);
        if (service != null && service.getTechnician() != null 
                && service.getTechnician().equals(aTechnician)) {
            return service;
        }
        return null;
    }

    public boolean requestService(Client aClient, Service aService) {
        if (aService != null && aClient != null) {
            services.add(aService);
            return true;
        }
        return false;
    }

    public boolean assignTechnician(Admin aAdmin, int serviceId, Technician aTechnician) {
        Service service = findServiceById(serviceId);
        return service != null && service.setTechnician(aTechnician);
    }

    public boolean assignTechnicianToAnalysis(Technician responsibleTechnician, int serviceId, int analysisCode, Technician assignedTechnician) {
        Service service = findServiceByIdAndTechnician(serviceId, responsibleTechnician);
        if (service == null || assignedTechnician == null) {
            return false;
        }
        
        Iterator<ServiceAnalysis> analysisIterator = service.getAnalyses().iterator();
        while (analysisIterator.hasNext()) {
            ServiceAnalysis analysis = analysisIterator.next();
            if (analysis.getCode() == analysisCode) {
                return analysis.setTechnician(assignedTechnician);
            }
        }
        return false;
    }

    public boolean approveService(Technician aTechnician, int serviceId, boolean approved) {
        Service service = findServiceById(serviceId);
        if (service != null) {
            service.setStatus(approved ? "approved" : "rejected");
            return true;
        }
        return false;
    }

    public ArrayList<Service> searchService(String attribute, String value) {
        ArrayList<Service> results = new ArrayList<>();
        Iterator<Service> iterator = services.iterator();
        while (iterator.hasNext()) {
            Service service = iterator.next();
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
        Service service = findServiceById(serviceId);
        if (service != null) {
            if (approved) {
                service.setTechnician(aTechnician);
                service.setStatus("approved");
            } else {
                service.setStatus("rejected");
            }
            return true;
        }
        return false;
    }

    public boolean startExecution(Technician aTechnician, int serviceId) {
        Service service = findServiceByIdAndTechnician(serviceId, aTechnician);
        if (service != null) {
            service.setStatus("in_progress");
            return true;
        }
        return false;
    }

    public boolean finishService(Technician aTechnician, int serviceId) {
        Service service = findServiceByIdAndTechnician(serviceId, aTechnician);
        if (service != null) {
            service.setStatus("completed");
            return true;
        }
        return false;
    }

    public boolean sortServices(String criterion, boolean ascending) {
        if (criterion.equalsIgnoreCase("code")) {
            Collections.sort(services);
            if (!ascending) {
                Collections.reverse(services);
            }
        } else if (criterion.equalsIgnoreCase("value") || criterion.equalsIgnoreCase("totalvalue")) {
            Collections.sort(services, new Comparator<Service>() {
                public int compare(Service s1, Service s2) {
                    return Float.compare(s1.getTotalValue(), s2.getTotalValue());
                }
            });
            if (!ascending) {
                Collections.reverse(services);
            }
        } else {
            return false;
        }
        return true;
    }

    public boolean sortServicesByCode(boolean ascending) {
        return sortServices("code", ascending);
    }

    public boolean sortServicesByTotalValue(boolean ascending) {
        return sortServices("value", ascending);
    }

    public ArrayList<Service> listAllServices() {
        return new ArrayList<>(services);
    }

    public ArrayList<Service> listServicesByClient(Client aClient) {
        ArrayList<Service> results = new ArrayList<>();
        Iterator<Service> iterator = services.iterator();
        while (iterator.hasNext()) {
            Service service = iterator.next();
            if (service.getClient().equals(aClient)) {
                results.add(service);
            }
        }
        return results;
    }

    public ArrayList<Service> listServicesByStatus(String aStatus) {
        ArrayList<Service> results = new ArrayList<>();
        Iterator<Service> iterator = services.iterator();
        while (iterator.hasNext()) {
            Service service = iterator.next();
            if (service.getStatus().equalsIgnoreCase(aStatus)) {
                results.add(service);
            }
        }
        return results;
    }

    public ArrayList<Service> listServicesWithAnalysis(String aAnalysisCode) {
        ArrayList<Service> results = new ArrayList<>();
        Iterator<Service> iterator = services.iterator();
        while (iterator.hasNext()) {
            Service service = iterator.next();
            Iterator<ServiceAnalysis> analysisIterator = service.getAnalyses().iterator();
            while (analysisIterator.hasNext()) {
                ServiceAnalysis analysis = analysisIterator.next();
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
        Iterator<Service> iterator = services.iterator();
        while (iterator.hasNext()) {
            Service service = iterator.next();
            boolean found = false;
            Iterator<ServiceAnalysis> serviceAnalysisIterator = service.getAnalyses().iterator();
            while (serviceAnalysisIterator.hasNext()) {
                ServiceAnalysis serviceAnalysis = serviceAnalysisIterator.next();
                LabAnalysis analysis = serviceAnalysis.getAnalysis();
                Iterator<ChemicalComponent> componentIterator = analysis.getRequiredComponents().iterator();
                while (componentIterator.hasNext()) {
                    ChemicalComponent component = componentIterator.next();
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
        Iterator<Service> iterator = services.iterator();
        while (iterator.hasNext()) {
            Service service = iterator.next();
            if (String.valueOf(service.getCode()).contains(aCode)) {
                results.add(service);
            }
        }
        return results;
    }

    public ArrayList<Service> searchServicesByDescription(String keyword) {
        ArrayList<Service> results = new ArrayList<>();
        Iterator<Service> iterator = services.iterator();
        while (iterator.hasNext()) {
            Service service = iterator.next();
            if (service.getDescription().toLowerCase().contains(keyword.toLowerCase())) {
                results.add(service);
            }
        }
        return results;
    }

    public ArrayList<Service> searchServicesAdvanced(String keyword) {
        ArrayList<Service> results = new ArrayList<>();
        Iterator<Service> iterator = services.iterator();
        while (iterator.hasNext()) {
            Service service = iterator.next();
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
        Iterator<Service> iterator = userServices.iterator();
        while (iterator.hasNext()) {
            Service service = iterator.next();
            if (String.valueOf(service.getCode()).contains(keyword) ||
                    service.getDescription().toLowerCase().contains(keyword.toLowerCase()) ||
                    service.getStatus().toLowerCase().contains(keyword.toLowerCase())) {
                results.add(service);
            }
        }
        return results;
    }

    public ArrayList<Test> listTestsByAnalysis(String aAnalysisCode) {
        Iterator<Service> iterator = services.iterator();
        while (iterator.hasNext()) {
            Service service = iterator.next();
            Iterator<ServiceAnalysis> analysisIterator = service.getAnalyses().iterator();
            while (analysisIterator.hasNext()) {
                ServiceAnalysis analysis = analysisIterator.next();
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
            Iterator<Service> iterator = services.iterator();
            while (iterator.hasNext()) {
                Service service = iterator.next();
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
            Iterator<Service> iterator = services.iterator();
            while (iterator.hasNext()) {
                Service service = iterator.next();
                if ("completed".equals(service.getStatus())) {
                    completedServices.add(service);
                }
            }
            
            // Sort by finish date (most recent first)
            Collections.sort(completedServices, new Comparator<Service>() {
                public int compare(Service s1, Service s2) {
                    return s2.getFinishDate().compareTo(s1.getFinishDate());
                }
            });
            
            // Write data
            Iterator<Service> writeIterator = completedServices.iterator();
            while (writeIterator.hasNext()) {
                Service service = writeIterator.next();
                String date = service.getFinishDate().isEmpty() ? service.getRequestDate() : service.getFinishDate();
                String value = String.valueOf(service.getTotalValue());
                String client = service.getClient().getName();
                
                // Build analyses list
                StringBuilder analysesStr = new StringBuilder();
                ArrayList<ServiceAnalysis> analyses = service.getAnalyses();
                Iterator<ServiceAnalysis> analysisIterator = analyses.iterator();
                boolean first = true;
                while (analysisIterator.hasNext()) {
                    ServiceAnalysis analysis = analysisIterator.next();
                    if (!first) analysesStr.append(";");
                    analysesStr.append(analysis.getAnalysis().getName());
                    first = false;
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
