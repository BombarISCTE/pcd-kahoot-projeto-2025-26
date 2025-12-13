package Server;

import Game.Game;

import java.io.*;
import java.net.*;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.concurrent.Semaphore;

public class Server {
    private ArrayList<Game> listOfGames;
    private static final int PORT = 8080;
    private static final String IP = "localhost";
    private HashMap<String, Game> activeGames;
    private Semaphore semaphore; //todo posso usar ou tenho de criar uma classe semaforo?

    public Server() {
        listOfGames = new ArrayList<>();
        activeGames = new HashMap<>();
        semaphore = new Semaphore(5);
    }

    public void startServer() {
        try(ServerSocket serverSocket = new ServerSocket(PORT)) {
            System.out.println("Server started on port " + PORT);
            while (true) {
                Socket socket = serverSocket.accept();
                System.out.println("Server - New client connected: " + socket.getInetAddress().getHostAddress());
                // Handle client connection in a new thread

//                new Thread(() -> handleClient(clientSocket)).start();

                DealWithClient handler = new DealWithClient(socket, );
                handler.start();
            }
        } catch (IOException e) {
            System.err.println("Erro no servidor: " + e.getMessage());
            e.printStackTrace();
        }
    }
//
//    private void handleClient(Socket clientSocket) { //todo
//        try {
//            semaphore.acquire();
//            BufferedReader in = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
//            PrintWriter out = new PrintWriter(clientSocket.getOutputStream(), true);
//
//            // Exemplo: lê username do cliente
//            String username = in.readLine();
//            System.out.println("Cliente: " + username);
//            out.println("Bem-vindo, " + username + "!");
//
//            // TODO: lógica do jogo
//
//            //in.close();
//            //out.close();
//            clientSocket.close(); //  --> geralmente fechar o socket fecha os streams associados
//
//        } catch (IOException | InterruptedException e) {
//            e.printStackTrace();
//        } finally {
//            semaphore.release();
//        }
//    }

//
//    public void addGame(Game game) {
//        this.listOfGames.add(game);
//    }
//
//    private String gameStatistics() {
//        return "Not implemented yet"; //todo
//    }
//
//    private void nextQuestion() {
//        //todo
//    }


}
