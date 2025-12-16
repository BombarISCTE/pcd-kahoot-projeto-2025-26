package Server;

import Game.GameState;
import Utils.Constants;
import Utils.Records.*;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

public class Server {

    private static final int PORT = Constants.SERVER_PORT;
    private ServerSocket serverSocket;
    private final Map<Integer, GameState> games = new HashMap<>();
    private boolean isRunning = true;

    private final AtomicInteger gameIdCounter = new AtomicInteger(1);
    private final AtomicInteger playerIdCounter = new AtomicInteger(1);

    public Server() throws IOException {
        serverSocket = new ServerSocket(PORT);
    }

    public void startServer() {
        startTUI();

        try {
            while (isRunning && !serverSocket.isClosed()) {
                Socket socket = serverSocket.accept();
                System.out.println("Server: new client connected: " + socket.getInetAddress().getHostAddress());
                ClientHandler handler = new ClientHandler(socket, this);
                new Thread(handler).start();
            }
        } catch (IOException e) {
            System.err.println("Server: error accepting client - " + e.getMessage());
            closeServerSocket();
        }
    }

    // Inicia a TUI em thread separada
    public void startTUI() {
        new Thread(() -> {
            TUI tui = new TUI(this);
            try { tui.menu(); }
            catch (IOException e) { System.err.println("TUI error: " + e.getMessage()); }
        }).start();
    }

    // --- Métodos do Game Management ---

    public int createGameId() { return gameIdCounter.getAndIncrement(); }

    public int generatePlayerId() { return playerIdCounter.getAndIncrement(); }

    public void addGame(GameState game) { games.put(game.getGameCode(), game); }

    public void removeGame(int gameId) { games.remove(gameId); }

    public GameState getGame(int gameId) { return games.get(gameId); }

    public void listGames() {
        if (games.isEmpty()) {
            System.out.println("No active games.");
            return;
        }
        System.out.println("Active Games:");
        for (GameState g : games.values()) {
            System.out.println("Game " + g.getGameCode() + " - Teams: " + g.getTeams().size());
        }
    }

    // Cria novo ID de equipe baseado no número atual de equipes do jogo
    public synchronized int createTeamId(int gameId) {
        GameState game = games.get(gameId);
        if (game != null) return game.getTeams().size() + 1;
        return -1;
    }

    // Inicia o jogo: envia GameStarted e primeira pergunta
    public void startGame(int gameId) {
        GameState game = getGame(gameId);
        if (game == null) {
            System.out.println("Server startGame - No game with id: " + gameId);
            return;
        }

        System.out.println("Server startGame - Starting game " + gameId);

        // Envia mensagem GameStarted a todos os clientes do jogo
        for (ClientHandler ch : ClientHandler.clientHandlers) {
            if (ch.getGameId() == gameId) {
                ch.sendMessage(new GameStarted(gameId));
            }
        }

        // Envia a primeira pergunta
        for (ClientHandler ch : ClientHandler.clientHandlers) {
            if (ch.getGameId() == gameId) ch.sendNextQuestion();
        }
    }

    // Avança para a próxima pergunta
    public void nextQuestion(int gameId) {
        GameState game = getGame(gameId);
        if (game == null) return;
        game.startCurrentQuestion(); // inicializa temporizador/latch/barrier da pergunta atual
    }

    // Fecha o servidor
    public void closeServerSocket() {
        isRunning = false;
        try {
            if (serverSocket != null && !serverSocket.isClosed()) serverSocket.close();
        } catch (IOException e) { e.printStackTrace(); }
    }

    public boolean isRunning() { return isRunning; }

    // --- Main ---
    public static void main(String[] args) throws IOException {
        Server server = new Server();
        server.startServer();
    }
}
