package sa;

import sa.clients.ClientRequestHandler;
import sa.config.Config;
import sa.gossip.Aggregator;
import sa.gossip.GossipRequestHandler;
import sa.overlay.CyclonLauncher;
import sa.overlay.CyclonPeer;
import sa.sc.SCInfo;

import java.io.File;
import java.io.IOException;

public class Main {
    public static void main(String[] args) throws IOException {

       
        if (args.length != 2) {
            System.out.println("Usage: java Main <neighbours_filename> <myport>");
            return;
        }
        File config = new File(args[0]);
        if (!config.exists() || !config.isFile()) {
            System.out.println("Configuration file not found: " + args[0]);
            return;
        }

        Integer port = Integer.parseInt(args[1]);

        CyclonLauncher launcher = new CyclonLauncher(args[0],port);
        
        //esta classe tem o metodo que retorna os vizinhos (thread safe)
        CyclonPeer peer = launcher.launch();

        Aggregator aggregator = new Aggregator(peer);

        //a porta para interaões com clientes é port-1
        ClientRequestHandler clientHandler = new ClientRequestHandler(port-1,peer,aggregator);
        //a porta para o gossip é port-2
        GossipRequestHandler gossipHandler = new GossipRequestHandler(port-2,peer);

        new Thread(clientHandler).start();
        new Thread(gossipHandler).start();

    }
}

