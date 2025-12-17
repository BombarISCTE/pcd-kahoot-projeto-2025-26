package Server;

import Game.*;
import Utils.Constants;
import Utils.Records.*;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

public class Server {

    private ServerSocket serverSocket;
    private final Map<Integer, GameState> games = new HashMap<>();
    private boolean isRunning = true;

    private final AtomicInteger gameIdCounter = new AtomicInteger(1);
    private final AtomicInteger playerIdCounter = new AtomicInteger(1);

    public Server() throws IOException {
        serverSocket = new ServerSocket(Constants.SERVER_PORT);

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
        } catch (Exception e) {
            System.err.println("Server: unexpected error - " + e.getMessage());
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

    public synchronized void addGame(GameState game) { games.put(game.getGameCode(), game); }

    public synchronized void removeGame(int gameId) { games.remove(gameId); }

    public synchronized GameState getGame(int gameId) { return games.get(gameId); }

    public synchronized void addTeam(int gameId, int teamId) {
        GameState game = games.get(gameId);
        if (game == null) {
            throw new IllegalArgumentException("No game with code " + gameId);
        }
        game.addTeam(teamId);
    }

    public synchronized void listGames() {
        if (games.isEmpty()) {
            System.out.println("No active games.");
            return;
        }
        System.out.println("Active Games:");
        for (GameState g : games.values()) {
            System.out.println("Game " + g.getGameCode() + " - Teams: " + g.getTeams().size());
        }
    }

    public synchronized int createTeamId(int gameId) {
        GameState game = games.get(gameId);
        if (game != null) return game.getTeams().size() + 1;
        return -1;
    }

    // Inicia o jogo: envia GameStarted e primeira pergunta
    public void startGame(int gameId) {
        GameState game = getGame(gameId);
        if (game == null) {
            throw new IllegalArgumentException("No game with code " + gameId);
        }

        // --- 1. Validar equipas e jogadores ---
        ArrayList<Team> teams = game.getTeams();
        if (teams.isEmpty()) {
            throw new IllegalStateException("Cannot start game: no teams added");
        }

        int totalPlayers = 0;
        for (Team team : teams) {
            int size = team.getCurrentSize();
            if (size <= 0) {
                throw new IllegalStateException("Team " + team.getTeamId() + " has no players");
            }
            totalPlayers += size;
        }

        if (totalPlayers <= 0) {
            throw new IllegalStateException("Cannot start game: total players must be at least 1");
        }

        // --- 2. Inicializar perguntas de equipa e individual ---
        Question[] questions = game.getQuestions();
        if (questions == null || questions.length == 0) {
            throw new IllegalStateException("Cannot start game: no questions loaded");
        }

        for (Question q : questions) {
            if (q instanceof TeamQuestion tq) {
                for (Team team : teams) {
                    tq.addTeam(team);
                }
                tq.initializeBarrier(); // barrier conhece agora todos os jogadores
            }

            if (q instanceof IndividualQuestion iq) {
                iq.setTotalPlayers(totalPlayers);
                iq.initializeLatch();   // latch conhece agora todos os jogadores
            }
        }

        game.setActive(true);

        System.out.println("[Server] Game " + gameId + " successfully initialized and ready to start.");

        // Broadcast GameStartedWithPlayers using PlayerInfo
        ArrayList<PlayerInfo> playerInfos = new ArrayList<>();
        for (Player p : game.getAllPlayers()) {
            playerInfos.add(new PlayerInfo(p.getName(), p.getTeamId(), p.getScore()));
        }

        synchronized (ClientHandler.clientHandlers) {
            for (ClientHandler ch : ClientHandler.clientHandlers) {
                if (ch.getGameId() == gameId) {
                    ch.sendMessage(new GameStartedWithPlayers(gameId, playerInfos));
                }
            }
        }

        // Start the first question (initializes timers/latches for current question)
        nextQuestion(gameId);

        // Send the first question to all clients of the game by creating the message once and broadcasting it
        Object firstQuestionMsg = null;
        if (game.getCurrentQuestion() instanceof IndividualQuestion) {
            firstQuestionMsg = game.createSendIndividualQuestion();
        } else if (game.getCurrentQuestion() instanceof TeamQuestion) {
            firstQuestionMsg = game.createSendTeamQuestion();
        }

        if (firstQuestionMsg != null) {
            synchronized (ClientHandler.clientHandlers) {
                for (ClientHandler ch : ClientHandler.clientHandlers) {
                    if (ch.getGameId() == gameId && ch.handlerRunning) {
                        ch.sendMessage(firstQuestionMsg);
                    }
                }
            }
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
