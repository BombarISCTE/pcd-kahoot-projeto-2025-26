package Server;

import Utils.Constants;
import Utils.Records;
import Utils.Records.*;
import Game.GameState;
import Game.Player;

import java.io.*;
import java.net.Socket;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class ClientHandler extends Thread {

    private final Server server;
    static ArrayList<ClientHandler> clientHandlers = new ArrayList<>();

    private Socket socket;
    private ObjectInputStream objectInputStream;
    private ObjectOutputStream objectOutputStream;

    public ClientConnect clientConnected;
    public int gameId;
    public GameState gameState;

    public ClientHandler(Socket socket, Server server) throws IOException, ClassNotFoundException {
        this.socket = socket;
        this.server = server;

        this.objectOutputStream = new ObjectOutputStream(socket.getOutputStream());
        this.objectOutputStream.flush();
        this.objectInputStream = new ObjectInputStream(socket.getInputStream());

        Object line = objectInputStream.readObject();
        connectClient(line);

        synchronized (clientHandlers) {
            clientHandlers.add(this);
        }

        broadcastMessage("SERVER: " + clientConnected.username() + " has entered the game!", gameId);
    }

    @Override
    public void run() {
        try {
            while (socket != null && !socket.isClosed()) {
                Object message = objectInputStream.readObject();
                handleMessage(message);
            }
        } catch (IOException | ClassNotFoundException e) {
            System.out.println("Client disconnected: " + (clientConnected != null ? clientConnected.username() : "unknown"));
        } finally {
            closeEverything();
        }
    }

    // ----------------------- Messaging -----------------------

    public void broadcastMessage(Serializable message, int gameId) {
        synchronized (clientHandlers) {
            for (ClientHandler ch : clientHandlers) {
                if (ch.gameId == gameId) {
                    ch.sendMessage(message);
                }
            }
        }
    }

    public void sendMessage(Serializable message) {
        try {
            if (objectOutputStream != null) {
                objectOutputStream.writeObject(message);
                objectOutputStream.flush();
            }
        } catch (IOException e) {
            closeEverything();
        }
    }

    // ----------------------- Client Connect -----------------------

    public void connectClient(Object line) {
        if (line instanceof ClientConnect connect) {
            this.clientConnected = connect;
            this.gameId = connect.gameId();
            this.gameState = server.getGame(gameId);
        }
    }

    // ----------------------- Handle Messages -----------------------

    private void handleMessage(Object message) {
        if (message == null) return;

        synchronized (gameState) {
            switch (message) {

                // Client connects
                case ClientConnect connect -> {
                    this.clientConnected = connect;
                    this.gameId = connect.gameId();
                    Player player = gameState.addPlayer(connect.username());

                    // Broadcast ClientConnectAck
                    List<String> connectedPlayers = new ArrayList<>(gameState.getPlayers().keySet());
                    ClientConnectAck ack = new ClientConnectAck(connect.username(), gameId, connectedPlayers);
                    broadcastMessage(ack, gameId);
                }

                // Client sends answer
                case SendAnswer sa -> {
                    gameState.registerAnswer(sa.username(), sa.selectedOption());

                    // Check if round ended
                    if (gameState.roundEnded()) {
                        RoundResult roundResult = gameState.endRound();
                        sendRoundStats(roundResult);

                        if (!roundResult.gameEnded()) {
                            sendQuestionWithTimer(); // 30s per question
                        } else {
                            sendGameEnded();
                        }
                    }
                }

                // Start game (from TUI)
                case GameStarted gs -> {
                    broadcastMessage(gs, gs.getGameId());
                    sendQuestionWithTimer(); // start first question with 30s timer
                }

                // Unknown messages
                default -> System.out.println("Unknown message received: " + message);
            }
        }
    }

    // ----------------------- Question / Round Handling -----------------------

    public void sendQuestionWithTimer() {
        int timerSecs = Constants.TIMOUT_SECS;
        SendQuestion questionMsg = gameState.createSendQuestion(timerSecs);
        if (questionMsg == null) return;

        broadcastMessage(questionMsg, gameId);

        // Timer thread for the question
        new Thread(() -> {
            try {
                Thread.sleep(timerSecs * 1000);

                synchronized (gameState) {
                    if (!gameState.roundEnded()) {
                        RoundResult roundResult = gameState.endRound();
                        sendRoundStats(roundResult);

                        if (!roundResult.gameEnded()) {
                            sendQuestionWithTimer();
                        } else {
                            sendGameEnded();
                        }
                    }
                }
            } catch (InterruptedException e) {
                System.out.println("Question timer interrupted for game " + gameId);
                Thread.currentThread().interrupt();
            }
        }).start();
    }

    private void sendRoundStats(RoundResult roundResult) {
        SendRoundStats srs = new SendRoundStats(gameId, new HashMap<>(roundResult.playerScores()));
        broadcastMessage(srs, gameId);
    }

    private void sendGameEnded() {
        broadcastMessage(new GameEnded(gameId), gameId);

        HashMap<String, Integer> finalScores = new HashMap<>();
        for (Player p : gameState.getPlayers().values()) {
            finalScores.put(p.getName(), p.getScore());
        }
        broadcastMessage(new SendFinalScores(finalScores), gameId);
    }


    public void closeEverything() {
        try {
            synchronized (clientHandlers) { clientHandlers.remove(this); }
            if (objectInputStream != null) objectInputStream.close();
            if (objectOutputStream != null) objectOutputStream.close();
            if (socket != null && !socket.isClosed()) socket.close();
        } catch (IOException ignored) {}
    }
}
