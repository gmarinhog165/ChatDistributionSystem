package pt.uminho.di.config;

/**
 * Configuration constants for the Chat System.
 * Contains all port definitions and other configuration parameters.
 */
public class Config {
    // Main Chat Server port for gRPC
    public static final int SC_PORT = 6002;
    
    // SA port
    public static final int SA_PORT = 7000;
    // Reply socket port for SA communication
    public static final int REP_SOCK_PORT = 8000;
    // Server-to-server communication port
    public static final int SC_COMMS_PORT = 9000;
    // Thread pool sizes
    public static final int SA_THREAD_POOL_SIZE = 2;
    public static final int DISTRIBUTION_THREAD_POOL_SIZE = 2;
}