package network_game;

import java.io.*;
import java.net.*;
import java.util.function.Consumer;

public class NetworkClient {

    private Socket socket;
    private PrintWriter out;
    private BufferedReader in;

    public NetworkClient(String host, int port,
                         Consumer<String> onMessage) throws Exception {

        socket = new Socket(host, port);
        out = new PrintWriter(socket.getOutputStream(), true);
        in = new BufferedReader(new InputStreamReader(socket.getInputStream()));

        new Thread(() -> {
            try {
                String line;
                while ((line = in.readLine()) != null) {
                    onMessage.accept(line);
                }
            } catch (Exception e) {}
        }).start();
    }

    public void send(String msg) {
        out.println(msg);
    }
}
