/*
c(sp_server).
c(sp_tcp_listener).

sp_server:start_link().
sp_server:register_topic("esportes", [{"127.0.0.1", 9001}, {"127.0.0.1", 9002}]).
sp_server:register_topic("música", [{"127.0.0.1", 9003}]).

sp_tcp_listener:start(8080).
*/
// TestSPClient.java

import java.io.*;
import java.net.*;

public class TestSPClient {
    public static void main(String[] args) throws Exception {
        Socket socket = new Socket("localhost", 8080);
        BufferedWriter out = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream()));
        BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()));

        out.write("GET_TOPICS\n");
        out.flush();

        String response = in.readLine();
        System.out.println("Response: " + response);

        out.write("GET_SCS:esportes\n");
        out.flush();
        String response2 = in.readLine();
        System.out.println("Response: " + response2);

        socket.close();
    }
}
