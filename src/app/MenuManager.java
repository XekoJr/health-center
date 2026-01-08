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

    public MenuManager(ApplicationManager appManager, LogManager logManager) {
        this.appManager = appManager;
        this.logManager = logManager;
        this.scanner = new Scanner(System.in);
    }

    // ==================== MAIN MENU ====================

    public void showMainMenu() {
        while (true) {
            clearScreen();
            System.out.println("+========================================+");
            System.out.println("|     SISTEMA DE GESTAO LABORATORIAL     |");
            System.out.println("+========================================+");
            System.out.println();

            // Check if no users exist - force admin creation
            ArrayList<User> users = appManager.getManageUsers().listUsers();
            if (users.isEmpty()) {
                System.out.println("AVISO: Nenhum utilizador no sistema!");
                System.out.println("       Deve criar um administrador.");
                System.out.println();
                System.out.println("1. Registar administrador");
                System.out.println("0. Sair");
                System.out.println();
                System.out.print("Escolha uma opcao: ");

                int choice = readInt();

                switch (choice) {
                    case 1:
                        handleRegister();
                        break;
                    case 0:
                        return;
                    default:
                        showError("Opcao invalida!");
                        pause();
                }
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
        System.out.println("+========================================+");
        System.out.println("|              LOGIN                     |");
        System.out.println("+========================================+");
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
        logManager.log(username, "Login realizado");

        clearScreen();
        System.out.println("+========================================+");
        System.out.println("|          BEM-VINDO                     |");
        System.out.println("+========================================+");
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
        System.out.println("+========================================+");
        System.out.println("|          REGISTO DE UTILIZADOR         |");
        System.out.println("+========================================+");
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

            System.out.println("+========================================+");
            System.out.println("|         MENU ADMINISTRADOR             |");
            System.out.println("+========================================+");
            System.out.println("Utilizador: " + currentUser.getName());
            System.out.println();
            System.out.println("1. Aprovar utilizadores pendentes");
            System.out.println("2. Listar todos os utilizadores");
            System.out.println("3. Pesquisar utilizadores");
            System.out.println("4. Gerir servicos");
            System.out.println("5. Listar analises");
            System.out.println("6. Listar encomendas");
            System.out.println("7. Exportar servicos para CSV");
            System.out.println("8. Consultar log do sistema");
            System.out.println("9. Editar meu perfil");
            System.out.println("0. Logout");
            System.out.println();
            System.out.print("Escolha uma opcao: ");

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
                case 4:
                    manageServices();
                    break;
                case 5:
                    listAnalyses();
                    break;
                case 6:
                    listOrders();
                    break;
                case 7:
                    exportServicesToCSV();
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

    private void approveUsers() {
        clearScreen();
        System.out.println("+========================================+");
        System.out.println("|      APROVAR UTILIZADORES PENDENTES    |");
        System.out.println("+========================================+");
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
        for (User user : pendingUsers) {
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
        System.out.println("+========================================+");
        System.out.println("|        LISTAR UTILIZADORES             |");
        System.out.println("+========================================+");
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

        for (User user : users) {
            System.out.println("-----------------------------------------");
            System.out.println("Username: " + user.getUsername());
            System.out.println("Nome: " + user.getName());
            System.out.println("Email: " + user.getEmail());
            System.out.println("Tipo: " + user.getType());
            System.out.println("Estado: " + user.getStatus());

            if (user instanceof Client) {
                Client c = (Client) user;
                System.out.println("NIF: " + c.getNif());
                System.out.println("Telefone: " + c.getPhone());
                System.out.println("Morada: " + c.getAddress());
            } else if (user instanceof Technician) {
                Technician t = (Technician) user;
                System.out.println("NIF: " + t.getNif());
                System.out.println("Telefone: " + t.getPhone());
                System.out.println("Morada: " + t.getAddress());
            }
        }

        System.out.println("-----------------------------------------");
        pause();
    }

    private void searchUsers() {
        clearScreen();
        System.out.println("+========================================+");
        System.out.println("|        PESQUISAR UTILIZADORES          |");
        System.out.println("+========================================+");
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

        for (User user : results) {
            System.out.println("-----------------------------------------");
            System.out.println("Username: " + user.getUsername());
            System.out.println("Nome: " + user.getName());
            System.out.println("Email: " + user.getEmail());
            System.out.println("Tipo: " + user.getType());
            System.out.println("Estado: " + user.getStatus());
        }

        if (!results.isEmpty()) {
            System.out.println("-----------------------------------------");
        }

        pause();
    }

    private void manageServices() {
        while (true) {
            clearScreen();
            System.out.println("+========================================+");
            System.out.println("|          GERIR SERVICOS                |");
            System.out.println("+========================================+");
            System.out.println();

            System.out.println("1. Aprovar pedidos de servico");
            System.out.println("2. Associar tecnico a servico");
            System.out.println("3. Listar todos os servicos");
            System.out.println("4. Listar servicos por estado");
            System.out.println("5. Pesquisar servicos");
            System.out.println("0. Voltar");
            System.out.print("Escolha: ");

            int choice = readInt();

            switch (choice) {
                case 1:
                    adminApproveRejectServices();
                    break;
                case 2:
                    approveRejectServices();
                    break;
                case 3:
                    listAllServices();
                    break;
                case 4:
                    listServicesByStatus();
                    break;
                case 5:
                    searchServices();
                    break;
                case 0:
                    return;
                default:
                    showError("Opcao invalida!");
                    pause();
            }
        }
    }

    private void listAllServices() {
        clearScreen();
        ArrayList<Service> services = appManager.getManageServices().listAllServices();

        System.out.println("Total de servicos: " + services.size());
        System.out.println();

        appManager.getManageServices().sortServicesByCode(true);

        for (Service service : services) {
            displayService(service);
        }

        pause();
    }

    private void listServicesByStatus() {
        clearScreen();
        System.out.println("+========================================+");
        System.out.println("|      LISTAR SERVICOS POR ESTADO        |");
        System.out.println("+========================================+");
        System.out.println();

        System.out.println("Estados disponiveis:");
        System.out.println("1. pending (Pendente)");
        System.out.println("2. approved (Aprovado)");
        System.out.println("3. rejected (Rejeitado)");
        System.out.println("4. in_progress (Em execucao)");
        System.out.println("5. completed (Terminado)");
        System.out.print("Escolha: ");

        int choice = readInt();
        String status = "";

        switch (choice) {
            case 1:
                status = "pending";
                break;
            case 2:
                status = "approved";
                break;
            case 3:
                status = "rejected";
                break;
            case 4:
                status = "in_progress";
                break;
            case 5:
                status = "completed";
                break;
            default:
                showError("Opcao invalida!");
                pause();
                return;
        }

        ArrayList<Service> services = appManager.getManageServices().listServicesByStatus(status);

        clearScreen();
        System.out.println("Servicos com estado '" + status + "': " + services.size());
        System.out.println();

        for (Service service : services) {
            displayService(service);
        }

        pause();
    }

    private void searchServices() {
        clearScreen();
        System.out.println("+========================================+");
        System.out.println("|        PESQUISAR SERVICOS              |");
        System.out.println("+========================================+");
        System.out.println();

        System.out.print("Termo de pesquisa (codigo ou descricao, vazio para listar todos): ");
        String term = scanner.nextLine().trim();

        ArrayList<Service> results;
        if (term.isEmpty()) {
            results = appManager.getManageServices().listAllServices();
        } else {
            results = appManager.getManageServices().searchServicesAdvanced(term);
        }

        clearScreen();
        System.out.println("Resultados encontrados: " + results.size());
        System.out.println();

        for (Service service : results) {
            displayService(service);
        }

        pause();
    }

    private void adminApproveRejectServices() {
        clearScreen();
        System.out.println("+========================================+");
        System.out.println("|   APROVAR/REJEITAR PEDIDOS SERVICO     |");
        System.out.println("+========================================+");
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
        for (Service service : pendingServices) {
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

    private void assignTechnicianToService() {
        clearScreen();
        System.out.println("+========================================+");
        System.out.println("|    ASSOCIAR TECNICO A SERVICO          |");
        System.out.println("+========================================+");
        System.out.println();

        System.out.print("Codigo do servico: ");
        int serviceCode = readInt();

        ArrayList<User> technicians = appManager.getManageUsers().listUsersByType("technician");

        if (technicians.isEmpty()) {
            showError("Nao existem tecnicos registados!");
            pause();
            return;
        }

        System.out.println();
        System.out.println("Tecnicos disponiveis:");

        int index = 1;
        for (User tech : technicians) {
            System.out.println(index + ". " + tech.getUsername() + " - " + tech.getName());
            index++;
        }

        System.out.print("Escolha o tecnico: ");
        int choice = readInt();

        if (choice < 1 || choice > technicians.size()) {
            showError("Escolha invalida!");
            pause();
            return;
        }

        Technician selectedTech = (Technician) technicians.get(choice - 1);

        if (appManager.getManageServices().assignTechnician((Admin) appManager.getSession().getCurrentUser(),
                serviceCode, selectedTech)) {
            showSuccess("Tecnico associado ao servico com sucesso!");
            logManager.log(appManager.getSession().getCurrentUser().getUsername(),
                    "Associou tecnico " + selectedTech.getUsername() + " ao servico " + serviceCode);
        } else {
            showError("Erro ao associar tecnico. Verifique se o codigo do servico esta correto.");
        }

        pause();
    }

    private void approveRejectServices() {
        clearScreen();
        System.out.println("+========================================+");
        System.out.println("|    ASSOCIAR TECNICO A SERVICO          |");
        System.out.println("+========================================+");
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
        for (Service service : pendingServices) {
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
        for (User tech : technicians) {
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

            System.out.println("+========================================+");
            System.out.println("|           MENU TECNICO                 |");
            System.out.println("+========================================+");
            System.out.println("Utilizador: " + currentUser.getName());
            System.out.println();
            System.out.println("1.  Aprovar/Rejeitar pedidos de servico");
            System.out.println("2.  Listar meus servicos");
            System.out.println("3.  Iniciar execucao de servico");
            System.out.println("4.  Finalizar servico");
            System.out.println("5.  Criar analise");
            System.out.println("6.  Listar analises");
            System.out.println("7.  Adicionar componente quimico");
            System.out.println("8.  Listar componentes quimicos");
            System.out.println("9.  Criar fornecedor");
            System.out.println("10. Criar area medica");
            System.out.println("11. Criar encomenda");
            System.out.println("12. Listar encomendas");
            System.out.println("13. Editar meu perfil");
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
                    startServiceExecution();
                    break;
                case 4:
                    finishService();
                    break;
                case 5:
                    createAnalysis();
                    break;
                case 6:
                    listAnalyses();
                    break;
                case 7:
                    addChemicalComponent();
                    break;
                case 8:
                    listChemicalComponents();
                    break;
                case 9:
                    createSupplier();
                    break;
                case 10:
                    createMedicalArea();
                    break;
                case 11:
                    createOrder();
                    break;
                case 12:
                    listOrders();
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
        System.out.println("+========================================+");
        System.out.println("|   APROVAR/REJEITAR PEDIDOS SERVICO     |");
        System.out.println("+========================================+");
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
        for (Service service : pendingServices) {
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

    private void listMyServices() {
        clearScreen();
        System.out.println("+========================================+");
        System.out.println("|         MEUS SERVICOS                  |");
        System.out.println("+========================================+");
        System.out.println();

        Technician currentTech = (Technician) appManager.getSession().getCurrentUser();
        ArrayList<Service> allServices = appManager.getManageServices().listAllServices();
        ArrayList<Service> myServices = new ArrayList<>();

        for (Service service : allServices) {
            if (service.getTechnician() != null &&
                    service.getTechnician().getUsername().equals(currentTech.getUsername())) {
                myServices.add(service);
            }
        }

        System.out.println("Total: " + myServices.size() + " servicos");
        System.out.println();

        for (Service service : myServices) {
            displayService(service);
        }

        pause();
    }

    private void startServiceExecution() {
        clearScreen();
        System.out.println("+========================================+");
        System.out.println("|    INICIAR EXECUCAO DE SERVICO         |");
        System.out.println("+========================================+");
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
        System.out.println("+========================================+");
        System.out.println("|        FINALIZAR SERVICO               |");
        System.out.println("+========================================+");
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
        System.out.println("+========================================+");
        System.out.println("|          CRIAR ANALISE                 |");
        System.out.println("+========================================+");
        System.out.println();

        int code = appManager.getManageCatalog().generateAnalysisCode();
        System.out.println("Codigo da analise (gerado): " + code);
        System.out.println();

        System.out.print("Nome: ");
        String name = scanner.nextLine().trim();

        System.out.print("Certificacao: ");
        String certification = scanner.nextLine().trim();

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

    private void listAnalyses() {
        clearScreen();
        System.out.println("+========================================+");
        System.out.println("|          LISTAR ANALISES               |");
        System.out.println("+========================================+");
        System.out.println();

        ArrayList<LabAnalysis> analyses = appManager.getManageCatalog().listAnalysis();

        System.out.println("Total de analises: " + analyses.size());
        System.out.println();

        appManager.getManageCatalog().sortAnalysisByCode(true);

        for (LabAnalysis analysis : analyses) {
            System.out.println("-----------------------------------------");
            System.out.println("Codigo: " + analysis.getCode());
            System.out.println("Nome: " + analysis.getName());
            System.out.println("Certificacao: " + analysis.getCertification());
            System.out.println("Metodos: " + analysis.getMethods());
            System.out.println("Fornecedores: " + analysis.getSuppliers().size());
            System.out.println("Areas medicas: " + analysis.getAreas().size());
            System.out.println("Componentes: " + analysis.getRequiredComponents().size());
        }

        if (!analyses.isEmpty()) {
            System.out.println("-----------------------------------------");
        }

        pause();
    }

    private void addChemicalComponent() {
        clearScreen();
        System.out.println("+========================================+");
        System.out.println("|      ADICIONAR COMPONENTE QUIMICO      |");
        System.out.println("+========================================+");
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

    private void listChemicalComponents() {
        clearScreen();
        System.out.println("+========================================+");
        System.out.println("|      COMPONENTES QUIMICOS              |");
        System.out.println("+========================================+");
        System.out.println();

        ArrayList<ChemicalComponent> components = appManager.getManageCatalog().listChemicalComponents();

        System.out.println("Total de componentes: " + components.size());
        System.out.println();

        for (ChemicalComponent comp : components) {
            System.out.println("-----------------------------------------");
            System.out.println("Codigo: " + comp.getCode());
            System.out.println("Nome: " + comp.getName());
            System.out.println("Alfa: " + comp.getAlphaValue());
            System.out.println("Beta: " + comp.getBetaValue());
            System.out.println("Stock: " + comp.getStockQty());
        }

        if (!components.isEmpty()) {
            System.out.println("-----------------------------------------");
        }

        pause();
    }

    private void createSupplier() {
        clearScreen();
        System.out.println("+========================================+");
        System.out.println("|         CRIAR FORNECEDOR               |");
        System.out.println("+========================================+");
        System.out.println();

        int code = appManager.getManageCatalog().generateSupplierCode();
        System.out.println("Codigo (gerado): " + code);
        System.out.println();

        System.out.print("Nome: ");
        String name = scanner.nextLine().trim();

        System.out.print("Email: ");
        String email = scanner.nextLine().trim();

        System.out.print("Telefone: ");
        String phone = scanner.nextLine().trim();

        Supplier supplier = new Supplier(code, name, email, phone);

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
        System.out.println("+========================================+");
        System.out.println("|        CRIAR AREA MEDICA               |");
        System.out.println("+========================================+");
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
        System.out.println("+========================================+");
        System.out.println("|         CRIAR ENCOMENDA                |");
        System.out.println("+========================================+");
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
        for (Supplier supplier : suppliers) {
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

        System.out.print("Data do pedido (DD-MM-YYYY): ");
        String requestDate = scanner.nextLine().trim();

        Technician currentTech = (Technician) appManager.getSession().getCurrentUser();
        Order order = new Order(code, selectedSupplier, currentTech, requestDate);

        if (appManager.getManageCatalog().createOrder(currentTech, selectedSupplier, order)) {
            showSuccess("Encomenda criada com sucesso!");
            logManager.log(currentTech.getUsername(), "Criou encomenda " + code);
        } else {
            showError("Erro ao criar encomenda.");
        }

        pause();
    }

    private void listOrders() {
        clearScreen();
        System.out.println("+========================================+");
        System.out.println("|         LISTAR ENCOMENDAS              |");
        System.out.println("+========================================+");
        System.out.println();

        ArrayList<Order> orders = appManager.getManageCatalog().listOrders();

        System.out.println("Total de encomendas: " + orders.size());
        System.out.println();

        appManager.getManageCatalog().sortOrdersByCode(true);

        for (Order order : orders) {
            System.out.println("-----------------------------------------");
            System.out.println("Codigo: " + order.getCode());
            System.out.println("Fornecedor: " + order.getSupplier().getName());
            System.out.println("Tecnico: " + order.getTechnician().getName());
            System.out.println("Data pedido: " + order.getRequestDate());
            System.out.println(
                    "Data entrega: " + (order.getDeliveryDate().isEmpty() ? "Pendente" : order.getDeliveryDate()));
            System.out.println("Estado: " + order.getStatus());
        }

        if (!orders.isEmpty()) {
            System.out.println("-----------------------------------------");
        }

        pause();
    }

    // ==================== CLIENT MENU ====================

    private void showClientMenu() {
        while (appManager.getSession().isLoggedIn()) {
            clearScreen();
            User currentUser = appManager.getSession().getCurrentUser();

            System.out.println("+========================================+");
            System.out.println("|           MENU CLIENTE                 |");
            System.out.println("+========================================+");
            System.out.println("Utilizador: " + currentUser.getName());
            System.out.println();
            System.out.println("1. Pedir novo servico");
            System.out.println("2. Listar meus servicos");
            System.out.println("3. Pesquisar meus servicos");
            System.out.println("4. Editar meu perfil");
            System.out.println("0. Logout");
            System.out.println();
            System.out.print("Escolha uma opcao: ");

            int choice = readInt();

            switch (choice) {
                case 1:
                    requestNewService();
                    break;
                case 2:
                    listMyServicesAsClient();
                    break;
                case 3:
                    searchMyServicesAsClient();
                    break;
                case 4:
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
        System.out.println("+========================================+");
        System.out.println("|        PEDIR NOVO SERVICO              |");
        System.out.println("+========================================+");
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

        System.out.print("Data do pedido (DD-MM-YYYY): ");
        String requestDate = scanner.nextLine().trim();

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

    private void listMyServicesAsClient() {
        clearScreen();
        System.out.println("+========================================+");
        System.out.println("|         MEUS SERVICOS                  |");
        System.out.println("+========================================+");
        System.out.println();

        Client currentClient = (Client) appManager.getSession().getCurrentUser();
        ArrayList<Service> myServices = appManager.getManageServices().listServicesByClient(currentClient);

        System.out.println("Total: " + myServices.size() + " servicos");
        System.out.println();

        for (Service service : myServices) {
            displayService(service);
        }

        pause();
    }

    private void searchMyServicesAsClient() {
        clearScreen();
        System.out.println("+========================================+");
        System.out.println("|      PESQUISAR MEUS SERVICOS           |");
        System.out.println("+========================================+");
        System.out.println();

        System.out.print("Termo de pesquisa (vazio para listar todos): ");
        String term = scanner.nextLine().trim();

        Client currentClient = (Client) appManager.getSession().getCurrentUser();
        ArrayList<Service> allMyServices = appManager.getManageServices().listServicesByClient(currentClient);
        ArrayList<Service> results = new ArrayList<>();

        if (term.isEmpty()) {
            results = allMyServices;
        } else {
            for (Service service : allMyServices) {
                if (String.valueOf(service.getCode()).contains(term) ||
                        service.getDescription().toLowerCase().contains(term.toLowerCase())) {
                    results.add(service);
                }
            }
        }

        clearScreen();
        System.out.println("Resultados encontrados: " + results.size());
        System.out.println();

        for (Service service : results) {
            displayService(service);
        }

        pause();
    }

    // ==================== COMMON FUNCTIONS ====================

    private void editMyProfile() {
        clearScreen();
        System.out.println("+========================================+");
        System.out.println("|         EDITAR MEU PERFIL              |");
        System.out.println("+========================================+");
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
        System.out.println("+========================================+");
        System.out.println("|         LOG DO SISTEMA                 |");
        System.out.println("+========================================+");
        System.out.println();

        ArrayList<String> logs = logManager.readLog();

        if (logs.isEmpty()) {
            System.out.println("Nao existem registos no log.");
        } else {
            System.out.println("ultimas " + Math.min(50, logs.size()) + " entradas:");
            System.out.println();

            // Show last 50 entries (most recent first)
            int count = 0;
            for (int i = logs.size() - 1; i >= 0 && count < 50; i--) {
                System.out.println(logs.get(i));
                count++;
            }
        }

        pause();
    }

    private void exportServicesToCSV() {
        clearScreen();
        System.out.println("+========================================+");
        System.out.println("|      EXPORTAR SERVICOS PARA CSV        |");
        System.out.println("+========================================+");
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

    private void displayService(Service service) {
        System.out.println("-----------------------------------------");
        System.out.println("Codigo: " + service.getCode());
        System.out.println("Cliente: " + service.getClient().getName());
        System.out.println("Descricao: " + service.getDescription());
        System.out.println("Estado: " + service.getStatus());
        System.out.println("Data pedido: " + service.getRequestDate());

        if (service.getTechnician() != null) {
            System.out.println("Tecnico: " + service.getTechnician().getName());
        }

        if (!service.getFinishDate().isEmpty()) {
            System.out.println("Data conclusao: " + service.getFinishDate());
        }

        System.out.println("Valor total: " + service.getTotalValue() + " euros");
        System.out.println("Analises: " + service.getAnalyses().size());
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
}
