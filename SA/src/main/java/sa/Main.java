package sa;

import sa.overlay.CyclonLauncher;

import java.io.File;
import java.io.IOException;

public class Main {
    public static void main(String[] args) throws IOException {

        if (args.length != 2) {
            System.out.println("Usage: java Main <neighbours_filename> <ip:port>");
            return;
        }
        File config = new File(args[0]);
        if (!config.exists() || !config.isFile()) {
            System.out.println("Configuration file not found: " + args[0]);
            return;
        }
        CyclonLauncher launcher = new CyclonLauncher(args[0], args[1]);
        launcher.launch();
    }
}

