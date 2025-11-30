package Client;

import Game.*;

import java.io.*;
import java.net.InetAddress;
import java.net.Socket;

public class Client {
    private Socket socket;
    private ObjectInputStream in;
    private ObjectOutputStream out;

    private final String IP;
    private final int port;
    private final Game game;
    private final Team team;
    private final String username;


    public Client (String IP , int port, Game game, Team team, String username) {
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
        } catch (IOException | ClassNotFoundException _){
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
    private void connectToServer() {
        try {
            InetAddress address = InetAddress.getByName(null);
            socket = new Socket(address, 8080);
            out = new ObjectOutputStream(socket.getOutputStream());
            out.flush();
            in = new ObjectInputStream(socket.getInputStream());
        }catch (IOException e) {
            System.err.println("Falha em estabelecer ligação ao servidor: " + IP + " - " + e.getMessage());
        }
    }

    // example message exchange: send a greeting and read one response
    private void sendMessages() throws IOException, ClassNotFoundException {
        if (out == null || in == null) {
            throw new IOException("I/O streams not initialized");
        }
        // send a simple handshake / registration
        out.writeObject("HELLO " + username);
        out.flush();
        // read one line response from server (non-blocking assumption depends on server)
//        Message response = (Message) in.readObject();
//        if (response != null) {
//            System.out.println("Server response: " + response);
//        } else {
//            System.err.println("Server closed connection or sent no response");
//        }

        // optional follow-up message
        out.writeObject("GOODBYE " + username);
        out.flush();
    }

    public void sendMessage(String message) throws IOException {
        if (out != null) {
            out.writeObject(message);
        } else {
            System.err.println("Output stream not initialized");
        }
    }
//    public Message readMessage() throws IOException, ClassNotFoundException {
//        if (in != null) {
//            return (Message) in.readObject();
//        } else {
//            throw new IOException("Input stream not initialized");
//        }
//    }
    public void disconnect() {
        try {
            in.close();
            out.close();
            socket.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }


}
