package Client;

import Game.*;
import Server.Server;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.InetAddress;
import java.net.Socket;

public class Client {
    private Socket socket;
    private BufferedReader in;
    private PrintWriter out;

    private final String IP;
    private final int port;
    private final Game game;
    private final Team team;
    private final String username;




    //usado na main clienteKahoot
    public Client (String IP , int port, Game game, Team team, String username) throws IOException {
        this.IP = IP;
        this.port = port;
        this.game = game;
        this.team = team;
        this.username = username;

        connectToServer();


    }

    public void runClient(){ //copiado dos slides do ano passado
        try{
            connectToServer();
            sendMessages();
        } catch (IOException _){
            System.err.println("Failed to run client");
        } finally {
            try {
                socket.close();
            } catch (IOException close ){
                System.err.println("Failed to close socket" + close.getMessage());
            }
        }
    }

    // establish connection and I/O
    private void connectToServer() throws IOException {
        InetAddress address = InetAddress.getByName(IP);
        socket = new Socket(address, port);
        in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
        out = new PrintWriter(socket.getOutputStream(), true);
    }

    // example message exchange: send a greeting and read one response
    private void sendMessages() throws IOException {
        if (out == null || in == null) {
            throw new IOException("I/O streams not initialized");
        }
        // send a simple handshake / registration
        out.println("HELLO " + username);
        // read one line response from server (non-blocking assumption depends on server)
        String response = in.readLine();
        if (response != null) {
            System.out.println("Server response: " + response);
        } else {
            System.err.println("Server closed connection or sent no response");
        }

//        // optional follow-up message
//        out.println("GOODBYE " + username);
    }

    public void sendMessage(String message) {
        if (out != null) {
            out.println(message);
        } else {
            System.err.println("Output stream not initialized");
        }
    }
    public String readMessage() throws IOException {
        if (in != null) {
            return in.readLine();
        } else {
            throw new IOException("Input stream not initialized");
        }
    }
    public void disconnect() {
        try {
            in.close();
            out.close();
            socket.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }



//    public static void main( String[] args){
//        new Client().runClient();
//    }

}
