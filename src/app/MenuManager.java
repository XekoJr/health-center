package app;

import users.*;
import services.*;
import util.*;
import java.util.ArrayList;
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

    // --- Main Menu ---

    // Show main application menu (login or register)
    public void showMainMenu() {
        while (true) {
            clearScreen();
            System.out.println("==========================================");
            System.out.println(Colors.BOLD_CYAN + "     SISTEMA DE GESTAO LABORATORIAL      " + Colors.RESET);
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

    // --- Login & Register ---

    // Handle user login and redirect to appropriate menu
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
        System.out.println(Colors.BOLD_CYAN + "                BEM-VINDO                 " + Colors.RESET);
        System.out.println("==========================================");
        System.out.println();
        System.out.println(Colors.BOLD_GREEN + "Bem-vindo " + user.getName() + "!" + Colors.RESET);
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
        System.out.println(Colors.BOLD_YELLOW + "Adeus " + name + "!" + Colors.RESET);
        pause();
    }

    // Register new user with validation
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

        if (!Validator.isValidUsername(username)) {
            showError(Validator.getUsernameError());
            pause();
            return;
        }

        System.out.print("Email: ");
        String email = scanner.nextLine().trim();

        if (!Validator.isValidEmail(email)) {
            showError(Validator.getEmailError());
            pause();
            return;
        }

        System.out.print("Password: ");
        String password = scanner.nextLine().trim();

        if (!Validator.isValidPassword(password)) {
            showError(Validator.getPasswordError());
            pause();
            return;
        }

        System.out.print("Nome completo: ");
        String name = scanner.nextLine().trim();

        if (!Validator.isValidName(name)) {
            showError(Validator.getNameError());
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
                if (!Validator.isNonNegativeInteger(nif) || nif.length() != 9) {
                    showError("NIF invalido. Deve ter exatamente 9 digitos.");
                    pause();
                    return;
                }
                System.out.print("Morada: ");
                String address = scanner.nextLine().trim();
                if (!Validator.isNotEmpty(address)) {
                    showError("Morada nao pode estar vazia!");
                    pause();
                    return;
                }
                System.out.print("Telefone: ");
                String phone = scanner.nextLine().trim();
                if (!Validator.isValidPhone(phone)) {
                    showError(Validator.getPhoneError());
                    pause();
                    return;
                }

                newUser = new Client(username, email, password, name, "pending", type, false, nif, address, phone);
            } else if (typeChoice == 2) {
                type = "technician";
                System.out.print("NIF: ");
                String nif = scanner.nextLine().trim();
                if (!Validator.isNonNegativeInteger(nif) || nif.length() != 9) {
                    showError("NIF invalido. Deve ter exatamente 9 digitos.");
                    pause();
                    return;
                }
                System.out.print("Morada: ");
                String address = scanner.nextLine().trim();
                if (!Validator.isNotEmpty(address)) {
                    showError("Morada nao pode estar vazia!");
                    pause();
                    return;
                }
                System.out.print("Telefone: ");
                String phone = scanner.nextLine().trim();
                if (!Validator.isValidPhone(phone)) {
                    showError(Validator.getPhoneError());
                    pause();
                    return;
                }

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

    // --- Admin Menu ---

    // Display admin menu with all management options
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

    // --- User Management (Admin) ---

    // User management menu for admin
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

    // Approve or reject pending user registrations
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

        appManager.getManageUsers().displayUserList(pendingUsers);

        System.out.println();
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

    // List all users or filter by type
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

        appManager.getManageUsers().displayUserList(users);

        System.out.println("-----------------------------------------");
        pause();
    }

    // Search users by username or name
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

        appManager.getManageUsers().displayUserList(results);

        if (!results.isEmpty()) {
            System.out.println("-----------------------------------------");
        }

        pause();
    }

    // --- Service Management (Admin) ---

    // Service management menu for admin
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

    // Search or list all services
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

        appManager.getManageServices().displayServiceList(results);

        pause();
    }

    // Filter and list services by status
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

        appManager.getManageServices().displayServiceList(results);

        pause();
    }

    // Admin approves or rejects service requests
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

        appManager.getManageServices().displayServiceList(pendingServices);

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

    // Assign technician to approved service
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

        appManager.getManageServices().displayServiceList(pendingServices);

        System.out.println();
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

        appManager.getManageUsers().displayUserListIndexed(technicians);

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

    // Display technician menu with all available options
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

    // Technician approves or rejects service requests
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

        appManager.getManageServices().displayServiceList(pendingServices);

        System.out.println();
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

    // --- Analysis Management (Technician) ---

    // Assign technicians to service analyses
    private void assignTechniciansToAnalyses() {
        clearScreen();
        System.out.println("==========================================");
        System.out.println("   ATRIBUIR TECNICOS AS ANALISES [R23]   ");
        System.out.println("==========================================");
        System.out.println();

        Technician currentTech = (Technician) appManager.getSession().getCurrentUser();
        ArrayList<Service> myServices = appManager.getManageServices().listServicesByTechnician(currentTech);

        if (myServices.isEmpty()) {
            showError("Nao possui servicos atribuidos.");
            pause();
            return;
        }

        // Listar serviços do técnico
        System.out.println("Seus servicos:");
        System.out.println();
        appManager.getManageServices().displayServiceList(myServices);

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
        appManager.getManageCatalog().displayServiceAnalysisListIndexed(analyses);

        System.out.println();
        System.out.print("Escolha a analise (0 para cancelar): ");
        int analysisChoice = readInt();

        if (analysisChoice < 1 || analysisChoice > analyses.size()) {
            return;
        }

        ServiceAnalysis selectedAnalysis = analyses.get(analysisChoice - 1);

        // List all available approved technicians
        ArrayList<Technician> technicians = appManager.getManageUsers().listApprovedTechnicians();

        if (technicians.isEmpty()) {
            showError("Nenhum tecnico disponivel no sistema.");
            pause();
            return;
        }

        clearScreen();
        System.out.println("Tecnicos disponiveis:");
        System.out.println();
        ArrayList<User> techList = new ArrayList<>(technicians);
        appManager.getManageUsers().displayUserListDetailed(techList);

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

    // Menu for executing assigned analyses
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

    // View all analyses assigned to current technician
    private void viewMyAnalyses() {
        clearScreen();
        System.out.println("==========================================");
        System.out.println("         MINHAS ANALISES ATRIBUIDAS       ");
        System.out.println("==========================================");
        System.out.println();

        Technician currentTech = (Technician) appManager.getSession().getCurrentUser();
        ArrayList<ServiceAnalysis> myAnalyses = appManager.getManageServices().listAnalysesByTechnician(currentTech);

        if (myAnalyses.isEmpty()) {
            showError("Nenhuma analise atribuida a voce.");
            pause();
            return;
        }

        System.out.println("Total: " + myAnalyses.size() + " analises");
        System.out.println();

        appManager.getManageServices().displayServiceAnalysisListDetailed(myAnalyses);

        pause();
    }

    // Add tests to a specific analysis
    private void addTestsToAnalysis() {
        clearScreen();
        System.out.println("==========================================");
        System.out.println("       ADICIONAR TESTES A ANALISE         ");
        System.out.println("==========================================");
        System.out.println();

        Technician currentTech = (Technician) appManager.getSession().getCurrentUser();
        ArrayList<ServiceAnalysis> myAnalyses = appManager.getManageServices().listAnalysesByTechnician(currentTech);

        if (myAnalyses.isEmpty()) {
            showError("Nenhuma analise atribuida a voce.");
            pause();
            return;
        }

        // Listar análises
        System.out.println("Suas analises:");
        System.out.println();
        appManager.getManageServices().displayServiceAnalysisListIndexed(myAnalyses);

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

    // Record measured values for analysis tests
    private void recordTestValues() {
        clearScreen();
        System.out.println("==========================================");
        System.out.println("     REGISTRAR VALORES DOS TESTES         ");
        System.out.println("==========================================");
        System.out.println();

        Technician currentTech = (Technician) appManager.getSession().getCurrentUser();
        ArrayList<ServiceAnalysis> myAnalyses = appManager.getManageServices().listAnalysesByTechnician(currentTech);

        if (myAnalyses.isEmpty()) {
            showError("Nenhuma analise atribuida a voce.");
            pause();
            return;
        }

        // Listar análises
        System.out.println("Suas analises:");
        System.out.println();
        appManager.getManageCatalog().displayServiceAnalysisListCompact(myAnalyses);

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
        appManager.getManageCatalog().displayTestListIndexed(tests);

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

    // Set final result for completed analysis
    private void setAnalysisFinalResult() {
        clearScreen();
        System.out.println("==========================================");
        System.out.println("    DEFINIR RESULTADO FINAL DA ANALISE    ");
        System.out.println("==========================================");
        System.out.println();

        Technician currentTech = (Technician) appManager.getSession().getCurrentUser();
        ArrayList<ServiceAnalysis> myAnalyses = appManager.getManageServices().listAnalysesByTechnician(currentTech);

        if (myAnalyses.isEmpty()) {
            showError("Nenhuma analise atribuida a voce.");
            pause();
            return;
        }

        // Listar análises
        System.out.println("Suas analises:");
        System.out.println();
        appManager.getManageCatalog().displayServiceAnalysisListWithResult(myAnalyses);

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

    // Mark analysis as completed with finish date
    private void finalizeAnalysis() {
        clearScreen();
        System.out.println("==========================================");
        System.out.println("          FINALIZAR ANALISE               ");
        System.out.println("==========================================");
        System.out.println();

        Technician currentTech = (Technician) appManager.getSession().getCurrentUser();
        ArrayList<ServiceAnalysis> myAnalyses = appManager.getManageServices().listAnalysesByTechnician(currentTech);

        if (myAnalyses.isEmpty()) {
            showError("Nenhuma analise atribuida a voce.");
            pause();
            return;
        }

        // Listar análises pendentes
        System.out.println("Suas analises:");
        System.out.println();
        appManager.getManageCatalog().displayServiceAnalysisListWithStatus(myAnalyses);

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

    // Search and list technician's assigned services
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
            results = appManager.getManageServices().searchServicesByCodeOrDescription(myServices, term);
        }

        clearScreen();
        System.out.println("Total: " + results.size() + " servicos");
        System.out.println();

        appManager.getManageServices().displayServiceList(results);

        pause();
    }

    // Start execution of a service
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

    // Mark service as finished
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

    // --- Catalog Management (Technician) ---

    // Create new lab analysis
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

    // Analysis catalog management menu
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

    // Search and display all analyses
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

        appManager.getManageCatalog().displayAnalysisList(results);

        if (!results.isEmpty()) {
            System.out.println("-----------------------------------------");
        }

        pause();
    }

    // Edit existing analysis (add/remove tests)
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
                appManager.getManageCatalog().displayTestList(tests);
                break;
        }

        pause();
    }

    // Remove analysis from catalog
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

        appManager.getManageCatalog().displayAnalysisListIndexed(results);

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

    // Add new chemical component to catalog
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

    // Create new supplier with validation
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
        if (!Validator.isNotEmpty(name)) {
            showError("Nome nao pode estar vazio!");
            pause();
            return;
        }

        System.out.print("Morada: ");
        String address = scanner.nextLine().trim();
        if (!Validator.isNotEmpty(address)) {
            showError("Morada nao pode estar vazia!");
            pause();
            return;
        }

        System.out.print("Telefone: ");
        String phone = scanner.nextLine().trim();
        if (!Validator.isValidPhone(phone)) {
            showError(Validator.getPhoneError());
            pause();
            return;
        }

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

    // Create new medical area
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

    // Create new order for chemical components
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
        appManager.getManageCatalog().displaySupplierListIndexed(suppliers);

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
                    qty);

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

    // Display client menu with service options
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

    // Client requests a new service
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

    // Client searches their own services
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
            results = appManager.getManageServices().searchServicesByCodeOrDescription(allMyServices, term);
        }

        clearScreen();
        System.out.println("Resultados encontrados: " + results.size());
        System.out.println();

        appManager.getManageServices().displayServiceList(results);

        pause();
    }

    // ==================== COMMON FUNCTIONS ====================

    // Edit current user profile information
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
                if (!Validator.isValidName(newName)) {
                    showError(Validator.getNameError());
                } else {
                    currentUser.setName(newName);
                    showSuccess("Nome atualizado!");
                }
                break;
            case 2:
                System.out.print("Novo email: ");
                String newEmail = scanner.nextLine().trim();
                if (!Validator.isValidEmail(newEmail)) {
                    showError(Validator.getEmailError());
                } else {
                    currentUser.setEmail(newEmail);
                    showSuccess("Email atualizado!");
                }
                break;
            case 3:
                System.out.print("Nova password: ");
                String newPassword = scanner.nextLine().trim();
                if (!Validator.isValidPassword(newPassword)) {
                    showError(Validator.getPasswordError());
                } else {
                    currentUser.setPassword(newPassword);
                    showSuccess("Password atualizada!");
                }
                break;
            case 4:
                if (currentUser instanceof Client) {
                    System.out.print("Novo telefone: ");
                    String newPhone = scanner.nextLine().trim();
                    if (!Validator.isValidPhone(newPhone)) {
                        showError(Validator.getPhoneError());
                    } else {
                        ((Client) currentUser).setPhone(newPhone);
                        showSuccess("Telefone atualizado!");
                    }
                } else if (currentUser instanceof Technician) {
                    System.out.print("Novo telefone: ");
                    String newPhone = scanner.nextLine().trim();
                    if (!Validator.isValidPhone(newPhone)) {
                        showError(Validator.getPhoneError());
                    } else {
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

    // View system activity log
    private void viewSystemLog() {
        clearScreen();
        System.out.println("==========================================");
        System.out.println("              LOG DO SISTEMA              ");
        System.out.println("==========================================");
        System.out.println();

        ArrayList<String> logs = logManager.readLog();

        if (!logs.isEmpty()) {
            System.out.println("Ultimas " + Math.min(50, logs.size()) + " entradas:");
            System.out.println("(mais recentes primeiro)");
            System.out.println();
        }

        logManager.displayRecentLogs(50);

        pause();
    }

    // Export all services to CSV file
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

    // List services that contain specific analysis
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

        appManager.getManageServices().displayServiceList(results);

        pause();
    }

    // List all tests for a specific analysis
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

        appManager.getManageCatalog().displayTestList(tests);

        if (!tests.isEmpty()) {
            System.out.println("-----------------------------------------");
        }

        pause();
    }

    // Search analyses that use specific chemical component
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

        appManager.getManageCatalog().displayAnalysisList(results);

        if (!results.isEmpty()) {
            System.out.println("-----------------------------------------");
        }

        pause();
    }

    // ==================== CATALOG MANAGEMENT (TECHNICIAN) ====================
    // Component, Supplier, Medical Area, Order, and Category management

    // --- Component Management ---

    // Chemical component management menu
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

    // Search and display chemical components
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

        appManager.getManageCatalog().displayComponentList(results);

        if (!results.isEmpty()) {
            System.out.println("-----------------------------------------");
        }

        pause();
    }

    // Edit component stock quantity
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

    // Remove chemical component from catalog
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

        appManager.getManageCatalog().displayComponentListIndexed(results);

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

    // --- Supplier Management ---

    // Supplier management menu
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

    // Search and display suppliers
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

        appManager.getManageCatalog().displaySupplierList(results);

        if (!results.isEmpty()) {
            System.out.println("-----------------------------------------");
        }

        pause();
    }

    // Edit supplier information
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

    // Remove supplier from system
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

        appManager.getManageCatalog().displaySupplierListIndexed(results);

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

    // --- Medical Area Management ---

    // Medical area management menu
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

    // Search and display medical areas
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

        appManager.getManageCatalog().displayAreaList(results);

        if (!results.isEmpty()) {
            System.out.println("-----------------------------------------");
        }

        pause();
    }

    // Edit medical area information
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

    // Remove medical area from system
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

        appManager.getManageCatalog().displayAreaListIndexed(results);

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

    // --- Order Management ---

    // Order management menu
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

    // Search and display orders
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

        appManager.getManageCatalog().displayOrderList(results);

        if (!results.isEmpty()) {
            System.out.println("-----------------------------------------");
        }

        pause();
    }

    // Mark order as delivered
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

    // List all pending orders
    private void listPendingOrders() {
        clearScreen();
        System.out.println("==========================================");
        System.out.println("            ENCOMENDAS PENDENTES          ");
        System.out.println("==========================================");
        System.out.println();

        ArrayList<Order> pendingOrders = appManager.getManageCatalog().listPendingOrders();

        System.out.println("Total de encomendas pendentes: " + pendingOrders.size());
        System.out.println();

        appManager.getManageCatalog().displayOrderList(pendingOrders);

        if (!pendingOrders.isEmpty()) {
            System.out.println("-----------------------------------------");
        }

        pause();
    }

    // Remove order from system
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

        appManager.getManageCatalog().displayOrderListIndexed(results);

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

    // --- Category Management ---

    // Category management menu
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

    // Create new category
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

    // Search and display categories
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

        appManager.getManageCatalog().displayCategoryList(results);

        if (!results.isEmpty()) {
            System.out.println("-----------------------------------------");
        }

        pause();
    }

    // Edit category information
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

    // Remove category from system
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

        appManager.getManageCatalog().displayCategoryListIndexed(results);

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

    // --- Utility Functions ---

    // Clear console screen
    private void clearScreen() {
        for (int i = 0; i < 50; i++) {
            System.out.println();
        }
    }

    // Display success message
    private void showSuccess(String message) {
        System.out.println();
        System.out.println("OK: " + message);
        System.out.println();
    }

    // Display error message
    private void showError(String message) {
        System.out.println();
        System.out.println("ERRO: " + message);
        System.out.println();
    }

    // Wait for user to press ENTER
    private void pause() {
        System.out.println();
        System.out.print("Pressione ENTER para continuar...");
        scanner.nextLine();
    }

    // Read and validate integer input
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
}
