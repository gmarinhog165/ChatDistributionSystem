import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

/**
 * Classe principal para teste de clientes SP
 */
public class TestSPClientMain {
    public static void main(String[] args) {
        try {
            // Parâmetros padrão
            String host = "localhost";
            int port = 8000; // Porta padrão para o primeiro nó
            
            // Verifica argumentos da linha de comando
            if (args.length >= 1) {
                host = args[0];
            }
            if (args.length >= 2) {
                port = Integer.parseInt(args[1]);
            }
            
            // Menu de opções
            Scanner scanner = new Scanner(System.in);
            System.out.println("=== SP Client Test Tool ===");
            System.out.println("1. Run interactive test");
            System.out.println("2. Run automated tests");
            System.out.println("3. Run stress test");
            System.out.println("4. Run shell mode");
            System.out.print("Escolha uma opção: ");
            
            String option = scanner.nextLine();
            
            switch (option) {
                case "1":
                    runInteractiveTest(host, port, scanner);
                    break;
                case "2":
                    runAutomatedTests(host, port);
                    break;
                case "3":
                    runStressTest(host, port, scanner);
                    break;
                case "4":
                    runShellMode(host, port, scanner);
                    break;
                default:
                    System.out.println("Opção inválida.");
            }
            
            scanner.close();
            
        } catch (Exception e) {
            System.err.println("Erro: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    /**
     * Executa o teste interativo com menu
     */
    private static void runInteractiveTest(String host, int port, Scanner scanner) throws IOException {
        System.out.println("\nIniciando teste interativo (servidor: " + host + ":" + port + ")");
        System.out.println("--------------------------------------");
        
        TestSPClient client = new TestSPClient(host, port);
        
        boolean running = true;
        while (running) {
            System.out.println("\nEscolha uma opção:");
            System.out.println("1. Listar todos os tópicos");
            System.out.println("2. Obter servidores para um tópico");
            System.out.println("3. Registrar um novo tópico");
            System.out.println("4. Executar testes automatizados");
            System.out.println("5. Sair");
            System.out.print("> ");
            
            String option = scanner.nextLine();
            
            try {
                switch (option) {
                    case "1":
                        List<String> topics = client.getTopics();
                        System.out.println("Tópicos disponíveis: " + topics);
                        break;
                        
                    case "2":
                        System.out.print("Digite o nome do tópico: ");
                        String topic = scanner.nextLine();
                        List<TestSPClient.ServerAddress> servers = client.getServersForTopic(topic);
                        System.out.println("Servidores para tópico '" + topic + "': " + servers);
                        break;
                        
                    case "3":
                        System.out.print("Digite o nome do novo tópico: ");
                        String newTopic = scanner.nextLine();
                        
                        System.out.print("Digite os servidores no formato 'ip1:porta1,ip2:porta2,...': ");
                        String serversStr = scanner.nextLine();
                        
                        boolean success = client.registerTopic(newTopic, serversStr);
                        if (success) {
                            System.out.println("Tópico registrado com sucesso!");
                        } else {
                            System.out.println("Falha ao registrar tópico.");
                        }
                        break;
                        
                    case "4":
                        client.runAutomatedTests();
                        break;
                        
                    case "5":
                        running = false;
                        break;
                        
                    default:
                        System.out.println("Opção inválida. Tente novamente.");
                }
            } catch (Exception e) {
                System.err.println("Erro: " + e.getMessage());
                e.printStackTrace();
            }
        }
        
        client.close();
        System.out.println("Teste interativo concluído.");
    }
    
    /**
     * Executa testes automatizados
     */
    private static void runAutomatedTests(String host, int port) throws IOException {
        System.out.println("\nIniciando testes automatizados (servidor: " + host + ":" + port + ")");
        TestSPClient client = new TestSPClient(host, port);
        client.runAutomatedTests();
        client.close();
    }
    
    /**
     * Executa teste de estresse
     */
    private static void runStressTest(String host, int port, Scanner scanner) throws IOException {
        System.out.println("\nIniciando teste de estresse (servidor: " + host + ":" + port + ")");
        
        System.out.print("Número de tópicos a registrar: ");
        int numTopics = Integer.parseInt(scanner.nextLine());
        
        System.out.print("Número de servidores por tópico: ");
        int numServers = Integer.parseInt(scanner.nextLine());
        
        TestSPClient client = new TestSPClient(host, port, false);
        
        System.out.println("Registrando " + numTopics + " tópicos com " + numServers + " servidores cada...");
        long startTime = System.currentTimeMillis();
        
        for (int i = 0; i < numTopics; i++) {
            String topic = "topic_" + i;
            
            List<TestSPClient.ServerAddress> servers = new ArrayList<>();
            for (int j = 0; j < numServers; j++) {
                servers.add(new TestSPClient.ServerAddress("192.168.1." + j, 5000 + j));
            }
            
            client.registerTopic(topic, servers);
            
            if (i % 10 == 0) {
                System.out.print(".");
                System.out.flush();
            }
        }
        
        long endTime = System.currentTimeMillis();
        System.out.println("\nTempo total: " + (endTime - startTime) + "ms");
        
        // Verifica quantos tópicos foram registrados
        List<String> topics = client.getTopics();
        System.out.println("Total de tópicos registrados: " + topics.size());
        
        client.close();
    }
    
    /**
     * Executa modo shell - similar ao netcat mas com historico e ajuda
     */
    private static void runShellMode(String host, int port, Scanner scanner) throws IOException {
        System.out.println("\nModo shell iniciado (servidor: " + host + ":" + port + ")");
        System.out.println("Digite comandos diretamente ou 'help' para ajuda, 'exit' para sair");
        
        TestSPClient client = new TestSPClient(host, port);
        
        boolean running = true;
        while (running) {
            System.out.print("nc> ");
            String command = scanner.nextLine();
            
            if ("exit".equalsIgnoreCase(command) || "quit".equalsIgnoreCase(command)) {
                running = false;
            } else if ("help".equalsIgnoreCase(command)) {
                System.out.println("Comandos disponíveis:");
                System.out.println("  GET_TOPICS - Lista todos os tópicos");
                System.out.println("  GET_SCS:<topic> - Lista servidores para um tópico");
                System.out.println("  REGISTER_TOPIC:<topic>:<ip1>:<port1>,<ip2>:<port2>,... - Registra um tópico");
                System.out.println("  exit/quit - Sai do modo shell");
            } else {
                try {
                    String response = client.sendCommand(command);
                    System.out.println(response);
                } catch (Exception e) {
                    System.err.println("Erro: " + e.getMessage());
                }
            }
        }
        
        client.close();
        System.out.println("Modo shell encerrado.");
    }
}