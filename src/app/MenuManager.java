package app;

import users.*;
import services.*;
import util.*;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.Scanner;

public class MenuManager {
    private ApplicationManager appManager;
    private Scanner scanner;
    private LogManager logManager;
    private SystemInfo systemInfo;

    public MenuManager(ApplicationManager appManager, LogManager logManager, SystemInfo systemInfo) {
        this.appManager = appManager;
        this.logManager = logManager;
        this.systemInfo = systemInfo;
        this.scanner = new Scanner(System.in);
    }

    // ==================== MAIN MENU ====================

    public void showMainMenu() {
        while (true) {
            clearScreen();
            System.out.println("==========================================");
            System.out.println("     SISTEMA DE GESTAO LABORATORIAL      ");
            System.out.println("==========================================");
            System.out.println();

            // Check if no users exist - force admin creation
            ArrayList<User> users = appManager.getManageUsers().listUsers();
            if (users.isEmpty()) {
                System.out.println("AVISO: Nenhum utilizador no sistema!");
                System.out.println("       Deve criar um administrador.");
                System.out.println();
                pause();
                handleRegister();
                
                // Check if user was created successfully
                if (appManager.getManageUsers().listUsers().isEmpty()) {
                    // User cancelled registration or it failed, exit
                    return;
                }
                // Continue to show normal menu after registration
                continue;
            } else {
                System.out.println("1. Login");
                System.out.println("2. Registar novo utilizador");
                System.out.println("0. Sair");
                System.out.println();
                System.out.print("Escolha uma opcao: ");

                int choice = readInt();

                switch (choice) {
                    case 1:
                        handleLogin();
                        break;
                    case 2:
                        handleRegister();
                        break;
                    case 0:
                        return;
                    default:
                        showError("Opcao invalida!");
                        pause();
                }
            }
        }
    }

    // ==================== LOGIN & REGISTER ====================

    private void handleLogin() {
        clearScreen();
        System.out.println("==========================================");
        System.out.println("                  LOGIN                   ");
        System.out.println("==========================================");
        System.out.println();

        System.out.print("Username: ");
        String username = scanner.nextLine().trim();

        System.out.print("Password: ");
        String password = scanner.nextLine().trim();

        User user = appManager.getManageUsers().login(username, password);

        if (user == null) {
            showError("Credenciais invalidas!");
            pause();
            return;
        }

        if (!user.getReviewed()) {
            showError("Conta pendente de aprovacao por um administrador.");
            pause();
            return;
        }

        if (!"approved".equals(user.getStatus())) {
            showError("Conta nao aprovada. Estado: " + user.getStatus());
            pause();
            return;
        }

        // Login successful
        appManager.getSession().setCurrentUser(user);
        systemInfo.setLastUsername(username);
        logManager.log(username, "Login realizado");

        clearScreen();
        System.out.println("==========================================");
        System.out.println("                BEM-VINDO                 ");
        System.out.println("==========================================");
        System.out.println();
        System.out.println("Bem-vindo " + user.getName() + "!");
        System.out.println();
        pause();

        // Show appropriate menu based on user type
        if (user instanceof Admin) {
            showAdminMenu();
        } else if (user instanceof Technician) {
            showTechnicianMenu();
        } else if (user instanceof Client) {
            showClientMenu();
        }

        // Logout
        String name = user.getName();
        appManager.getSession().logout();
        logManager.log(username, "Logout realizado");

        clearScreen();
        System.out.println("Adeus " + name + "!");
        pause();
    }

    private void handleRegister() {
        clearScreen();
        System.out.println("==========================================");
        System.out.println("          REGISTO DE UTILIZADOR           ");
        System.out.println("==========================================");
        System.out.println();

        // Check if this is the first user (must be admin)
        ArrayList<User> users = appManager.getManageUsers().listUsers();
        boolean isFirstUser = users.isEmpty();

        if (isFirstUser) {
            System.out.println("AVISO: Nao existem utilizadores no sistema.");
            System.out.println("  O primeiro utilizador sera criado como ADMINISTRADOR.");
            System.out.println();
        }

        System.out.print("Username: ");
        String username = scanner.nextLine().trim();

        if (username.isEmpty()) {
            showError("Username nao pode estar vazio!");
            pause();
            return;
        }

        System.out.print("Email: ");
        String email = scanner.nextLine().trim();

        if (email.isEmpty()) {
            showError("Email nao pode estar vazio!");
            pause();
            return;
        }

        System.out.print("Password: ");
        String password = scanner.nextLine().trim();

        if (password.isEmpty()) {
            showError("Password nao pode estar vazia!");
            pause();
            return;
        }

        System.out.print("Nome completo: ");
        String name = scanner.nextLine().trim();

        if (name.isEmpty()) {
            showError("Nome nao pode estar vazio!");
            pause();
            return;
        }

        String type;
        User newUser;

        if (isFirstUser) {
            type = "admin";
            newUser = new Admin(username, email, password, name, "approved", type, true);
        } else {
            System.out.println();
            System.out.println("Tipo de utilizador:");
            System.out.println("1. Cliente");
            System.out.println("2. Tecnico");
            System.out.print("Escolha: ");

            int typeChoice = readInt();

            if (typeChoice == 1) {
                type = "client";
                System.out.print("NIF: ");
                String nif = scanner.nextLine().trim();
                System.out.print("Morada: ");
                String address = scanner.nextLine().trim();
                System.out.print("Telefone: ");
                String phone = scanner.nextLine().trim();

                newUser = new Client(username, email, password, name, "pending", type, false, nif, address, phone);
            } else if (typeChoice == 2) {
                type = "technician";
                System.out.print("NIF: ");
                String nif = scanner.nextLine().trim();
                System.out.print("Morada: ");
                String address = scanner.nextLine().trim();
                System.out.print("Telefone: ");
                String phone = scanner.nextLine().trim();

                newUser = new Technician(username, email, password, name, "pending", type, false, nif, address, phone);
            } else {
                showError("Opcao invalida!");
                pause();
                return;
            }
        }

        if (appManager.getManageUsers().register(newUser)) {
            showSuccess("Utilizador registado com sucesso!");
            if (!isFirstUser) {
                System.out.println("A sua conta ficara pendente ate ser aprovada por um administrador.");
            }
            logManager.log("SYSTEM", "Novo registo: " + username + " (" + type + ")");
        } else {
            showError("Erro ao registar utilizador. Username ou email ja existem.");
        }

        pause();
    }

    // ==================== ADMIN MENU ====================

    private void showAdminMenu() {
        while (appManager.getSession().isLoggedIn()) {
            clearScreen();
            User currentUser = appManager.getSession().getCurrentUser();

            System.out.println("==========================================");
            System.out.println("           MENU ADMINISTRADOR             ");
            System.out.println("==========================================");
            System.out.println("Utilizador: " + currentUser.getName());
            System.out.println();
            System.out.println("1. Gerir Utilizadores");
            System.out.println("2. Gerir Servicos");
            System.out.println("3. Gerir Analises");
            System.out.println("4. Gerir Componentes Quimicos");
            System.out.println("5. Gerir Fornecedores");
            System.out.println("6. Gerir Areas Medicas");
            System.out.println("7. Gerir Encomendas");
            System.out.println("8. Consultar log do sistema");
            System.out.println("9. Editar perfil");
            System.out.println("0. Logout");
            System.out.println();
            System.out.print("Escolha uma opcao: ");

            int choice = readInt();

            switch (choice) {
                case 1:
                    manageUsers();
                    break;
                case 2:
                    manageServices();
                    break;
                case 3:
                    manageAnalyses();
                    break;
                case 4:
                    manageComponents();
                    break;
                case 5:
                    manageSuppliers();
                    break;
                case 6:
                    manageAreas();
                    break;
                case 7:
                    manageOrders();
                    break;
                case 8:
                    viewSystemLog();
                    break;
                case 9:
                    editMyProfile();
                    break;
                case 0:
                    return;
                default:
                    showError("Opcao invalida!");
                    pause();
            }
        }
    }

    private void manageUsers() {
        while (true) {
            clearScreen();
            System.out.println("==========================================");
            System.out.println("            GERIR UTILIZADORES            ");
            System.out.println("==========================================");
            System.out.println();
            System.out.println("1. Aprovar utilizadores pendentes");
            System.out.println("2. Listar utilizadores");
            System.out.println("3. Pesquisar utilizadores");
            System.out.println("0. Voltar");
            System.out.print("Escolha: ");

            int choice = readInt();

            switch (choice) {
                case 1:
                    approveUsers();
                    break;
                case 2:
                    listAllUsers();
                    break;
                case 3:
                    searchUsers();
                    break;
                case 0:
                    return;
                default:
                    showError("Opcao invalida!");
                    pause();
            }
        }
    }

    private void approveUsers() {
        clearScreen();
        System.out.println("==========================================");
        System.out.println("       APROVAR UTILIZADORES PENDENTES     ");
        System.out.println("==========================================");
        System.out.println();

        ArrayList<User> pendingUsers = appManager.getManageUsers().searchUser("status", "pending");

        if (pendingUsers.isEmpty()) {
            System.out.println("Nao existem utilizadores pendentes.");
            pause();
            return;
        }

        System.out.println("Utilizadores pendentes de aprovacao:");
        System.out.println();

        int index = 1;
        Iterator<User> iterator = pendingUsers.iterator();
        while (iterator.hasNext()) {
            User user = iterator.next();
            System.out
                    .println(index + ". " + user.getUsername() + " - " + user.getName() + " (" + user.getType() + ")");
            System.out.println("   Email: " + user.getEmail());
            if (user instanceof Client) {
                Client c = (Client) user;
                System.out.println("   NIF: " + c.getNif() + " | Tel: " + c.getPhone());
            } else if (user instanceof Technician) {
                Technician t = (Technician) user;
                System.out.println("   NIF: " + t.getNif() + " | Tel: " + t.getPhone());
            }
            System.out.println();
            index++;
        }

        System.out.print("Escolha o numero do utilizador (0 para cancelar): ");
        int choice = readInt();

        if (choice < 1 || choice > pendingUsers.size()) {
            return;
        }

        User selectedUser = pendingUsers.get(choice - 1);

        System.out.println();
        System.out.println("1. Aprovar");
        System.out.println("2. Rejeitar");
        System.out.print("Escolha: ");

        int action = readInt();

        if (action == 1) {
            if (appManager.getManageUsers().aproveUser(selectedUser, true)) {
                showSuccess("Utilizador aprovado com sucesso!");
                logManager.log(appManager.getSession().getCurrentUser().getUsername(),
                        "Aprovou utilizador: " + selectedUser.getUsername());
            } else {
                showError("Erro ao aprovar utilizador.");
            }
        } else if (action == 2) {
            if (appManager.getManageUsers().aproveUser(selectedUser, false)) {
                showSuccess("Utilizador rejeitado.");
                logManager.log(appManager.getSession().getCurrentUser().getUsername(),
                        "Rejeitou utilizador: " + selectedUser.getUsername());
            } else {
                showError("Erro ao rejeitar utilizador.");
            }
        }

        pause();
    }

    private void listAllUsers() {
        clearScreen();
        System.out.println("==========================================");
        System.out.println("            LISTAR UTILIZADORES           ");
        System.out.println("==========================================");
        System.out.println();

        System.out.println("1. Todos os utilizadores");
        System.out.println("2. Apenas Administradores");
        System.out.println("3. Apenas Tecnicos");
        System.out.println("4. Apenas Clientes");
        System.out.print("Escolha: ");

        int choice = readInt();
        ArrayList<User> users;

        switch (choice) {
            case 1:
                users = appManager.getManageUsers().listUsers();
                break;
            case 2:
                users = appManager.getManageUsers().listUsersByType("admin");
                break;
            case 3:
                users = appManager.getManageUsers().listUsersByType("technician");
                break;
            case 4:
                users = appManager.getManageUsers().listUsersByType("client");
                break;
            default:
                showError("Opcao invalida!");
                pause();
                return;
        }

        clearScreen();
        System.out.println("Total de utilizadores: " + users.size());
        System.out.println();

        appManager.getManageUsers().sortUsersByName(true);

        Iterator<User> userIterator = users.iterator();
        while (userIterator.hasNext()) {
            User user = userIterator.next();
            System.out.println(user.toString());
        }

        System.out.println("-----------------------------------------");
        pause();
    }

    private void searchUsers() {
        clearScreen();
        System.out.println("==========================================");
        System.out.println("          PESQUISAR UTILIZADORES          ");
        System.out.println("==========================================");
        System.out.println();

        System.out.print("Termo de pesquisa (username ou nome, vazio para listar todos): ");
        String term = scanner.nextLine().trim();

        ArrayList<User> results;
        if (term.isEmpty()) {
            results = appManager.getManageUsers().listUsers();
        } else {
            results = appManager.getManageUsers().searchUsersAdvanced(term);
        }

        clearScreen();
        System.out.println("Resultados encontrados: " + results.size());
        System.out.println();

        Iterator<User> resultIterator = results.iterator();
        while (resultIterator.hasNext()) {
            User user = resultIterator.next();
            System.out.println(user.toString());
        }

        if (!results.isEmpty()) {
            System.out.println("-----------------------------------------");
        }

        pause();
    }

    private void manageServices() {
        while (true) {
            clearScreen();
            System.out.println("==========================================");
            System.out.println("              GERIR SERVICOS              ");
            System.out.println("==========================================");
            System.out.println();

            System.out.println("1. Aprovar pedidos de servico");
            System.out.println("2. Associar tecnico a servico");
            System.out.println("3. Pesquisar/Listar servicos");
            System.out.println("4. Listar servicos por estado");
            System.out.println("5. Listar servicos com analise especifica");
            System.out.println("6. Exportar servicos para CSV");
            System.out.println("0. Voltar");
            System.out.print("Escolha: ");

            int choice = readInt();

            switch (choice) {
                case 1:
                    adminApproveRejectServices();
                    break;
                case 2:
                    associateTechnicianToServices();
                    break;
                case 3:
                    searchServices();
                    break;
                case 4:
                    listServicesByStatus();
                    break;
                case 5:
                    listServicesWithSpecificAnalysis();
                    break;
                case 6:
                    exportServicesToCSV();
                    break;
                case 0:
                    return;
                default:
                    showError("Opcao invalida!");
                    pause();
            }
        }
    }

    private void searchServices() {
        clearScreen();
        System.out.println("==========================================");
        System.out.println("         PESQUISAR/LISTAR SERVICOS        ");
        System.out.println("==========================================");
        System.out.println();

        System.out.print("Termo de pesquisa (codigo ou descricao, vazio para listar todos): ");
        String term = scanner.nextLine().trim();

        ArrayList<Service> results;
        if (term.isEmpty()) {
            System.out.println("\nOrdenar servicos por:");
            System.out.println("1 - Codigo");
            System.out.println("2 - Valor Total");
            System.out.print("Opcao: ");
            String sortOption = scanner.nextLine().trim();
            
            if (sortOption.equals("2")) {
                appManager.getManageServices().sortServicesByTotalValue(true);
            } else {
                appManager.getManageServices().sortServicesByCode(true);
            }
            results = appManager.getManageServices().listAllServices();
        } else {
            results = appManager.getManageServices().searchServicesAdvanced(term);
        }

        clearScreen();
        System.out.println("Resultados encontrados: " + results.size());
        System.out.println();

        Iterator<Service> serviceIterator = results.iterator();
        while (serviceIterator.hasNext()) {
            Service service = serviceIterator.next();
            displayService(service);
        }

        pause();
    }

    private void listServicesByStatus() {
        clearScreen();
        System.out.println("==========================================");
        System.out.println("        LISTAR SERVICOS POR ESTADO        ");
        System.out.println("==========================================");
        System.out.println();

        System.out.println("Escolha o estado:");
        System.out.println("1. Pendente (pending)");
        System.out.println("2. Aprovado por tecnico (technician_approved)");
        System.out.println("3. Aprovado (approved)");
        System.out.println("4. Em execucao (in_progress)");
        System.out.println("5. Concluido (finished)");
        System.out.println("6. Rejeitado (rejected)");
        System.out.print("Escolha: ");

        int choice = readInt();
        String status;

        switch (choice) {
            case 1:
                status = "pending";
                break;
            case 2:
                status = "technician_approved";
                break;
            case 3:
                status = "approved";
                break;
            case 4:
                status = "in_progress";
                break;
            case 5:
                status = "finished";
                break;
            case 6:
                status = "rejected";
                break;
            default:
                showError("Opcao invalida!");
                pause();
                return;
        }

        ArrayList<Service> results = appManager.getManageServices().listServicesByStatus(status);

        clearScreen();
        System.out.println("Servicos com estado '" + status + "': " + results.size());
        System.out.println();

        Iterator<Service> serviceIterator = results.iterator();
        while (serviceIterator.hasNext()) {
            Service service = serviceIterator.next();
            displayService(service);
        }

        pause();
    }

    private void adminApproveRejectServices() {
        clearScreen();
        System.out.println("==========================================");
        System.out.println("      APROVAR/REJEITAR PEDIDOS SERVICO    ");
        System.out.println("==========================================");
        System.out.println();

        ArrayList<Service> pendingServices = appManager.getManageServices().listServicesByStatus("pending");

        if (pendingServices.isEmpty()) {
            System.out.println("Nao existem pedidos de servico pendentes.");
            pause();
            return;
        }

        System.out.println("Pedidos de servico pendentes:");
        System.out.println();

        int index = 1;
        Iterator<Service> serviceIterator = pendingServices.iterator();
        while (serviceIterator.hasNext()) {
            Service service = serviceIterator.next();
            System.out.println(index + ". Codigo: " + service.getCode());
            System.out.println("   Cliente: " + service.getClient().getName());
            System.out.println("   Descricao: " + service.getDescription());
            System.out.println("   Data pedido: " + service.getRequestDate());
            System.out.println();
            index++;
        }

        System.out.print("Escolha o servico (0 para cancelar): ");
        int choice = readInt();

        if (choice < 1 || choice > pendingServices.size()) {
            return;
        }

        Service selectedService = pendingServices.get(choice - 1);

        System.out.println();
        System.out.println("1. Aprovar");
        System.out.println("2. Rejeitar");
        System.out.print("Escolha: ");

        int action = readInt();

        if (action == 1) {
            selectedService.setStatus("technician_approved");
            showSuccess("Pedido de servico aprovado!");
            System.out.println("O servico aguarda agora a atribuicao de um tecnico.");
            logManager.log(appManager.getSession().getCurrentUser().getUsername(),
                    "Aprovou pedido de servico " + selectedService.getCode());
        } else if (action == 2) {
            selectedService.setStatus("rejected");
            showSuccess("Pedido de servico rejeitado.");
            logManager.log(appManager.getSession().getCurrentUser().getUsername(),
                    "Rejeitou pedido de servico " + selectedService.getCode());
        }

        pause();
    }

    private void associateTechnicianToServices() {
        clearScreen();
        System.out.println("==========================================");
        System.out.println("         ASSOCIAR TECNICO A SERVICO       ");
        System.out.println("==========================================");
        System.out.println();

        ArrayList<Service> pendingServices = appManager.getManageServices().listServicesByStatus("technician_approved");

        if (pendingServices.isEmpty()) {
            System.out.println("Nao existem servicos aprovados por tecnicos aguardando atribuicao.");
            pause();
            return;
        }

        System.out.println("Servicos aprovados por tecnicos:");
        System.out.println();

        int index = 1;
        Iterator<Service> associateIterator = pendingServices.iterator();
        while (associateIterator.hasNext()) {
            Service service = associateIterator.next();
            System.out.println(index + ". Codigo: " + service.getCode());
            System.out.println("   Cliente: " + service.getClient().getName());
            System.out.println("   Descricao: " + service.getDescription());
            System.out.println("   Data pedido: " + service.getRequestDate());
            System.out.println();
            index++;
        }

        System.out.print("Escolha o servico (0 para cancelar): ");
        int choice = readInt();

        if (choice < 1 || choice > pendingServices.size()) {
            return;
        }

        Service selectedService = pendingServices.get(choice - 1);

        System.out.println();
        System.out.println("Associar tecnico responsavel:");
        System.out.print("Continuar? (1-Sim, 0-Nao): ");

        int action = readInt();

        if (action != 1) {
            return;
        }

        ArrayList<User> technicians = appManager.getManageUsers().listUsersByType("technician");

        if (technicians.isEmpty()) {
            showError("Nao existem tecnicos para associar!");
            pause();
            return;
        }

        System.out.println();
        System.out.println("Escolha o tecnico responsavel:");

        int techIndex = 1;
        Iterator<User> techIterator = technicians.iterator();
        while (techIterator.hasNext()) {
            User tech = techIterator.next();
            System.out.println(techIndex + ". " + tech.getName());
            techIndex++;
        }

        System.out.print("Escolha: ");
        int techChoice = readInt();

        if (techChoice < 1 || techChoice > technicians.size()) {
            showError("Escolha invalida!");
            pause();
            return;
        }

        Technician selectedTech = (Technician) technicians.get(techChoice - 1);

        if (appManager.getManageServices().approveService((Admin) appManager.getSession().getCurrentUser(),
                selectedService.getCode(), selectedTech, true)) {
            showSuccess("Tecnico associado com sucesso!");
            System.out.println("O servico esta agora pronto para execucao.");
            logManager.log(appManager.getSession().getCurrentUser().getUsername(),
                    "Associou tecnico ao servico " + selectedService.getCode());
        } else {
            showError("Erro ao associar tecnico.");
        }

        pause();
    }

    // ==================== TECHNICIAN MENU ====================

    private void showTechnicianMenu() {
        while (appManager.getSession().isLoggedIn()) {
            clearScreen();
            User currentUser = appManager.getSession().getCurrentUser();

            System.out.println("==========================================");
            System.out.println("              MENU TECNICO                ");
            System.out.println("==========================================");
            System.out.println("Utilizador: " + currentUser.getName());
            System.out.println();
            System.out.println("1.  Aprovar/Rejeitar pedidos de servico");
            System.out.println("2.  Listar meus servicos");
            System.out.println("3.  Atribuir tecnicos as analises");
            System.out.println("4.  Executar analises atribuidas");
            System.out.println("5.  Iniciar execucao de servico");
            System.out.println("6.  Finalizar servico");
            System.out.println("7.  Gestao de Analises");
            System.out.println("8.  Gestao de Componentes Quimicos");
            System.out.println("9.  Gestao de Fornecedores");
            System.out.println("10. Gestao de Areas Medicas");
            System.out.println("11. Gestao de Encomendas");
            System.out.println("12. Gestao de Categorias");
            System.out.println("13. Editar perfil");
            System.out.println("0.  Logout");
            System.out.println();
            System.out.print("Escolha uma opcao: ");

            int choice = readInt();

            switch (choice) {
                case 1:
                    technicianApproveRejectServices();
                    break;
                case 2:
                    listMyServices();
                    break;
                case 3:
                    assignTechniciansToAnalyses();
                    break;
                case 4:
                    executeMyAnalyses();
                    break;
                case 5:
                    startServiceExecution();
                    break;
                case 6:
                    finishService();
                    break;
                case 7:
                    manageAnalyses();
                    break;
                case 8:
                    manageComponents();
                    break;
                case 9:
                    manageSuppliers();
                    break;
                case 10:
                    manageAreas();
                    break;
                case 11:
                    manageOrders();
                    break;
                case 12:
                    manageCategories();
                    break;
                case 13:
                    editMyProfile();
                    break;
                case 0:
                    return;
                default:
                    showError("Opcao invalida!");
                    pause();
            }
        }
    }

    private void technicianApproveRejectServices() {
        clearScreen();
        System.out.println("==========================================");
        System.out.println("      APROVAR/REJEITAR PEDIDOS SERVICO    ");
        System.out.println("==========================================");
        System.out.println();

        ArrayList<Service> pendingServices = appManager.getManageServices().listServicesByStatus("pending");

        if (pendingServices.isEmpty()) {
            System.out.println("Nao existem pedidos de servico pendentes.");
            pause();
            return;
        }

        System.out.println("Pedidos de servico pendentes:");
        System.out.println();

        int index = 1;
        Iterator<Service> serviceIterator = pendingServices.iterator();
        while (serviceIterator.hasNext()) {
            Service service = serviceIterator.next();
            System.out.println(index + ". Codigo: " + service.getCode());
            System.out.println("   Cliente: " + service.getClient().getName());
            System.out.println("   Descricao: " + service.getDescription());
            System.out.println("   Data pedido: " + service.getRequestDate());
            System.out.println();
            index++;
        }

        System.out.print("Escolha o servico (0 para cancelar): ");
        int choice = readInt();

        if (choice < 1 || choice > pendingServices.size()) {
            return;
        }

        Service selectedService = pendingServices.get(choice - 1);

        System.out.println();
        System.out.println("1. Aprovar");
        System.out.println("2. Rejeitar");
        System.out.print("Escolha: ");

        int action = readInt();

        Technician currentTech = (Technician) appManager.getSession().getCurrentUser();

        if (action == 1) {
            if (appManager.getManageServices().approveService(currentTech, selectedService.getCode(), true)) {
                selectedService.setStatus("technician_approved");
                showSuccess("Pedido de servico aprovado!");
                System.out.println("O servico aguarda agora a atribuicao de um tecnico pelo administrador.");
                logManager.log(currentTech.getUsername(), "Aprovou pedido de servico " + selectedService.getCode());
            } else {
                showError("Erro ao aprovar pedido de servico.");
            }
        } else if (action == 2) {
            if (appManager.getManageServices().approveService(currentTech, selectedService.getCode(), false)) {
                showSuccess("Pedido de servico rejeitado.");
                logManager.log(currentTech.getUsername(), "Rejeitou pedido de servico " + selectedService.getCode());
            } else {
                showError("Erro ao rejeitar pedido de servico.");
            }
        }

        pause();
    }

    private void assignTechniciansToAnalyses() {
        clearScreen();
        System.out.println("==========================================");
        System.out.println("   ATRIBUIR TECNICOS AS ANALISES [R23]   ");
        System.out.println("==========================================");
        System.out.println();

        Technician currentTech = (Technician) appManager.getSession().getCurrentUser();
        ArrayList<Service> allServices = appManager.getManageServices().listAllServices();
        ArrayList<Service> myServices = new ArrayList<>();

        // Filtrar apenas serviços onde o técnico atual é o responsável
        Iterator<Service> allIterator = allServices.iterator();
        while (allIterator.hasNext()) {
            Service service = allIterator.next();
            if (service.getTechnician() != null &&
                    service.getTechnician().getUsername().equals(currentTech.getUsername())) {
                myServices.add(service);
            }
        }

        if (myServices.isEmpty()) {
            showError("Nao possui servicos atribuidos.");
            pause();
            return;
        }

        // Listar serviços do técnico
        System.out.println("Seus servicos:");
        System.out.println();
        int index = 1;
        Iterator<Service> myIterator = myServices.iterator();
        while (myIterator.hasNext()) {
            Service service = myIterator.next();
            System.out.println(index + ". Codigo: " + service.getCode() + " | " +
                    "Cliente: " + service.getClient().getName() + " | " +
                    "Estado: " + service.getStatus() + " | " +
                    "Analises: " + service.getAnalyses().size());
            index++;
        }

        System.out.println();
        System.out.print("Escolha o servico (0 para cancelar): ");
        int serviceChoice = readInt();

        if (serviceChoice < 1 || serviceChoice > myServices.size()) {
            return;
        }

        Service selectedService = myServices.get(serviceChoice - 1);
        ArrayList<ServiceAnalysis> analyses = selectedService.getAnalyses();

        if (analyses.isEmpty()) {
            showError("Este servico nao possui analises.");
            pause();
            return;
        }

        // Listar análises do serviço
        clearScreen();
        System.out.println("Analises do servico " + selectedService.getCode() + ":");
        System.out.println();
        index = 1;
        Iterator<ServiceAnalysis> analysisIterator = analyses.iterator();
        while (analysisIterator.hasNext()) {
            ServiceAnalysis analysis = analysisIterator.next();
            String techName = analysis.getTechnician() != null ? 
                    analysis.getTechnician().getName() : "[Nenhum]";
            System.out.println(index + ". Codigo: " + analysis.getCode() + " | " +
                    "Analise: " + analysis.getAnalysis().getName() + " | " +
                    "Tecnico atual: " + techName);
            index++;
        }

        System.out.println();
        System.out.print("Escolha a analise (0 para cancelar): ");
        int analysisChoice = readInt();

        if (analysisChoice < 1 || analysisChoice > analyses.size()) {
            return;
        }

        ServiceAnalysis selectedAnalysis = analyses.get(analysisChoice - 1);

        // Listar todos os técnicos disponíveis
        ArrayList<User> allUsers = appManager.getManageUsers().listUsers();
        ArrayList<Technician> technicians = new ArrayList<>();
        Iterator<User> userIterator = allUsers.iterator();
        while (userIterator.hasNext()) {
            User user = userIterator.next();
            if (user instanceof Technician && "approved".equals(user.getStatus())) {
                technicians.add((Technician) user);
            }
        }

        if (technicians.isEmpty()) {
            showError("Nenhum tecnico disponivel no sistema.");
            pause();
            return;
        }

        clearScreen();
        System.out.println("Tecnicos disponiveis:");
        System.out.println();
        index = 1;
        Iterator<Technician> techIterator = technicians.iterator();
        while (techIterator.hasNext()) {
            Technician tech = techIterator.next();
            System.out.println(index + ". " + tech.getName() + " (" + tech.getUsername() + ")");
            index++;
        }

        System.out.println();
        System.out.print("Escolha o tecnico (0 para cancelar): ");
        int techChoice = readInt();

        if (techChoice < 1 || techChoice > technicians.size()) {
            return;
        }

        Technician selectedTechnician = technicians.get(techChoice - 1);

        // Atribuir técnico à análise
        if (appManager.getManageServices().assignTechnicianToAnalysis(
                currentTech, selectedService.getCode(), selectedAnalysis.getCode(), selectedTechnician)) {
            showSuccess("Tecnico " + selectedTechnician.getName() + 
                    " atribuido a analise " + selectedAnalysis.getAnalysis().getName() + " com sucesso!");
            logManager.log(currentTech.getUsername(), 
                    "Atribuiu tecnico " + selectedTechnician.getName() + 
                    " a analise " + selectedAnalysis.getCode() + " do servico " + selectedService.getCode());
        } else {
            showError("Erro ao atribuir tecnico a analise.");
        }

        pause();
    }

    private void executeMyAnalyses() {
        while (true) {
            clearScreen();
            System.out.println("==========================================");
            System.out.println("      EXECUTAR ANALISES ATRIBUIDAS       ");
            System.out.println("==========================================");
            System.out.println();
            System.out.println("1. Ver minhas analises");
            System.out.println("2. Adicionar testes a uma analise");
            System.out.println("3. Registrar valores medidos nos testes");
            System.out.println("4. Definir resultado final da analise");
            System.out.println("5. Finalizar analise");
            System.out.println("0. Voltar");
            System.out.println();
            System.out.print("Escolha uma opcao: ");

            int choice = readInt();

            switch (choice) {
                case 1:
                    viewMyAnalyses();
                    break;
                case 2:
                    addTestsToAnalysis();
                    break;
                case 3:
                    recordTestValues();
                    break;
                case 4:
                    setAnalysisFinalResult();
                    break;
                case 5:
                    finalizeAnalysis();
                    break;
                case 0:
                    return;
                default:
                    showError("Opcao invalida!");
                    pause();
            }
        }
    }

    private void viewMyAnalyses() {
        clearScreen();
        System.out.println("==========================================");
        System.out.println("         MINHAS ANALISES ATRIBUIDAS       ");
        System.out.println("==========================================");
        System.out.println();

        Technician currentTech = (Technician) appManager.getSession().getCurrentUser();
        ArrayList<Service> allServices = appManager.getManageServices().listAllServices();
        ArrayList<ServiceAnalysis> myAnalyses = new ArrayList<>();

        // Buscar todas as análises atribuídas ao técnico atual
        Iterator<Service> serviceIterator = allServices.iterator();
        while (serviceIterator.hasNext()) {
            Service service = serviceIterator.next();
            Iterator<ServiceAnalysis> analysisIterator = service.getAnalyses().iterator();
            while (analysisIterator.hasNext()) {
                ServiceAnalysis analysis = analysisIterator.next();
                if (analysis.getTechnician() != null &&
                        analysis.getTechnician().getUsername().equals(currentTech.getUsername())) {
                    myAnalyses.add(analysis);
                }
            }
        }

        if (myAnalyses.isEmpty()) {
            showError("Nenhuma analise atribuida a voce.");
            pause();
            return;
        }

        System.out.println("Total: " + myAnalyses.size() + " analises");
        System.out.println();

        int index = 1;
        Iterator<ServiceAnalysis> myIterator = myAnalyses.iterator();
        while (myIterator.hasNext()) {
            ServiceAnalysis analysis = myIterator.next();
            String supervisor = analysis.getSupervisor() != null ?
                    analysis.getSupervisor().getName() : "[Nenhum]";
            System.out.println(index + ". Codigo: " + analysis.getCode());
            System.out.println("   Analise: " + analysis.getAnalysis().getName());
            System.out.println("   Estado: " + analysis.getStatus());
            System.out.println("   Supervisor: " + supervisor);
            System.out.println("   Testes: " + analysis.getTests().size());
            System.out.println("   Resultado: " + (analysis.getFinalResult().isEmpty() ?
                    "[Pendente]" : analysis.getFinalResult()));
            System.out.println();
            index++;
        }

        pause();
    }

    private void addTestsToAnalysis() {
        clearScreen();
        System.out.println("==========================================");
        System.out.println("       ADICIONAR TESTES A ANALISE         ");
        System.out.println("==========================================");
        System.out.println();

        Technician currentTech = (Technician) appManager.getSession().getCurrentUser();
        ArrayList<Service> allServices = appManager.getManageServices().listAllServices();
        ArrayList<ServiceAnalysis> myAnalyses = new ArrayList<>();

        // Buscar análises do técnico
        Iterator<Service> serviceIterator = allServices.iterator();
        while (serviceIterator.hasNext()) {
            Service service = serviceIterator.next();
            Iterator<ServiceAnalysis> analysisIterator = service.getAnalyses().iterator();
            while (analysisIterator.hasNext()) {
                ServiceAnalysis analysis = analysisIterator.next();
                if (analysis.getTechnician() != null &&
                        analysis.getTechnician().getUsername().equals(currentTech.getUsername())) {
                    myAnalyses.add(analysis);
                }
            }
        }

        if (myAnalyses.isEmpty()) {
            showError("Nenhuma analise atribuida a voce.");
            pause();
            return;
        }

        // Listar análises
        System.out.println("Suas analises:");
        System.out.println();
        int index = 1;
        Iterator<ServiceAnalysis> myIterator = myAnalyses.iterator();
        while (myIterator.hasNext()) {
            ServiceAnalysis analysis = myIterator.next();
            System.out.println(index + ". " + analysis.getAnalysis().getName() +
                    " (Codigo: " + analysis.getCode() + ") - Testes: " + analysis.getTests().size());
            index++;
        }

        System.out.println();
        System.out.print("Escolha a analise (0 para cancelar): ");
        int choice = readInt();

        if (choice < 1 || choice > myAnalyses.size()) {
            return;
        }

        ServiceAnalysis selectedAnalysis = myAnalyses.get(choice - 1);

        // Adicionar testes
        while (true) {
            clearScreen();
            System.out.println("Analise: " + selectedAnalysis.getAnalysis().getName());
            System.out.println("Testes atuais: " + selectedAnalysis.getTests().size());
            System.out.println();

            System.out.print("Designacao do teste: ");
            String designation = scanner.nextLine().trim();

            if (designation.isEmpty()) {
                break;
            }

            System.out.print("Valor de referencia: ");
            String refValue = scanner.nextLine().trim();

            System.out.print("Unidade: ");
            String unit = scanner.nextLine().trim();

            Test newTest = new Test(designation, refValue, unit);
            if (selectedAnalysis.addTest(newTest)) {
                showSuccess("Teste adicionado com sucesso!");
                logManager.log(currentTech.getUsername(),
                        "Adicionou teste '" + designation + "' a analise " + selectedAnalysis.getCode());
            } else {
                showError("Erro ao adicionar teste.");
            }

            System.out.println();
            System.out.print("Adicionar outro teste? (S/N): ");
            String cont = scanner.nextLine().trim();
            if (!cont.equalsIgnoreCase("S")) {
                break;
            }
        }
    }

    private void recordTestValues() {
        clearScreen();
        System.out.println("==========================================");
        System.out.println("     REGISTRAR VALORES DOS TESTES         ");
        System.out.println("==========================================");
        System.out.println();

        Technician currentTech = (Technician) appManager.getSession().getCurrentUser();
        ArrayList<Service> allServices = appManager.getManageServices().listAllServices();
        ArrayList<ServiceAnalysis> myAnalyses = new ArrayList<>();

        // Buscar análises do técnico
        Iterator<Service> serviceIterator = allServices.iterator();
        while (serviceIterator.hasNext()) {
            Service service = serviceIterator.next();
            Iterator<ServiceAnalysis> analysisIterator = service.getAnalyses().iterator();
            while (analysisIterator.hasNext()) {
                ServiceAnalysis analysis = analysisIterator.next();
                if (analysis.getTechnician() != null &&
                        analysis.getTechnician().getUsername().equals(currentTech.getUsername())) {
                    myAnalyses.add(analysis);
                }
            }
        }

        if (myAnalyses.isEmpty()) {
            showError("Nenhuma analise atribuida a voce.");
            pause();
            return;
        }

        // Listar análises
        System.out.println("Suas analises:");
        System.out.println();
        int index = 1;
        Iterator<ServiceAnalysis> myIterator = myAnalyses.iterator();
        while (myIterator.hasNext()) {
            ServiceAnalysis analysis = myIterator.next();
            System.out.println(index + ". " + analysis.getAnalysis().getName() +
                    " - Testes: " + analysis.getTests().size());
            index++;
        }

        System.out.println();
        System.out.print("Escolha a analise (0 para cancelar): ");
        int choice = readInt();

        if (choice < 1 || choice > myAnalyses.size()) {
            return;
        }

        ServiceAnalysis selectedAnalysis = myAnalyses.get(choice - 1);
        ArrayList<Test> tests = selectedAnalysis.getTests();

        if (tests.isEmpty()) {
            showError("Esta analise nao possui testes. Adicione testes primeiro.");
            pause();
            return;
        }

        // Listar testes
        clearScreen();
        System.out.println("Testes da analise: " + selectedAnalysis.getAnalysis().getName());
        System.out.println();
        index = 1;
        Iterator<Test> testIterator = tests.iterator();
        while (testIterator.hasNext()) {
            Test test = testIterator.next();
            String measured = test.getMeasuredValue().isEmpty() ? "[Pendente]" : test.getMeasuredValue();
            System.out.println(index + ". " + test.getDesignation());
            System.out.println("   Referencia: " + test.getReferenceValue() + " " + test.getUnit());
            System.out.println("   Valor medido: " + measured);
            System.out.println();
            index++;
        }

        System.out.print("Escolha o teste (0 para cancelar): ");
        int testChoice = readInt();

        if (testChoice < 1 || testChoice > tests.size()) {
            return;
        }

        Test selectedTest = tests.get(testChoice - 1);

        System.out.println();
        System.out.print("Valor medido: ");
        String measuredValue = scanner.nextLine().trim();

        if (!measuredValue.isEmpty()) {
            selectedTest.setMeasuredValue(measuredValue);
            showSuccess("Valor registrado com sucesso!");
            logManager.log(currentTech.getUsername(),
                    "Registrou valor medido no teste '" + selectedTest.getDesignation() +
                    "' da analise " + selectedAnalysis.getCode());
        }

        pause();
    }

    private void setAnalysisFinalResult() {
        clearScreen();
        System.out.println("==========================================");
        System.out.println("    DEFINIR RESULTADO FINAL DA ANALISE    ");
        System.out.println("==========================================");
        System.out.println();

        Technician currentTech = (Technician) appManager.getSession().getCurrentUser();
        ArrayList<Service> allServices = appManager.getManageServices().listAllServices();
        ArrayList<ServiceAnalysis> myAnalyses = new ArrayList<>();

        // Buscar análises do técnico
        Iterator<Service> serviceIterator = allServices.iterator();
        while (serviceIterator.hasNext()) {
            Service service = serviceIterator.next();
            Iterator<ServiceAnalysis> analysisIterator = service.getAnalyses().iterator();
            while (analysisIterator.hasNext()) {
                ServiceAnalysis analysis = analysisIterator.next();
                if (analysis.getTechnician() != null &&
                        analysis.getTechnician().getUsername().equals(currentTech.getUsername())) {
                    myAnalyses.add(analysis);
                }
            }
        }

        if (myAnalyses.isEmpty()) {
            showError("Nenhuma analise atribuida a voce.");
            pause();
            return;
        }

        // Listar análises
        System.out.println("Suas analises:");
        System.out.println();
        int index = 1;
        Iterator<ServiceAnalysis> myIterator = myAnalyses.iterator();
        while (myIterator.hasNext()) {
            ServiceAnalysis analysis = myIterator.next();
            String result = analysis.getFinalResult().isEmpty() ? "[Pendente]" : analysis.getFinalResult();
            System.out.println(index + ". " + analysis.getAnalysis().getName() +
                    " - Resultado: " + result);
            index++;
        }

        System.out.println();
        System.out.print("Escolha a analise (0 para cancelar): ");
        int choice = readInt();

        if (choice < 1 || choice > myAnalyses.size()) {
            return;
        }

        ServiceAnalysis selectedAnalysis = myAnalyses.get(choice - 1);

        System.out.println();
        System.out.print("Resultado final da analise: ");
        String result = scanner.nextLine().trim();

        if (!result.isEmpty()) {
            selectedAnalysis.setFinalResult(result);
            showSuccess("Resultado definido com sucesso!");
            logManager.log(currentTech.getUsername(),
                    "Definiu resultado final da analise " + selectedAnalysis.getCode() +
                    ": " + result);
        }

        pause();
    }

    private void finalizeAnalysis() {
        clearScreen();
        System.out.println("==========================================");
        System.out.println("          FINALIZAR ANALISE               ");
        System.out.println("==========================================");
        System.out.println();

        Technician currentTech = (Technician) appManager.getSession().getCurrentUser();
        ArrayList<Service> allServices = appManager.getManageServices().listAllServices();
        ArrayList<ServiceAnalysis> myAnalyses = new ArrayList<>();

        // Buscar análises do técnico
        Iterator<Service> serviceIterator = allServices.iterator();
        while (serviceIterator.hasNext()) {
            Service service = serviceIterator.next();
            Iterator<ServiceAnalysis> analysisIterator = service.getAnalyses().iterator();
            while (analysisIterator.hasNext()) {
                ServiceAnalysis analysis = analysisIterator.next();
                if (analysis.getTechnician() != null &&
                        analysis.getTechnician().getUsername().equals(currentTech.getUsername())) {
                    myAnalyses.add(analysis);
                }
            }
        }

        if (myAnalyses.isEmpty()) {
            showError("Nenhuma analise atribuida a voce.");
            pause();
            return;
        }

        // Listar análises pendentes
        System.out.println("Suas analises:");
        System.out.println();
        int index = 1;
        Iterator<ServiceAnalysis> myIterator = myAnalyses.iterator();
        while (myIterator.hasNext()) {
            ServiceAnalysis analysis = myIterator.next();
            System.out.println(index + ". " + analysis.getAnalysis().getName() +
                    " - Estado: " + analysis.getStatus() +
                    " - Testes: " + analysis.getTests().size());
            index++;
        }

        System.out.println();
        System.out.print("Escolha a analise (0 para cancelar): ");
        int choice = readInt();

        if (choice < 1 || choice > myAnalyses.size()) {
            return;
        }

        ServiceAnalysis selectedAnalysis = myAnalyses.get(choice - 1);

        // Verificar se tem resultado final
        if (selectedAnalysis.getFinalResult().isEmpty()) {
            showError("Defina o resultado final antes de finalizar a analise.");
            pause();
            return;
        }

        // Obter data atual
        java.time.LocalDate now = java.time.LocalDate.now();
        java.time.format.DateTimeFormatter formatter = java.time.format.DateTimeFormatter.ofPattern("dd/MM/yyyy");
        String finishDate = now.format(formatter);

        selectedAnalysis.setFinishDate(finishDate);
        selectedAnalysis.setStatus("completed");

        showSuccess("Analise finalizada com sucesso!");
        logManager.log(currentTech.getUsername(),
                "Finalizou a analise " + selectedAnalysis.getCode() +
                " em " + finishDate);

        pause();
    }

    private void listMyServices() {
        clearScreen();
        System.out.println("==========================================");
        System.out.println("       PESQUISAR/LISTAR MEUS SERVICOS     ");
        System.out.println("==========================================");
        System.out.println();

        System.out.print("Termo de pesquisa (vazio para listar todos): ");
        String term = scanner.nextLine().trim();

        Technician currentTech = (Technician) appManager.getSession().getCurrentUser();
        ArrayList<Service> myServices = appManager.getManageServices().listServicesByTechnician(currentTech);

        ArrayList<Service> results = new ArrayList<>();

        if (term.isEmpty()) {
            System.out.println("\nOrdenar servicos por:");
            System.out.println("1 - Codigo");
            System.out.println("2 - Valor Total");
            System.out.print("Opcao: ");
            String sortOption = scanner.nextLine().trim();
            
            if (sortOption.equals("2")) {
                appManager.getManageServices().sortServicesByTotalValue(true);
            } else {
                appManager.getManageServices().sortServicesByCode(true);
            }
            results = myServices;
        } else {
            Iterator<Service> myIterator = myServices.iterator();
            while (myIterator.hasNext()) {
                Service service = myIterator.next();
                if (String.valueOf(service.getCode()).contains(term) ||
                        service.getDescription().toLowerCase().contains(term.toLowerCase())) {
                    results.add(service);
                }
            }
        }

        clearScreen();
        System.out.println("Total: " + results.size() + " servicos");
        System.out.println();

        Iterator<Service> displayIterator = results.iterator();
        while (displayIterator.hasNext()) {
            Service service = displayIterator.next();
            displayService(service);
        }

        pause();
    }

    private void startServiceExecution() {
        clearScreen();
        System.out.println("==========================================");
        System.out.println("         INICIAR EXECUCAO DE SERVICO      ");
        System.out.println("==========================================");
        System.out.println();

        System.out.print("Codigo do servico: ");
        int serviceCode = readInt();

        Technician currentTech = (Technician) appManager.getSession().getCurrentUser();

        if (appManager.getManageServices().startExecution(currentTech, serviceCode)) {
            showSuccess("Servico iniciado com sucesso!");
            logManager.log(currentTech.getUsername(), "Iniciou execucao do servico " + serviceCode);
        } else {
            showError(
                    "Erro ao iniciar servico. Verifique se o codigo esta correto e se e responsavel por este servico.");
        }

        pause();
    }

    private void finishService() {
        clearScreen();
        System.out.println("==========================================");
        System.out.println("             FINALIZAR SERVICO            ");
        System.out.println("==========================================");
        System.out.println();

        System.out.print("Codigo do servico: ");
        int serviceCode = readInt();

        Technician currentTech = (Technician) appManager.getSession().getCurrentUser();

        if (appManager.getManageServices().finishService(currentTech, serviceCode)) {
            showSuccess("Servico finalizado com sucesso!");
            logManager.log(currentTech.getUsername(), "Finalizou servico " + serviceCode);
        } else {
            showError(
                    "Erro ao finalizar servico. Verifique se o codigo esta correto e se e responsavel por este servico.");
        }

        pause();
    }

    private void createAnalysis() {
        clearScreen();
        System.out.println("==========================================");
        System.out.println("               CRIAR ANALISE              ");
        System.out.println("==========================================");
        System.out.println();

        int code = appManager.getManageCatalog().generateAnalysisCode();
        System.out.println("Codigo da analise (gerado): " + code);
        System.out.println();

        System.out.print("Nome: ");
        String name = scanner.nextLine().trim();

        System.out.println("\nCertificacao:");
        System.out.print("Nivel: ");
        String nivel = scanner.nextLine().trim();
        System.out.print("Grau: ");
        String grau = scanner.nextLine().trim();
        Certification certification = new Certification(nivel, grau);

        System.out.print("Metodos: ");
        String methods = scanner.nextLine().trim();

        LabAnalysis analysis = new LabAnalysis(code, name, certification, methods);

        if (appManager.getManageCatalog().addAnalysis(analysis)) {
            showSuccess("Analise criada com sucesso!");
            logManager.log(appManager.getSession().getCurrentUser().getUsername(),
                    "Criou analise " + code);
        } else {
            showError("Erro ao criar analise.");
        }

        pause();
    }

    private void manageAnalyses() {
        while (true) {
            clearScreen();
            System.out.println("==========================================");
            System.out.println("            GESTAO DE ANALISES            ");
            System.out.println("==========================================");
            System.out.println();
            System.out.println("1. Criar analise");
            System.out.println("2. Pesquisar/Listar analises");
            System.out.println("3. Editar analise");
            System.out.println("4. Remover analise");
            System.out.println("5. Listar testes por analise");
            System.out.println("6. Pesquisar analises por componente");
            System.out.println("0. Voltar");
            System.out.println();
            System.out.print("Escolha uma opcao: ");

            int choice = readInt();

            switch (choice) {
                case 1:
                    createAnalysis();
                    break;
                case 2:
                    searchAndDisplayAnalyses();
                    break;
                case 3:
                    editAnalysis();
                    break;
                case 4:
                    removeAnalysis();
                    break;
                case 5:
                    listTestsBySpecificAnalysis();
                    break;
                case 6:
                    searchAnalysesByComponent();
                    break;
                case 0:
                    return;
                default:
                    showError("Opcao invalida!");
                    pause();
            }
        }
    }

    private void searchAndDisplayAnalyses() {
        clearScreen();
        System.out.println("==========================================");
        System.out.println("         PESQUISAR/LISTAR ANALISES        ");
        System.out.println("==========================================");
        System.out.println();

        System.out.print("Termo de pesquisa (vazio para listar todas): ");
        String term = scanner.nextLine().trim();

        ArrayList<LabAnalysis> results;
        if (term.isEmpty()) {
            results = appManager.getManageCatalog().listAnalysis();
        } else {
            results = appManager.getManageCatalog().searchAnalyses(term);
        }

        clearScreen();
        System.out.println("Total de analises: " + results.size());
        System.out.println();

        Iterator<LabAnalysis> analysisIterator = results.iterator();
        while (analysisIterator.hasNext()) {
            LabAnalysis analysis = analysisIterator.next();
            System.out.println(analysis.toString());
        }

        if (!results.isEmpty()) {
            System.out.println("-----------------------------------------");
        }

        pause();
    }

    private void editAnalysis() {
        clearScreen();
        System.out.println("==========================================");
        System.out.println("              EDITAR ANALISE              ");
        System.out.println("==========================================");
        System.out.println();

        System.out.print("Codigo da analise: ");
        int code = readInt();

        LabAnalysis analysis = appManager.getManageCatalog().findAnalysis(code);

        if (analysis == null) {
            showError("Analise nao encontrada!");
            pause();
            return;
        }

        System.out.println();
        System.out.println("Dados atuais:");
        System.out.println(analysis.toString());
        System.out.println();

        System.out.println("O que deseja fazer?");
        System.out.println("1. Adicionar teste");
        System.out.println("2. Remover teste");
        System.out.println("3. Listar testes");
        System.out.println("0. Cancelar");
        System.out.print("Escolha: ");

        int choice = readInt();

        switch (choice) {
            case 1:
                System.out.print("Designacao do teste: ");
                String designation = scanner.nextLine().trim();
                System.out.print("Valor de referencia: ");
                String refValue = scanner.nextLine().trim();
                System.out.print("Unidade: ");
                String unit = scanner.nextLine().trim();
                
                Test newTest = new Test(designation, refValue, unit);
                if (analysis.addTest(newTest)) {
                    showSuccess("Teste adicionado!");
                    logManager.log(appManager.getSession().getCurrentUser().getUsername(),
                            "Adicionou teste a analise " + code);
                } else {
                    showError("Erro ao adicionar teste.");
                }
                break;
                
            case 2:
                System.out.print("Designacao do teste a remover: ");
                String testName = scanner.nextLine().trim();
                if (analysis.removeTest(testName)) {
                    showSuccess("Teste removido!");
                    logManager.log(appManager.getSession().getCurrentUser().getUsername(),
                            "Removeu teste da analise " + code);
                } else {
                    showError("Teste nao encontrado.");
                }
                break;
                
            case 3:
                ArrayList<Test> tests = analysis.getTests();
                System.out.println();
                System.out.println("Testes da analise:");
                if (tests.isEmpty()) {
                    System.out.println("Nenhum teste definido.");
                } else {
                    int index = 1;
                    Iterator<Test> testIterator = tests.iterator();
                    while (testIterator.hasNext()) {
                        Test test = testIterator.next();
                        System.out.println(index + ". " + test.getDesignation() + 
                                         " (Ref: " + test.getReferenceValue() + 
                                         " " + test.getUnit() + ")");
                        index++;
                    }
                }
                break;
        }

        pause();
    }

    private void removeAnalysis() {
        clearScreen();
        System.out.println("==========================================");
        System.out.println("              REMOVER ANALISE             ");
        System.out.println("==========================================");
        System.out.println();

        System.out.print("Termo de pesquisa (vazio para listar todas): ");
        String term = scanner.nextLine().trim();

        ArrayList<LabAnalysis> results = appManager.getManageCatalog().searchAnalyses(term);

        if (results.isEmpty()) {
            showError("Nenhuma analise encontrada!");
            pause();
            return;
        }

        clearScreen();
        System.out.println("Analises encontradas:");
        System.out.println();

        int index = 1;
        Iterator<LabAnalysis> analysisIterator2 = results.iterator();
        while (analysisIterator2.hasNext()) {
            LabAnalysis analysis = analysisIterator2.next();
            System.out.println(index + ".");
            System.out.println(analysis.toString());
            index++;
        }

        if (!results.isEmpty()) {
            System.out.println("-----------------------------------------");
        }

        System.out.print("\\nEscolha o numero da analise (0 para cancelar): ");
        int choice = readInt();

        if (choice < 1 || choice > results.size()) {
            return;
        }

        LabAnalysis selectedAnalysis = results.get(choice - 1);

        System.out.println();
        System.out.print("Tem certeza que deseja remover a analise " + selectedAnalysis.getCode() + "? (S/N): ");
        String confirmation = scanner.nextLine().trim().toUpperCase();

        if (!confirmation.equals("S")) {
            System.out.println("Operacao cancelada.");
            pause();
            return;
        }

        if (appManager.getManageCatalog().removeAnalysis(selectedAnalysis.getCode())) {
            showSuccess("Analise removida com sucesso!");
            logManager.log(appManager.getSession().getCurrentUser().getUsername(),
                    "Removeu analise " + selectedAnalysis.getCode());
        } else {
            showError("Erro ao remover analise.");
        }

        pause();
    }

    private void addChemicalComponent() {
        clearScreen();
        System.out.println("==========================================");
        System.out.println("        ADICIONAR COMPONENTE QUIMICO      ");
        System.out.println("==========================================");
        System.out.println();

        int code = appManager.getManageCatalog().generateComponentCode();
        System.out.println("Codigo (gerado): " + code);
        System.out.println();

        System.out.print("Nome: ");
        String name = scanner.nextLine().trim();

        System.out.print("Valor Alfa: ");
        String alpha = scanner.nextLine().trim();

        System.out.print("Valor Beta: ");
        String beta = scanner.nextLine().trim();

        System.out.print("Quantidade em stock: ");
        int stock = readInt();

        ChemicalComponent component = new ChemicalComponent(code, name, alpha, beta, stock);

        if (appManager.getManageCatalog().addComponent(component)) {
            showSuccess("Componente quimico adicionado com sucesso!");
            logManager.log(appManager.getSession().getCurrentUser().getUsername(),
                    "Adicionou componente quimico " + code);
        } else {
            showError("Erro ao adicionar componente quimico.");
        }

        pause();
    }

    private void createSupplier() {
        clearScreen();
        System.out.println("==========================================");
        System.out.println("              CRIAR FORNECEDOR            ");
        System.out.println("==========================================");
        System.out.println();

        int code = appManager.getManageCatalog().generateSupplierCode();
        System.out.println("Codigo (gerado): " + code);
        System.out.println();

        System.out.print("Nome: ");
        String name = scanner.nextLine().trim();

        System.out.print("Morada: ");
        String address = scanner.nextLine().trim();

        System.out.print("Telefone: ");
        String phone = scanner.nextLine().trim();

        Supplier supplier = new Supplier(code, name, address, phone);

        if (appManager.getManageCatalog().addSupplier(supplier)) {
            showSuccess("Fornecedor criado com sucesso!");
            logManager.log(appManager.getSession().getCurrentUser().getUsername(),
                    "Criou fornecedor " + code);
        } else {
            showError("Erro ao criar fornecedor.");
        }

        pause();
    }

    private void createMedicalArea() {
        clearScreen();
        System.out.println("==========================================");
        System.out.println("             CRIAR AREA MEDICA            ");
        System.out.println("==========================================");
        System.out.println();

        int code = appManager.getManageCatalog().generateAreaCode();
        System.out.println("Codigo (gerado): " + code);
        System.out.println();

        System.out.print("Designacao: ");
        String designation = scanner.nextLine().trim();

        System.out.print("Familia: ");
        String family = scanner.nextLine().trim();

        MedicalArea area = new MedicalArea(code, designation, family);

        if (appManager.getManageCatalog().addArea(area)) {
            showSuccess("Area medica criada com sucesso!");
            logManager.log(appManager.getSession().getCurrentUser().getUsername(),
                    "Criou area medica " + code);
        } else {
            showError("Erro ao criar area medica.");
        }

        pause();
    }

    private void createOrder() {
        clearScreen();
        System.out.println("==========================================");
        System.out.println("              CRIAR ENCOMENDA             ");
        System.out.println("==========================================");
        System.out.println();

        int code = appManager.getManageCatalog().generateOrderCode();
        System.out.println("Codigo da encomenda (gerado): " + code);
        System.out.println();

        ArrayList<Supplier> suppliers = appManager.getManageCatalog().getSuppliers();

        if (suppliers.isEmpty()) {
            showError("Nao existem fornecedores registados!");
            pause();
            return;
        }

        System.out.println();
        System.out.println("Fornecedores disponiveis:");
        int index = 1;
        Iterator<Supplier> supplierIterator = suppliers.iterator();
        while (supplierIterator.hasNext()) {
            Supplier supplier = supplierIterator.next();
            System.out.println(index + ". " + supplier.getName());
            index++;
        }

        System.out.print("Escolha o fornecedor: ");
        int supplierChoice = readInt();

        if (supplierChoice < 1 || supplierChoice > suppliers.size()) {
            showError("Escolha invalida!");
            pause();
            return;
        }

        Supplier selectedSupplier = suppliers.get(supplierChoice - 1);

        String requestDate = java.time.LocalDate.now().toString();

        Technician currentTech = (Technician) appManager.getSession().getCurrentUser();
        Order order = new Order(code, selectedSupplier, currentTech, requestDate);

        System.out.println();
        System.out.println("Adicionar componentes quimicos a encomenda:");
        
        while (true) {
            System.out.println();
            System.out.print("Codigo do componente (0 para terminar): ");
            int componentCode = readInt();
            
            if (componentCode == 0) {
                break;
            }
            
            ChemicalComponent component = appManager.getManageCatalog().findComponent(componentCode);
            
            if (component == null) {
                showError("Componente nao encontrado!");
                continue;
            }
            
            System.out.print("Quantidade a encomendar: ");
            int qty = readInt();
            
            if (qty <= 0) {
                showError("Quantidade invalida!");
                continue;
            }
            
            ChemicalComponent orderItem = new ChemicalComponent(
                component.getCode(),
                component.getName(),
                component.getAlphaValue(),
                component.getBetaValue(),
                qty
            );
            
            order.addItem(orderItem);
            System.out.println("Adicionado: " + component.getName() + " (quantidade: " + qty + ")");
        }

        if (appManager.getManageCatalog().createOrder(currentTech, selectedSupplier, order)) {
            showSuccess("Encomenda criada com sucesso!");
            logManager.log(currentTech.getUsername(), "Criou encomenda " + code);
        } else {
            showError("Erro ao criar encomenda.");
        }

        pause();
    }

    // ==================== CLIENT MENU ====================

    private void showClientMenu() {
        while (appManager.getSession().isLoggedIn()) {
            clearScreen();
            User currentUser = appManager.getSession().getCurrentUser();

            System.out.println("==========================================");
            System.out.println("               MENU CLIENTE               ");
            System.out.println("==========================================");
            System.out.println("Utilizador: " + currentUser.getName());
            System.out.println();
            System.out.println("1. Pedir novo servico");
            System.out.println("2. Pesquisar/Listar meus servicos");
            System.out.println("3. Editar meu perfil");
            System.out.println("0. Logout");
            System.out.println();
            System.out.print("Escolha uma opcao: ");

            int choice = readInt();

            switch (choice) {
                case 1:
                    requestNewService();
                    break;
                case 2:
                    searchMyServicesAsClient();
                    break;
                case 3:
                    editMyProfile();
                    break;
                case 0:
                    return;
                default:
                    showError("Opcao invalida!");
                    pause();
            }
        }
    }

    private void requestNewService() {
        clearScreen();
        System.out.println("==========================================");
        System.out.println("             PEDIR NOVO SERVICO           ");
        System.out.println("==========================================");
        System.out.println();

        int code = appManager.getManageServices().generateServiceCode();
        System.out.println("Codigo do servico (gerado): " + code);
        System.out.println();

        System.out.print("Descricao do servico: ");
        String description = scanner.nextLine().trim();

        if (description.isEmpty()) {
            showError("Descricao nao pode estar vazia!");
            pause();
            return;
        }

        String requestDate = java.time.LocalDate.now().toString();

        Client currentClient = (Client) appManager.getSession().getCurrentUser();
        Service service = new Service(code, currentClient, description, requestDate);

        if (appManager.getManageServices().requestService(currentClient, service)) {
            showSuccess("Servico pedido com sucesso!");
            System.out.println("O seu pedido ficara pendente ate ser aprovado por um tecnico.");
            System.out.println("Apos aprovacao, um administrador associara um tecnico responsavel.");
            logManager.log(currentClient.getUsername(), "Pediu servico " + code);
        } else {
            showError("Erro ao pedir servico.");
        }

        pause();
    }

    private void searchMyServicesAsClient() {
        clearScreen();
        System.out.println("==========================================");
        System.out.println("       PESQUISAR/LISTAR MEUS SERVICOS     ");
        System.out.println("==========================================");
        System.out.println();

        System.out.print("Termo de pesquisa (vazio para listar todos): ");
        String term = scanner.nextLine().trim();

        Client currentClient = (Client) appManager.getSession().getCurrentUser();
        ArrayList<Service> allMyServices = appManager.getManageServices().listServicesByClient(currentClient);
        ArrayList<Service> results = new ArrayList<>();

        if (term.isEmpty()) {
            System.out.println("\nOrdenar servicos por:");
            System.out.println("1 - Codigo");
            System.out.println("2 - Valor Total");
            System.out.print("Opcao: ");
            String sortOption = scanner.nextLine().trim();
            
            if (sortOption.equals("2")) {
                appManager.getManageServices().sortServicesByTotalValue(true);
            } else {
                appManager.getManageServices().sortServicesByCode(true);
            }
            results = allMyServices;
        } else {
            Iterator<Service> myServicesIterator = allMyServices.iterator();
            while (myServicesIterator.hasNext()) {
                Service service = myServicesIterator.next();
                if (String.valueOf(service.getCode()).contains(term) ||
                        service.getDescription().toLowerCase().contains(term.toLowerCase())) {
                    results.add(service);
                }
            }
        }

        clearScreen();
        System.out.println("Resultados encontrados: " + results.size());
        System.out.println();

        Iterator<Service> clientSearchIterator = results.iterator();
        while (clientSearchIterator.hasNext()) {
            Service service = clientSearchIterator.next();
            displayService(service);
        }

        pause();
    }

    // ==================== COMMON FUNCTIONS ====================

    private void editMyProfile() {
        clearScreen();
        System.out.println("==========================================");
        System.out.println("              EDITAR MEU PERFIL           ");
        System.out.println("==========================================");
        System.out.println();

        User currentUser = appManager.getSession().getCurrentUser();

        System.out.println("Dados atuais:");
        System.out.println("Nome: " + currentUser.getName());
        System.out.println("Email: " + currentUser.getEmail());

        if (currentUser instanceof Client) {
            Client c = (Client) currentUser;
            System.out.println("NIF: " + c.getNif());
            System.out.println("Telefone: " + c.getPhone());
            System.out.println("Morada: " + c.getAddress());
        } else if (currentUser instanceof Technician) {
            Technician t = (Technician) currentUser;
            System.out.println("NIF: " + t.getNif());
            System.out.println("Telefone: " + t.getPhone());
            System.out.println("Morada: " + t.getAddress());
        }

        System.out.println();
        System.out.println("O que deseja alterar?");
        System.out.println("1. Nome");
        System.out.println("2. Email");
        System.out.println("3. Password");

        if (currentUser instanceof Client || currentUser instanceof Technician) {
            System.out.println("4. Telefone");
            System.out.println("5. Morada");
        }

        System.out.println("0. Cancelar");
        System.out.print("Escolha: ");

        int choice = readInt();

        switch (choice) {
            case 1:
                System.out.print("Novo nome: ");
                String newName = scanner.nextLine().trim();
                if (!newName.isEmpty()) {
                    currentUser.setName(newName);
                    showSuccess("Nome atualizado!");
                }
                break;
            case 2:
                System.out.print("Novo email: ");
                String newEmail = scanner.nextLine().trim();
                if (!newEmail.isEmpty()) {
                    currentUser.setEmail(newEmail);
                    showSuccess("Email atualizado!");
                }
                break;
            case 3:
                System.out.print("Nova password: ");
                String newPassword = scanner.nextLine().trim();
                if (!newPassword.isEmpty()) {
                    currentUser.setPassword(newPassword);
                    showSuccess("Password atualizada!");
                }
                break;
            case 4:
                if (currentUser instanceof Client) {
                    System.out.print("Novo telefone: ");
                    String newPhone = scanner.nextLine().trim();
                    if (!newPhone.isEmpty()) {
                        ((Client) currentUser).setPhone(newPhone);
                        showSuccess("Telefone atualizado!");
                    }
                } else if (currentUser instanceof Technician) {
                    System.out.print("Novo telefone: ");
                    String newPhone = scanner.nextLine().trim();
                    if (!newPhone.isEmpty()) {
                        ((Technician) currentUser).setPhone(newPhone);
                        showSuccess("Telefone atualizado!");
                    }
                }
                break;
            case 5:
                if (currentUser instanceof Client) {
                    System.out.print("Nova morada: ");
                    String newAddress = scanner.nextLine().trim();
                    if (!newAddress.isEmpty()) {
                        ((Client) currentUser).setAddress(newAddress);
                        showSuccess("Morada atualizada!");
                    }
                } else if (currentUser instanceof Technician) {
                    System.out.print("Nova morada: ");
                    String newAddress = scanner.nextLine().trim();
                    if (!newAddress.isEmpty()) {
                        ((Technician) currentUser).setAddress(newAddress);
                        showSuccess("Morada atualizada!");
                    }
                }
                break;
            case 0:
                return;
        }

        if (choice >= 1 && choice <= 5) {
            logManager.log(currentUser.getUsername(), "Atualizou perfil");
        }

        pause();
    }

    private void viewSystemLog() {
        clearScreen();
        System.out.println("==========================================");
        System.out.println("              LOG DO SISTEMA              ");
        System.out.println("==========================================");
        System.out.println();

        ArrayList<String> logs = logManager.readLog();

        if (logs.isEmpty()) {
            System.out.println("Nao existem registos no log.");
        } else {
            System.out.println("Ultimas " + Math.min(50, logs.size()) + " entradas:");
            System.out.println("(mais recentes primeiro)");
            System.out.println();

            // Show last 50 entries (most recent first - já estão ordenadas)
            int count = 0;
            Iterator<String> logIterator = logs.iterator();
            while (logIterator.hasNext() && count < 50) {
                System.out.println(logIterator.next());
                count++;
            }
        }

        pause();
    }

    private void exportServicesToCSV() {
        clearScreen();
        System.out.println("==========================================");
        System.out.println("         EXPORTAR SERVICOS PARA CSV       ");
        System.out.println("==========================================");
        System.out.println();

        System.out.print("Nome do ficheiro (ex: servicos.csv): ");
        String filename = scanner.nextLine().trim();

        if (filename.isEmpty()) {
            showError("Nome do ficheiro nao pode estar vazio!");
            pause();
            return;
        }

        if (!filename.endsWith(".csv")) {
            filename += ".csv";
        }

        if (appManager.getManageServices().exportServicesToCSV(filename)) {
            showSuccess("Servicos exportados com sucesso para " + filename);
            logManager.log(appManager.getSession().getCurrentUser().getUsername(),
                    "Exportou servicos para CSV");
        } else {
            showError("Erro ao exportar servicos.");
        }

        pause();
    }

    private void listServicesWithSpecificAnalysis() {
        clearScreen();
        System.out.println("==========================================");
        System.out.println("  LISTAR SERVICOS COM ANALISE ESPECIFICA ");
        System.out.println("==========================================");
        System.out.println();

        System.out.print("Codigo da analise: ");
        String analysisCode = scanner.nextLine().trim();

        if (analysisCode.isEmpty()) {
            showError("Codigo da analise nao pode estar vazio!");
            pause();
            return;
        }

        ArrayList<Service> results = appManager.getManageServices().listServicesWithAnalysis(analysisCode);

        clearScreen();
        System.out.println("Servicos com analise " + analysisCode + ": " + results.size());
        System.out.println();

        Iterator<Service> serviceIterator = results.iterator();
        while (serviceIterator.hasNext()) {
            Service service = serviceIterator.next();
            displayService(service);
        }

        pause();
    }

    private void listTestsBySpecificAnalysis() {
        clearScreen();
        System.out.println("==========================================");
        System.out.println("     LISTAR TESTES POR ANALISE            ");
        System.out.println("==========================================");
        System.out.println();

        System.out.print("Codigo da analise: ");
        String analysisCode = scanner.nextLine().trim();

        if (analysisCode.isEmpty()) {
            showError("Codigo da analise nao pode estar vazio!");
            pause();
            return;
        }

        LabAnalysis analysis = appManager.getManageCatalog().findAnalysis(Integer.parseInt(analysisCode));
        
        if (analysis == null) {
            showError("Analise nao encontrada!");
            pause();
            return;
        }

        ArrayList<Test> tests = analysis.getTests();

        clearScreen();
        System.out.println("Testes da analise " + analysis.getName() + ":");
        System.out.println("Total: " + tests.size());
        System.out.println();

        Iterator<Test> testIterator = tests.iterator();
        while (testIterator.hasNext()) {
            Test test = testIterator.next();
            System.out.println(test.toString());
        }

        if (!tests.isEmpty()) {
            System.out.println("-----------------------------------------");
        }

        pause();
    }

    private void searchAnalysesByComponent() {
        clearScreen();
        System.out.println("==========================================");
        System.out.println("  PESQUISAR ANALISES POR COMPONENTE      ");
        System.out.println("==========================================");
        System.out.println();

        System.out.print("Codigo do componente quimico: ");
        String componentCode = scanner.nextLine().trim();

        if (componentCode.isEmpty()) {
            showError("Codigo do componente nao pode estar vazio!");
            pause();
            return;
        }

        ArrayList<LabAnalysis> results = appManager.getManageCatalog().searchAnalysisByComponent(componentCode);

        clearScreen();
        System.out.println("Analises que usam componente " + componentCode + ": " + results.size());
        System.out.println();

        Iterator<LabAnalysis> analysisIterator = results.iterator();
        while (analysisIterator.hasNext()) {
            LabAnalysis analysis = analysisIterator.next();
            System.out.println(analysis.toString());
        }

        if (!results.isEmpty()) {
            System.out.println("-----------------------------------------");
        }

        pause();
    }

    private void displayService(Service service) {
        System.out.println(service.toString());
    }

    // ==================== UTILITY FUNCTIONS ====================

    private void clearScreen() {
        for (int i = 0; i < 50; i++) {
            System.out.println();
        }
    }

    private void showSuccess(String message) {
        System.out.println();
        System.out.println("OK: " + message);
        System.out.println();
    }

    private void showError(String message) {
        System.out.println();
        System.out.println("ERRO: " + message);
        System.out.println();
    }

    private void pause() {
        System.out.println();
        System.out.print("Pressione ENTER para continuar...");
        scanner.nextLine();
    }

    private int readInt() {
        while (true) {
            try {
                String input = scanner.nextLine().trim();
                return Integer.parseInt(input);
            } catch (NumberFormatException e) {
                System.out.print("Por favor, insira um numero valido: ");
            }
        }
    }

    private void manageComponents() {
        while (true) {
            clearScreen();
            System.out.println("==========================================");
            System.out.println("       GESTAO DE COMPONENTES QUIMICOS     ");
            System.out.println("==========================================");
            System.out.println();
            System.out.println("1. Adicionar componente");
            System.out.println("2. Pesquisar/Listar componentes");
            System.out.println("3. Editar componente");
            System.out.println("4. Remover componente");
            System.out.println("0. Voltar");
            System.out.println();
            System.out.print("Escolha uma opcao: ");

            int choice = readInt();

            switch (choice) {
                case 1:
                    addChemicalComponent();
                    break;
                case 2:
                    searchAndDisplayComponents();
                    break;
                case 3:
                    editComponent();
                    break;
                case 4:
                    removeComponent();
                    break;
                case 0:
                    return;
                default:
                    showError("Opcao invalida!");
                    pause();
            }
        }
    }

    private void searchAndDisplayComponents() {
        clearScreen();
        System.out.println("==========================================");
        System.out.println("        PESQUISAR/LISTAR COMPONENTES      ");
        System.out.println("==========================================");
        System.out.println();

        System.out.print("Termo de pesquisa (vazio para listar todos): ");
        String term = scanner.nextLine().trim();

        ArrayList<ChemicalComponent> results;
        if (term.isEmpty()) {
            results = appManager.getManageCatalog().listChemicalComponent();
        } else {
            results = appManager.getManageCatalog().searchComponents(term);
        }

        clearScreen();
        System.out.println("Total de componentes: " + results.size());
        System.out.println();

        Iterator<ChemicalComponent> componentIterator = results.iterator();
        while (componentIterator.hasNext()) {
            ChemicalComponent component = componentIterator.next();
            System.out.println(component.toString());
        }

        if (!results.isEmpty()) {
            System.out.println("-----------------------------------------");
        }

        pause();
    }

    private void editComponent() {
        clearScreen();
        System.out.println("==========================================");
        System.out.println("             EDITAR COMPONENTE            ");
        System.out.println("==========================================");
        System.out.println();

        System.out.print("Codigo do componente: ");
        int code = readInt();

        ChemicalComponent component = appManager.getManageCatalog().findComponent(code);

        if (component == null) {
            showError("Componente nao encontrado!");
            pause();
            return;
        }

        System.out.println();
        System.out.println("Dados atuais:");
        System.out.println(component.toString());
        System.out.println();

        System.out.println("O que deseja alterar?");
        System.out.println("1. Quantidade em stock");
        System.out.println("0. Cancelar");
        System.out.print("Escolha: ");

        int choice = readInt();

        if (choice == 1) {
            System.out.print("Nova quantidade: ");
            int newQty = readInt();
            component.setStockQty(newQty);
            showSuccess("Quantidade atualizada!");
            logManager.log(appManager.getSession().getCurrentUser().getUsername(),
                    "Editou componente " + code);
        }

        pause();
    }

    private void removeComponent() {
        clearScreen();
        System.out.println("==========================================");
        System.out.println("             REMOVER COMPONENTE           ");
        System.out.println("==========================================");
        System.out.println();

        System.out.print("Termo de pesquisa (vazio para listar todos): ");
        String term = scanner.nextLine().trim();

        ArrayList<ChemicalComponent> results = appManager.getManageCatalog().searchComponents(term);

        if (results.isEmpty()) {
            showError("Nenhum componente encontrado!");
            pause();
            return;
        }

        clearScreen();
        System.out.println("Componentes encontrados:");
        System.out.println();

        int index = 1;
        Iterator<ChemicalComponent> componentIterator2 = results.iterator();
        while (componentIterator2.hasNext()) {
            ChemicalComponent component = componentIterator2.next();
            System.out.println(index + ".");
            System.out.println(component.toString());
            index++;
        }

        if (!results.isEmpty()) {
            System.out.println("-----------------------------------------");
        }

        System.out.print("\\nEscolha o numero do componente (0 para cancelar): ");
        int choice = readInt();

        if (choice < 1 || choice > results.size()) {
            return;
        }

        ChemicalComponent selected = results.get(choice - 1);

        System.out.println();
        System.out.print("Tem certeza que deseja remover o componente " + selected.getCode() + "? (S/N): ");
        String confirmation = scanner.nextLine().trim().toUpperCase();

        if (!confirmation.equals("S")) {
            System.out.println("Operacao cancelada.");
            pause();
            return;
        }

        if (appManager.getManageCatalog().removeComponent(selected.getCode())) {
            showSuccess("Componente removido com sucesso!");
            logManager.log(appManager.getSession().getCurrentUser().getUsername(),
                    "Removeu componente " + selected.getCode());
        } else {
            showError("Erro ao remover componente.");
        }

        pause();
    }

    // ==================== SUPPLIER MANAGEMENT ====================

    private void manageSuppliers() {
        while (true) {
            clearScreen();
            System.out.println("==========================================");
            System.out.println("           GESTAO DE FORNECEDORES         ");
            System.out.println("==========================================");
            System.out.println();
            System.out.println("1. Criar fornecedor");
            System.out.println("2. Pesquisar/Listar fornecedores");
            System.out.println("3. Editar fornecedor");
            System.out.println("4. Remover fornecedor");
            System.out.println("0. Voltar");
            System.out.println();
            System.out.print("Escolha uma opcao: ");

            int choice = readInt();

            switch (choice) {
                case 1:
                    createSupplier();
                    break;
                case 2:
                    searchAndDisplaySuppliers();
                    break;
                case 3:
                    editSupplier();
                    break;
                case 4:
                    removeSupplier();
                    break;
                case 0:
                    return;
                default:
                    showError("Opcao invalida!");
                    pause();
            }
        }
    }

    private void searchAndDisplaySuppliers() {
        clearScreen();
        System.out.println("==========================================");
        System.out.println("        PESQUISAR/LISTAR FORNECEDORES     ");
        System.out.println("==========================================");
        System.out.println();

        System.out.print("Termo de pesquisa (vazio para listar todos): ");
        String term = scanner.nextLine().trim();

        ArrayList<Supplier> results = appManager.getManageCatalog().searchSuppliers(term);

        clearScreen();
        System.out.println("Total de fornecedores: " + results.size());
        System.out.println();

        Iterator<Supplier> supplierIterator2 = results.iterator();
        while (supplierIterator2.hasNext()) {
            Supplier supplier = supplierIterator2.next();
            System.out.println(supplier.toString());
        }

        if (!results.isEmpty()) {
            System.out.println("-----------------------------------------");
        }

        pause();
    }

    private void editSupplier() {
        clearScreen();
        System.out.println("==========================================");
        System.out.println("             EDITAR FORNECEDOR            ");
        System.out.println("==========================================");
        System.out.println();

        System.out.print("Codigo do fornecedor: ");
        int code = readInt();

        Supplier supplier = appManager.getManageCatalog().findSupplier(code);

        if (supplier == null) {
            showError("Fornecedor nao encontrado!");
            pause();
            return;
        }

        System.out.println();
        System.out.println("Dados atuais:");
        System.out.println(supplier.toString());
        System.out.println();

        System.out.println("O que deseja alterar?");
        System.out.println("1. Nome");
        System.out.println("2. Morada");
        System.out.println("3. Telefone");
        System.out.println("0. Cancelar");
        System.out.print("Escolha: ");

        int choice = readInt();

        if (choice == 1) {
            System.out.print("Novo nome: ");
            String newName = scanner.nextLine().trim();
            supplier.setName(newName);
            showSuccess("Nome atualizado!");
            logManager.log(appManager.getSession().getCurrentUser().getUsername(),
                    "Editou fornecedor " + code);
        } else if (choice == 2) {
            System.out.print("Nova morada: ");
            String newAddress = scanner.nextLine().trim();
            supplier.setAddress(newAddress);
            showSuccess("Morada atualizada!");
            logManager.log(appManager.getSession().getCurrentUser().getUsername(),
                    "Editou fornecedor " + code);
        } else if (choice == 3) {
            System.out.print("Novo telefone: ");
            String newPhone = scanner.nextLine().trim();
            supplier.setPhone(newPhone);
            showSuccess("Telefone atualizado!");
            logManager.log(appManager.getSession().getCurrentUser().getUsername(),
                    "Editou fornecedor " + code);
        }

        pause();
    }

    private void removeSupplier() {
        clearScreen();
        System.out.println("==========================================");
        System.out.println("             REMOVER FORNECEDOR           ");
        System.out.println("==========================================");
        System.out.println();

        System.out.print("Termo de pesquisa (vazio para listar todos): ");
        String term = scanner.nextLine().trim();

        ArrayList<Supplier> results = appManager.getManageCatalog().searchSuppliers(term);

        if (results.isEmpty()) {
            showError("Nenhum fornecedor encontrado!");
            pause();
            return;
        }

        clearScreen();
        System.out.println("Fornecedores encontrados:");
        System.out.println();

        int index = 1;
        Iterator<Supplier> supplierIterator3 = results.iterator();
        while (supplierIterator3.hasNext()) {
            Supplier supplier = supplierIterator3.next();
            System.out.println(index + ".");
            System.out.println(supplier.toString());
            index++;
        }

        if (!results.isEmpty()) {
            System.out.println("-----------------------------------------");
        }

        System.out.print("\\nEscolha o numero do fornecedor (0 para cancelar): ");
        int choice = readInt();

        if (choice < 1 || choice > results.size()) {
            return;
        }

        Supplier selected = results.get(choice - 1);

        System.out.println();
        System.out.print("Tem certeza que deseja remover o fornecedor " + selected.getCode() + "? (S/N): ");
        String confirmation = scanner.nextLine().trim().toUpperCase();

        if (!confirmation.equals("S")) {
            System.out.println("Operacao cancelada.");
            pause();
            return;
        }

        if (appManager.getManageCatalog().removeSupplier(selected.getCode())) {
            showSuccess("Fornecedor removido com sucesso!");
            logManager.log(appManager.getSession().getCurrentUser().getUsername(),
                    "Removeu fornecedor " + selected.getCode());
        } else {
            showError("Erro ao remover fornecedor.");
        }

        pause();
    }

    // ==================== MEDICAL AREA MANAGEMENT ====================

    private void manageAreas() {
        while (true) {
            clearScreen();
            System.out.println("==========================================");
            System.out.println("          GESTAO DE AREAS MEDICAS         ");
            System.out.println("==========================================");
            System.out.println();
            System.out.println("1. Criar area medica");
            System.out.println("2. Pesquisar/Listar areas medicas");
            System.out.println("3. Editar area medica");
            System.out.println("4. Remover area medica");
            System.out.println("0. Voltar");
            System.out.println();
            System.out.print("Escolha uma opcao: ");

            int choice = readInt();

            switch (choice) {
                case 1:
                    createMedicalArea();
                    break;
                case 2:
                    searchAndDisplayAreas();
                    break;
                case 3:
                    editArea();
                    break;
                case 4:
                    removeArea();
                    break;
                case 0:
                    return;
                default:
                    showError("Opcao invalida!");
                    pause();
            }
        }
    }

    private void searchAndDisplayAreas() {
        clearScreen();
        System.out.println("==========================================");
        System.out.println("        PESQUISAR/LISTAR AREAS MEDICAS    ");
        System.out.println("==========================================");
        System.out.println();

        System.out.print("Termo de pesquisa (vazio para listar todas): ");
        String term = scanner.nextLine().trim();

        ArrayList<MedicalArea> results = appManager.getManageCatalog().searchAreas(term);

        clearScreen();
        System.out.println("Total de areas medicas: " + results.size());
        System.out.println();

        Iterator<MedicalArea> areaIterator = results.iterator();
        while (areaIterator.hasNext()) {
            MedicalArea area = areaIterator.next();
            System.out.println(area.toString());
        }

        if (!results.isEmpty()) {
            System.out.println("-----------------------------------------");
        }

        pause();
    }

    private void editArea() {
        clearScreen();
        System.out.println("==========================================");
        System.out.println("             EDITAR AREA MEDICA           ");
        System.out.println("==========================================");
        System.out.println();

        System.out.print("Codigo da area: ");
        int code = readInt();

        MedicalArea area = appManager.getManageCatalog().findArea(code);

        if (area == null) {
            showError("Area medica nao encontrada!");
            pause();
            return;
        }

        System.out.println();
        System.out.println("Dados atuais:");
        System.out.println(area.toString());
        System.out.println();

        System.out.println("O que deseja alterar?");
        System.out.println("1. Designacao");
        System.out.println("2. Familia");
        System.out.println("0. Cancelar");
        System.out.print("Escolha: ");

        int choice = readInt();

        if (choice == 1) {
            System.out.print("Nova designacao: ");
            String newDesignation = scanner.nextLine().trim();
            area.setDesignation(newDesignation);
            showSuccess("Designacao atualizada!");
            logManager.log(appManager.getSession().getCurrentUser().getUsername(),
                    "Editou area medica " + code);
        } else if (choice == 2) {
            System.out.print("Nova familia: ");
            String newFamily = scanner.nextLine().trim();
            area.setFamily(newFamily);
            showSuccess("Familia atualizada!");
            logManager.log(appManager.getSession().getCurrentUser().getUsername(),
                    "Editou area medica " + code);
        }

        pause();
    }

    private void removeArea() {
        clearScreen();
        System.out.println("==========================================");
        System.out.println("             REMOVER AREA MEDICA          ");
        System.out.println("==========================================");
        System.out.println();

        System.out.print("Termo de pesquisa (vazio para listar todas): ");
        String term = scanner.nextLine().trim();

        ArrayList<MedicalArea> results = appManager.getManageCatalog().searchAreas(term);

        if (results.isEmpty()) {
            showError("Nenhuma area medica encontrada!");
            pause();
            return;
        }

        clearScreen();
        System.out.println("Areas medicas encontradas:");
        System.out.println();

        int index = 1;
        Iterator<MedicalArea> areaIterator2 = results.iterator();
        while (areaIterator2.hasNext()) {
            MedicalArea area = areaIterator2.next();
            System.out.println(index + ".");
            System.out.println(area.toString());
            index++;
        }

        if (!results.isEmpty()) {
            System.out.println("-----------------------------------------");
        }

        System.out.print("\\nEscolha o numero da area (0 para cancelar): ");
        int choice = readInt();

        if (choice < 1 || choice > results.size()) {
            return;
        }

        MedicalArea selected = results.get(choice - 1);

        System.out.println();
        System.out.print("Tem certeza que deseja remover a area " + selected.getCode() + "? (S/N): ");
        String confirmation = scanner.nextLine().trim().toUpperCase();

        if (!confirmation.equals("S")) {
            System.out.println("Operacao cancelada.");
            pause();
            return;
        }

        if (appManager.getManageCatalog().removeArea(selected.getCode())) {
            showSuccess("Area medica removida com sucesso!");
            logManager.log(appManager.getSession().getCurrentUser().getUsername(),
                    "Removeu area medica " + selected.getCode());
        } else {
            showError("Erro ao remover area medica.");
        }

        pause();
    }

    // ==================== ORDER MANAGEMENT ====================

    private void manageOrders() {
        while (true) {
            clearScreen();
            System.out.println("==========================================");
            System.out.println("            GESTAO DE ENCOMENDAS          ");
            System.out.println("==========================================");
            System.out.println();
            System.out.println("1. Criar encomenda");
            System.out.println("2. Pesquisar/Listar encomendas");
            System.out.println("3. Marcar como entregue");
            System.out.println("4. Listar encomendas pendentes");
            System.out.println("5. Remover encomenda");
            System.out.println("0. Voltar");
            System.out.println();
            System.out.print("Escolha uma opcao: ");

            int choice = readInt();

            switch (choice) {
                case 1:
                    createOrder();
                    break;
                case 2:
                    searchAndDisplayOrders();
                    break;
                case 3:
                    deliverOrder();
                    break;
                case 4:
                    listPendingOrders();
                    break;
                case 5:
                    removeOrder();
                    break;
                case 0:
                    return;
                default:
                    showError("Opcao invalida!");
                    pause();
            }
        }
    }

    private void searchAndDisplayOrders() {
        clearScreen();
        System.out.println("==========================================");
        System.out.println("         PESQUISAR/LISTAR ENCOMENDAS      ");
        System.out.println("==========================================");
        System.out.println();

        System.out.print("Termo de pesquisa (vazio para listar todas): ");
        String term = scanner.nextLine().trim();

        ArrayList<Order> results;
        if (term.isEmpty()) {
            results = appManager.getManageCatalog().listOrders();
        } else {
            results = appManager.getManageCatalog().searchOrders(term);
        }

        clearScreen();
        System.out.println("Total de encomendas: " + results.size());
        System.out.println();

        Iterator<Order> orderIterator = results.iterator();
        while (orderIterator.hasNext()) {
            Order order = orderIterator.next();
            System.out.println(order.toString());
        }

        if (!results.isEmpty()) {
            System.out.println("-----------------------------------------");
        }

        pause();
    }

    private void deliverOrder() {
        clearScreen();
        System.out.println("==========================================");
        System.out.println("          MARCAR ENCOMENDA ENTREGUE       ");
        System.out.println("==========================================");
        System.out.println();

        System.out.print("Codigo da encomenda: ");
        int code = readInt();

        if (appManager.getManageCatalog().deliverOrder(code)) {
            showSuccess("Encomenda marcada como entregue!");
            logManager.log(appManager.getSession().getCurrentUser().getUsername(),
                    "Marcou encomenda " + code + " como entregue");
        } else {
            showError("Erro ao marcar encomenda. Verifique o codigo.");
        }

        pause();
    }

    private void listPendingOrders() {
        clearScreen();
        System.out.println("==========================================");
        System.out.println("            ENCOMENDAS PENDENTES          ");
        System.out.println("==========================================");
        System.out.println();

        ArrayList<Order> pendingOrders = appManager.getManageCatalog().listPendingOrders();

        System.out.println("Total de encomendas pendentes: " + pendingOrders.size());
        System.out.println();

        Iterator<Order> pendingOrderIterator = pendingOrders.iterator();
        while (pendingOrderIterator.hasNext()) {
            Order order = pendingOrderIterator.next();
            System.out.println(order.toString());
        }

        if (!pendingOrders.isEmpty()) {
            System.out.println("-----------------------------------------");
        }

        pause();
    }

    private void removeOrder() {
        clearScreen();
        System.out.println("==========================================");
        System.out.println("             REMOVER ENCOMENDA            ");
        System.out.println("==========================================");
        System.out.println();

        System.out.print("Termo de pesquisa (vazio para listar todas): ");
        String term = scanner.nextLine().trim();

        ArrayList<Order> results = appManager.getManageCatalog().searchOrders(term);

        if (results.isEmpty()) {
            showError("Nenhuma encomenda encontrada!");
            pause();
            return;
        }

        clearScreen();
        System.out.println("Encomendas encontradas:");
        System.out.println();

        int index = 1;
        Iterator<Order> orderIterator2 = results.iterator();
        while (orderIterator2.hasNext()) {
            Order order = orderIterator2.next();
            System.out.println(index + ".");
            System.out.println(order.toString());
            index++;
        }

        if (!results.isEmpty()) {
            System.out.println("-----------------------------------------");
        }

        System.out.print("\\nEscolha o numero da encomenda (0 para cancelar): ");
        int choice = readInt();

        if (choice < 1 || choice > results.size()) {
            return;
        }

        Order selected = results.get(choice - 1);

        System.out.println();
        System.out.print("Tem certeza que deseja remover a encomenda " + selected.getCode() + "? (S/N): ");
        String confirmation = scanner.nextLine().trim().toUpperCase();

        if (!confirmation.equals("S")) {
            System.out.println("Operacao cancelada.");
            pause();
            return;
        }

        if (appManager.getManageCatalog().removeOrder(selected.getCode())) {
            showSuccess("Encomenda removida com sucesso!");
            logManager.log(appManager.getSession().getCurrentUser().getUsername(),
                    "Removeu encomenda " + selected.getCode());
        } else {
            showError("Erro ao remover encomenda.");
        }

        pause();
    }

    // ==================== CATEGORY MANAGEMENT ====================

    private void manageCategories() {
        while (true) {
            clearScreen();
            System.out.println("==========================================");
            System.out.println("           GESTAO DE CATEGORIAS           ");
            System.out.println("==========================================");
            System.out.println();
            System.out.println("1. Criar categoria");
            System.out.println("2. Pesquisar/Listar categorias");
            System.out.println("3. Editar categoria");
            System.out.println("4. Remover categoria");
            System.out.println("0. Voltar");
            System.out.println();
            System.out.print("Escolha uma opcao: ");

            int choice = readInt();

            switch (choice) {
                case 1:
                    createCategory();
                    break;
                case 2:
                    searchAndDisplayCategories();
                    break;
                case 3:
                    editCategory();
                    break;
                case 4:
                    removeCategory();
                    break;
                case 0:
                    return;
                default:
                    showError("Opcao invalida!");
                    pause();
            }
        }
    }

    private void createCategory() {
        clearScreen();
        System.out.println("==========================================");
        System.out.println("            CRIAR CATEGORIA               ");
        System.out.println("==========================================");
        System.out.println();

        int code = appManager.getManageCatalog().generateCategoryCode();
        System.out.println("Codigo (gerado): " + code);
        System.out.println();

        System.out.print("Nome: ");
        String name = scanner.nextLine().trim();

        if (name.isEmpty()) {
            showError("Nome nao pode estar vazio!");
            pause();
            return;
        }

        Category category = new Category(code, name);

        if (appManager.getManageCatalog().addCategory(category)) {
            showSuccess("Categoria criada com sucesso!");
            logManager.log(appManager.getSession().getCurrentUser().getUsername(),
                    "Criou categoria " + code);
        } else {
            showError("Erro ao criar categoria.");
        }

        pause();
    }

    private void searchAndDisplayCategories() {
        clearScreen();
        System.out.println("==========================================");
        System.out.println("      PESQUISAR/LISTAR CATEGORIAS         ");
        System.out.println("==========================================");
        System.out.println();

        System.out.print("Termo de pesquisa (vazio para listar todas): ");
        String term = scanner.nextLine().trim();

        ArrayList<Category> results = appManager.getManageCatalog().searchCategories(term);

        clearScreen();
        System.out.println("Total de categorias: " + results.size());
        System.out.println();

        Iterator<Category> categoryIterator = results.iterator();
        while (categoryIterator.hasNext()) {
            Category category = categoryIterator.next();
            System.out.println(category.toString());
        }

        if (!results.isEmpty()) {
            System.out.println("-----------------------------------------");
        }

        pause();
    }

    private void editCategory() {
        clearScreen();
        System.out.println("==========================================");
        System.out.println("             EDITAR CATEGORIA             ");
        System.out.println("==========================================");
        System.out.println();

        System.out.print("Codigo da categoria: ");
        int code = readInt();

        Category category = appManager.getManageCatalog().findCategory(code);

        if (category == null) {
            showError("Categoria nao encontrada!");
            pause();
            return;
        }

        System.out.println();
        System.out.println("Dados atuais:");
        System.out.println(category.toString());
        System.out.println();

        System.out.print("Novo nome: ");
        String newName = scanner.nextLine().trim();

        if (newName.isEmpty()) {
            showError("Nome nao pode estar vazio!");
            pause();
            return;
        }

        category.setName(newName);
        showSuccess("Nome atualizado!");
        logManager.log(appManager.getSession().getCurrentUser().getUsername(),
                "Editou categoria " + code);

        pause();
    }

    private void removeCategory() {
        clearScreen();
        System.out.println("==========================================");
        System.out.println("            REMOVER CATEGORIA             ");
        System.out.println("==========================================");
        System.out.println();

        System.out.print("Termo de pesquisa (vazio para listar todas): ");
        String term = scanner.nextLine().trim();

        ArrayList<Category> results = appManager.getManageCatalog().searchCategories(term);

        if (results.isEmpty()) {
            showError("Nenhuma categoria encontrada!");
            pause();
            return;
        }

        clearScreen();
        System.out.println("Categorias encontradas:");
        System.out.println();

        int index = 1;
        Iterator<Category> categoryIterator2 = results.iterator();
        while (categoryIterator2.hasNext()) {
            Category category = categoryIterator2.next();
            System.out.println(index + ".");
            System.out.println(category.toString());
            index++;
        }

        if (!results.isEmpty()) {
            System.out.println("-----------------------------------------");
        }

        System.out.print("\nEscolha o numero da categoria (0 para cancelar): ");
        int choice = readInt();

        if (choice < 1 || choice > results.size()) {
            return;
        }

        Category selectedCategory = results.get(choice - 1);

        System.out.println();
        System.out.print("Tem certeza que deseja remover a categoria " + selectedCategory.getCode() + "? (S/N): ");
        String confirmation = scanner.nextLine().trim().toUpperCase();

        if (!confirmation.equals("S")) {
            System.out.println("Operacao cancelada.");
            pause();
            return;
        }

        if (appManager.getManageCatalog().removeCategory(selectedCategory.getCode())) {
            showSuccess("Categoria removida com sucesso!");
            logManager.log(appManager.getSession().getCurrentUser().getUsername(),
                    "Removeu categoria " + selectedCategory.getCode());
        } else {
            showError("Erro ao remover categoria.");
        }
        pause();
    }
}
