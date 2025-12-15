package Server;


import Game.GameState;
import Utils.Constants;

import java.io.*;
import java.net.*;

import java.util.ArrayList;

public class Server {
    private static final int PORT = Constants.SERVER_PORT;
    private ServerSocket serverSocket;
    private ArrayList<GameState> gameList = new ArrayList<>();


    public Server() throws IOException {
        serverSocket = new ServerSocket(PORT);
    }


    public void startServer() {

        try {

            while (!serverSocket.isClosed()) {
                Socket socket = serverSocket.accept();
                System.out.println("Server - New client connected: " + socket.getInetAddress().getHostAddress());
                ClientHandler clientHandler = new ClientHandler(socket);

                Thread thread = new Thread(clientHandler);
                thread.start();
            }
        } catch (IOException e) {
            System.out.println("Erro ao iniciar o servidor: " + e.getMessage());
            e.printStackTrace();
        }
    }

    public void closeServerSocket() {
        try {
            if (serverSocket != null){
                serverSocket.close();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public synchronized GameState getGame(int codigoJogo) {
        for (GameState game : gameList) {
            if (game.getGameCode() == codigoJogo) {
                return game;
            }
        }
        System.out.println("Jogo com codigo " + codigoJogo + " nao encontrado.");
        return null;
    }

    public synchronized void createGame(GameState game) {
        int codigoJogo = gameList.size() + +1;
        gameList.add(game);

    }

    public static void main(String[] args) throws IOException {
        Server server = new Server();
        server.startServer();
    }

}
