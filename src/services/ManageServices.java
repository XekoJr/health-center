package services;

import users.Admin;
import users.Client;
import users.Technician;
import users.User;
import java.util.ArrayList;
import java.util.Comparator;

public class ManageServices {
    private ArrayList<Service> services;

    public ManageServices() {
        this.services = new ArrayList<>();
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
        if (ascending) {
            services.sort(Comparator.comparing(Service::getCode));
        } else {
            services.sort(Comparator.comparing(Service::getCode).reversed());
        }
        return true;
    }

    public boolean sortServicesByTotalValue(boolean ascending) {
        if (ascending) {
            services.sort(Comparator.comparing(Service::getTotalValue));
        } else {
            services.sort(Comparator.comparing(Service::getTotalValue).reversed());
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
        // Implementation depends on how chemical components are stored
        // This is a placeholder
        return new ArrayList<>();
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
}
