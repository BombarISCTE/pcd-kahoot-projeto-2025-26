package Server;


import Game.GameState;
import Game.Pergunta;
import Messages.SendQuestion;
import Utils.Constants;

import java.io.*;
import java.net.*;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Scanner;

public class Server {
    private static final int PORT = Constants.SERVER_PORT;
    private ServerSocket serverSocket;
    private ArrayList<GameState> gameList = new ArrayList<>();
    private final Map<Integer, GameEngine> gameEngines = new HashMap<>();


    public Server() throws IOException {
        serverSocket = new ServerSocket(PORT);

    }


    public void startServer() {
        startTUI();

        try {
            while (!serverSocket.isClosed()) {
                Socket socket = serverSocket.accept();
                System.out.println("Server - New client connected: " + socket.getInetAddress().getHostAddress());
                ClientHandler clientHandler = new ClientHandler(socket, this);

                Thread thread = new Thread(clientHandler);
                thread.start();
            }
        } catch (IOException e) {
            System.out.println("Erro ao iniciar o servidor: " + e.getMessage());
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

    public synchronized void addGame(GameState game, Pergunta[] perguntas) {
        GameEngine gameEngine = new GameEngine(this, game, perguntas);
        gameList.add(game);
        gameEngines.put(game.getGameCode(), gameEngine);
        new Thread(gameEngine).start();
    }

    public synchronized void removeGame(int gameId) {
        gameList.removeIf(game -> game.getGameCode() == gameId);
    }

    public synchronized int createGameId(){
        return gameList.size() + 1;
    }

    public void listGames() {
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

    public void broadcastToGame(int gameCode, Object mensagem) {
        synchronized (ClientHandler.clientHandlers) {
            for (ClientHandler clientHandler : ClientHandler.clientHandlers) {
                if (clientHandler.getGameId() == gameCode){
                    clientHandler.sendMessage((Serializable) mensagem);
                }
            }
        }
    }

    public synchronized GameEngine getGameEngine(int gameCode) {
        return gameEngines.get(gameCode);
    }

}
