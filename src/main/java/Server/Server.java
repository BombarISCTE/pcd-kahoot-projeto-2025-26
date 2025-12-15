package Server;


import Game.GameState;
import Game.Pergunta;
import Utils.Constants;

import java.io.*;
import java.net.*;

import java.util.ArrayList;
import java.util.Scanner;

public class Server {
    private static final int PORT = Constants.SERVER_PORT;
    private ServerSocket serverSocket;
    private ArrayList<GameState> gameList = new ArrayList<>();
    private boolean isRunning;


    public Server() throws IOException {
        serverSocket = new ServerSocket(PORT);
        isRunning = true;

    }


    public void startServer() {
        startTUI(); // iniciar a TUI numa thread separada

        try {
            while (!serverSocket.isClosed() && isRunning) {
                Socket socket = serverSocket.accept();
                System.out.println("Server - New client connected: " + socket.getInetAddress().getHostAddress());
                ClientHandler clientHandler = new ClientHandler(socket);


                Thread thread = new Thread(clientHandler);
                thread.start();
            }
        } catch (IOException e) {
            System.out.println("Erro ao iniciar o servidor: " + e.getMessage());
            closeServerSocket();
            e.printStackTrace();

        }
    }

    public void startTUI() {
        Thread tuiThread = new Thread(() -> {
            TUI tui = new TUI(this);
            try {
                tui.menuConsola();
            } catch (IOException e) {
                System.out.println("Erro na TUI: " + e.getMessage());
            }
        });
        tuiThread.start();
    }

    public void closeServerSocket() {
        isRunning = false;
        try {
            if (serverSocket != null && !serverSocket.isClosed()) {
                serverSocket.close();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public synchronized boolean isRunning(){
        return isRunning;
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

    public synchronized void addGame(GameState game) {
        if(!gameList.contains(game)) {
            int gameId = gameList.size() + +1;
            gameList.add(game);
        }
    }

    public synchronized void removeGame(int gameId) {
        gameList.removeIf(game -> game.getGameCode() == gameId);
    }

    public synchronized int createGameId(){
        return gameList.size() + 1;
    }

    public synchronized void listGames() {
        if (gameList.isEmpty()) {
            System.out.println("No active games.");
        } else {
            System.out.println("Active games:");
            for (GameState game : gameList) {
                System.out.println(game);
            }
        }
    }



    public static void main(String[] args) throws IOException {
        Server server = new Server();
        server.startServer();
    }

}
