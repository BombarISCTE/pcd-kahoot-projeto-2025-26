package Server;


import Game.GameState;
import Utils.Constants;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

public class Server {
    private static final int PORT = Constants.SERVER_PORT;
    private ServerSocket serverSocket;
    private final Map<Integer, GameState> games = new HashMap<>(); //gameId -> GameState
    private boolean isRunning;
    private static final AtomicInteger playerIdGenerator = new AtomicInteger(1);


    public Server() throws IOException {
        serverSocket = new ServerSocket(PORT);
        isRunning = true;

    }


    public void startServer() {
        startTUI(); // inicia a TUI numa thread separada

        while (!serverSocket.isClosed() && isRunning) {
            try {
                Socket socket = serverSocket.accept();
                System.out.println("Server - New client connected: " +
                        socket.getInetAddress().getHostAddress());

                try {
                    ClientHandler clientHandler = new ClientHandler(socket, this);
                    new Thread(clientHandler).start();
                } catch (IOException e) {
                    System.out.println("Server - Falha ao criar ClientHandler para cliente");
                    socket.close();
                }

            } catch (IOException e) {
                if (isRunning) {
                    System.out.println("Server - Erro ao aceitar cliente: " + e.getMessage());
                }
                break;
            }
        }

        closeServerSocket();
    }


    public void startTUI() {
        Thread tuiThread = new Thread(() -> {
            TUI tui = new TUI(this);
            try {
                tui.menu();
            } catch (IOException e) {
                System.out.println("S startTUI - Erro na TUI: " + e.getMessage());
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

    public synchronized boolean isRunning(){return isRunning;}

    public synchronized GameState getGame(int gameId) {return games.get(gameId);}

    public synchronized void addGame(GameState game) {games.put(game.getGameCode(), game);}

    public synchronized void removeGame(int gameId) {games.remove(gameId);}

    public synchronized int createGameId(){return games.size() + Constants.GAMEID_GENERATOR;}

    public synchronized int createTeamId(int gameId){
        GameState game = games.get(gameId);
        if (game != null) {
            return game.getTeams().size() + 1;
        }
        return -1;
    }


    public synchronized void listGames() {
        if (games.isEmpty()) {
            System.out.println("S listGames - No active games.");
        } else {
            System.out.println("S listGames -Active games:");
            for (GameState game : games.values()) {
                System.out.println(game);
            }
        }
    }

    public void questionTimeout(){} //todo



    public static void main(String[] args) throws IOException {
        Server server = new Server();
        server.startServer();
    }



    public int generatePlayerId() {return playerIdGenerator.getAndIncrement();}

}
