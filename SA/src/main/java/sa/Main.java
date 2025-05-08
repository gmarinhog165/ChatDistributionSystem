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
            System.out.println("Usage: java Main <neighbours_filename> <ip>");
            return;
        }
        File config = new File(args[0]);
        if (!config.exists() || !config.isFile()) {
            System.out.println("Configuration file not found: " + args[0]);
            return;
        }

        String selfIPaddr = args[1];

        CyclonLauncher launcher = new CyclonLauncher(args[0],selfIPaddr);
        
        //esta classe tem o metodo que retorna os vizinhos (thread safe)
        CyclonPeer peer = launcher.launch();

        Aggregator aggregator = new Aggregator(peer);

        ClientRequestHandler clientHandler = new ClientRequestHandler(selfIPaddr,peer,aggregator);
        GossipRequestHandler gossipHandler = new GossipRequestHandler(selfIPaddr,peer,aggregator);

        new Thread(clientHandler).start();
        new Thread(gossipHandler).start();

    }
}

