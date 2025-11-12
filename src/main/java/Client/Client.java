//package Client;
//
//import Game.*;
//
//import java.io.BufferedReader;
//import java.io.IOException;
//import java.io.InputStreamReader;
//import java.io.PrintWriter;
//import java.net.Socket;
//
//public class Client {
//    private Socket socket;
//    private BufferedReader in;
//    private PrintWriter out;
//
//    private final String username;
//    private final String address;
//    private final int port;
//
//
////    //usado para entrar no jogo ?
////    public Client(int gameID, Team team, int ClientId, String address, int port) {
////
////    }
//
//    //usado na main clienteKahoot
//    public Client (int IP , int Port, Game game, Team team, String username){
//        this.username = username;
//
//
//    }
//
//    public void runClient(){ //copiado dos slides do ano passado
//        try{
//            connectToServer();
//            sendMessages();
//        } catch (IOException _){
//            System.err.println("Failed to run client");
//        } finally {
//            try {
//                socket.close();
//            } catch (IOException close ){
//                System.err.println("Failed to close socket" + close.getMessage());
//            }
//        }
//    }
//    // establish connection and I/O
//    private void connectToServer() throws IOException {
//        socket = new Socket(address, port);
//        in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
//        out = new PrintWriter(socket.getOutputStream(), true);
//    }
//
//    // example message exchange: send a greeting and read one response
//    private void sendMessages() throws IOException {
//        if (out == null || in == null) {
//            throw new IOException("I/O streams not initialized");
//        }
//        // send a simple handshake / registration
//        out.println("HELLO " + username);
//        // read one line response from server (non-blocking assumption depends on server)
//        String response = in.readLine();
//        if (response != null) {
//            System.out.println("Server response: " + response);
//        } else {
//            System.err.println("Server closed connection or sent no response");
//        }
//
//        // optional follow-up message
//        out.println("GOODBYE " + username);
//    }
//
//
//
////    public static void main( String[] args){
////        new Client().runClient();
////    }
//
//}
