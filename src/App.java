import app.ApplicationManager;
import app.MenuManager;
import util.LogManager;
import util.SystemInfo;
import data.CredentialsManager;

public class App {
    public static void main(String[] args) {
        // File paths
        String dataFilePath = "dados_apl.dat";
        String logFilePath = "log.txt";
        String systemInfoPath = "info_sistema.dat";
        String credentialsPath = "credenciais_acesso.txt";
        
        // Initialize managers
        ApplicationManager appManager = new ApplicationManager(dataFilePath);
        LogManager logManager = new LogManager(logFilePath);
        SystemInfo systemInfo = new SystemInfo(systemInfoPath);
        CredentialsManager credentialsManager = new CredentialsManager(credentialsPath);
        
        // Display system info
        System.out.println("==========================================");
        System.out.println("   SISTEMA DE GESTAO LABORATORIAL");
        System.out.println("==========================================");
        System.out.println();
        
        // Increment execution count and show stats
        systemInfo.incrementExecutionCount();
        
        System.out.println("N de execucoes: " + systemInfo.getExecutionCount());
        if (!systemInfo.getLastUsername().isEmpty()) {
            System.out.println("ultimo utilizador: " + systemInfo.getLastUsername());
        }
        System.out.println();
        
        // Load data
        System.out.print("A carregar dados...");
        boolean dataLoaded = appManager.startup();
        
        if (dataLoaded) {
            System.out.println("Dados carregados com sucesso!");
        } else {
            System.out.println("Nenhum ficheiro de dados encontrado. A iniciar sistema novo.");
        }
        
        System.out.println();
        
        // Check if admin user needs to be created
        if (appManager.getManageUsers().listUsers().isEmpty()) {
            System.out.println("==========================================");
            System.out.println("AVISO: Nenhum utilizador encontrado!");
            System.out.println("E necessario criar um utilizador ADMINISTRADOR.");
            System.out.println("==========================================");
            System.out.println();
            System.out.print("Pressione ENTER para continuar...");
            try {
                System.in.read();
            } catch (Exception e) {
                logManager.log("SYSTEM", "Erro ao aguardar input: " + e.getMessage());
            }
        } else {
            System.out.print("Pressione ENTER para continuar...");
            try {
                System.in.read();
            } catch (Exception e) {
                logManager.log("SYSTEM", "Erro ao aguardar input: " + e.getMessage());
            }
        }
        
        // Create menu manager and show main menu
        MenuManager menuManager = new MenuManager(appManager, logManager, systemInfo);
        menuManager.showMainMenu();
        
        // Save data before exit
        System.out.println();
        System.out.print("A guardar dados...");
        
        if (appManager.shutdown()) {
            System.out.println("Dados guardados com sucesso!");
        } else {
            System.out.println("Erro ao guardar dados!");
        }
        
        // Save credentials
        credentialsManager.saveCredentials(appManager.getManageUsers().listUsers());
        
        System.out.println();
        System.out.println("Sistema encerrado.");
        
        logManager.log("SYSTEM", "Sistema encerrado");
    }
}
