package Server;

import Game.*;
import Utils.Constants;
import Utils.Records;
import Utils.Records.*;
import Utils.ModifiedCountdownLatch;

import java.io.*;
import java.net.Socket;
import java.net.SocketException;
import java.util.ArrayList;

public class ClientHandler extends Thread {

    private final Server server;
    private final Socket socket;
    private ObjectInputStream objectInputStream;
    private ObjectOutputStream objectOutputStream;

    private boolean handlerRunning = true;
    private ClientConnect clientConnected;
    private int gameId = -1;

    public static ArrayList<ClientHandler> clientHandlers = new ArrayList<>();

    public ClientHandler(Socket socket, Server server) {
        this.socket = socket;
        this.server = server;

        try {
            this.objectOutputStream = new ObjectOutputStream(socket.getOutputStream());
            this.objectOutputStream.flush();
            this.objectInputStream = new ObjectInputStream(socket.getInputStream());

            synchronized (clientHandlers) {
                clientHandlers.add(this);
            }

        } catch (IOException e) {
            System.err.println("CH constructor - Erro ao criar streams: " + e.getMessage());
            closeEverything();
        }
    }

    @Override
    public void run() {
        try {
            handleClientConnect();
            listenLoop();
        } catch (SocketException e) {
            System.out.println("CH run: Cliente desconectou: " +
                    (clientConnected != null ? clientConnected.username() : "unknown"));
        } catch (IOException | ClassNotFoundException e) {
            System.err.println("CH run: Erro com cliente " +
                    (clientConnected != null ? clientConnected.username() : "unknown") +
                    " - " + e.getMessage());
        } finally {
            closeEverything();
        }
    }

    private void handleClientConnect() throws IOException, ClassNotFoundException {
        Object firstMsg = objectInputStream.readObject();
        if (!(firstMsg instanceof ClientConnect connect)) {
            sendMessage(new FatalErrorMessage("Mensagem inicial inválida"));
            closeEverything();
            return;
        }

        this.clientConnected = connect;
        this.gameId = connect.gameId();
        GameState gameState = server.getGame(gameId);

        if (gameState == null) {
            sendMessage(new FatalErrorMessage("Game not found"));
            closeEverything();
            return;
        }

        Team team = gameState.getTeam(connect.teamId());
        if (team == null) {
            gameState.addTeam(connect.teamId(), "Team " + connect.teamId());
            team = gameState.getTeam(connect.teamId());
        }

        Player player = new Player(connect.username(), connect.teamId());
        team.addPlayer(player);

        broadcastConnectedPlayers();
    }

    private void listenLoop() {
        while (handlerRunning && !socket.isClosed()) {
            try {
                Object msg = objectInputStream.readObject();
                handleMessage(msg);
            } catch (IOException | ClassNotFoundException e) {
                System.out.println("CH listenLoop: Cliente desconectou: " +
                        (clientConnected != null ? clientConnected.username() : "unknown"));
                break;
            }
        }
    }

    private void handleMessage(Object message) {
        switch (message) {
            case SendAnswer sa -> handleSendAnswer(sa);
            case GameStarted gs -> sendNextQuestion();
            default -> System.out.println("CH - Mensagem desconhecida: " + message);
        }
    }

    private void handleSendAnswer(SendAnswer sa) {
        GameState gameState = server.getGame(gameId);
        if (gameState == null || gameId == -1) return;

        gameState.registerAnswer(sa.username(), sa.selectedOption());

        if (gameState.isCurrentQuestionComplete()) {
            RoundResult result = gameState.endRound();
            broadcastMessage(result, gameId);

            if (!result.gameEnded()) {
                sendNextQuestion();
            } else {
                broadcastMessage(gameState.getFinalScores(), gameId);
            }
        }
    }

    public void sendNextQuestion() {
        GameState gameState = server.getGame(gameId);
        if (gameState == null) return;

        Question current = gameState.getCurrentQuestion();
        if (current == null) return;

        if (current instanceof IndividualQuestion iq) {
            ModifiedCountdownLatch latch = new ModifiedCountdownLatch(
                    Constants.BONUS_FACTOR,
                    2,
                    Constants.QUESTION_TIME_LIMIT,
                    gameState.getTeams().stream().mapToInt(Team::getCurrentSize).sum()
            );
            iq.setCountdownLatch(latch);

            for (ClientHandler ch : clientHandlers) {
                if (ch.getGameId() == gameId) ch.sendMessage(gameState.createSendIndividualQuestion());
            }

            new Thread(() -> {
                try {
                    Thread.sleep(Constants.QUESTION_TIME_LIMIT * 1000L);
                    latch.tempoExpirado();
                    iq.processResponses(gameState.getTeam(clientConnected.teamId()));
                    broadcastRoundResult(gameState.endRound(), gameId);
                } catch (InterruptedException ignored) {}
            }).start();

        } else if (current instanceof TeamQuestion tq) {
            for (ClientHandler ch : clientHandlers) {
                if (ch.getGameId() == gameId) ch.sendMessage(gameState.createSendTeamQuestion());
            }

            new Thread(() -> {
                try {
                    Thread.sleep(Constants.QUESTION_TIME_LIMIT * 1000L);
                    tq.getBarrier().tempoExpirado();
                    broadcastRoundResult(gameState.endRound(), gameId);
                } catch (InterruptedException ignored) {}
            }).start();
        }
    }

    public void broadcastMessage(Object message, int gameId) {
        synchronized (clientHandlers) {
            for (ClientHandler ch : clientHandlers) {
                if (ch.handlerRunning && ch.gameId == gameId) ch.sendMessage(message);
            }
        }
    }

    public void sendMessage(Object message) {
        try {
            objectOutputStream.writeObject(message);
            objectOutputStream.flush();
        } catch (IOException e) {
            closeEverything();
        }
    }

    private void broadcastRoundResult(RoundResult roundResult, int gameId) {
        if (!roundResult.gameEnded()) {
            broadcastMessage(roundResult, gameId);
            return;
        }

        broadcastMessage(new GameEnded(gameId), gameId);
        GameState game = server.getGame(gameId);
        if (game != null) broadcastMessage(game.getFinalScores(), gameId);
    }

    private void broadcastConnectedPlayers() {
        GameState gameState = server.getGame(gameId);
        if (gameState == null || gameId == -1) return;

        ArrayList<String> connected = new ArrayList<>();
        for (Team t : gameState.getTeams()) {
            for (Player p : t.getPlayers()) connected.add(p.getName());
        }

        broadcastMessage(new ClientConnectAck("Server", gameId, connected), gameId);
    }

    public void closeEverything() {
        if (!handlerRunning) return;
        handlerRunning = false;

        synchronized (clientHandlers) { clientHandlers.remove(this); }

        System.out.println("CH closeEverything - Fechando conexão para: " +
                (clientConnected != null ? clientConnected.username() : "unknown"));

        GameState gameState = server.getGame(gameId);

        if (clientConnected != null && gameState != null) {
            Team team = gameState.getTeam(clientConnected.teamId());
            if (team != null) team.getPlayers().removeIf(p -> p.getName().equals(clientConnected.username()));
            broadcastConnectedPlayers();
        }

        try {
            if (objectInputStream != null) objectInputStream.close();
            if (objectOutputStream != null) objectOutputStream.close();
            if (socket != null && !socket.isClosed()) socket.close();
        } catch (IOException ignored) {}
    }

    public int getGameId() { return gameId; }
}
