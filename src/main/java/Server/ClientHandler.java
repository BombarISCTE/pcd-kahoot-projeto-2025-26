package Server;

import Game.*;
import Utils.Constants;
import Utils.Records.*;

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

    public ClientConnect clientConnected;

    private GameState gameState;
    private int gameId;

    public int getGameId() {
        return gameId;
    }

    public static ArrayList<ClientHandler> clientHandlers = new ArrayList<>();

    private int responseCounter = 0;

    public ClientHandler(Socket socket, Server server) throws IOException {
        this.socket = socket;
        this.server = server;
        this.gameState = null;

        this.objectOutputStream = new ObjectOutputStream(socket.getOutputStream());
        this.objectOutputStream.flush();
        this.objectInputStream = new ObjectInputStream(socket.getInputStream());
    }

    @Override
    public void run() {
        try {
            Object firstMsg = objectInputStream.readObject();
            if (firstMsg instanceof ClientConnect connect) {
                handleClientConnect(connect);
            } else {
                closeEverything();
                return;
            }

            while (handlerRunning && !socket.isClosed()) {
                Object msg = objectInputStream.readObject();
                handleMessage(msg);
            }

        } catch (SocketException | EOFException e) {
            System.out.println("CH run: Cliente desconectou: " +
                    (clientConnected != null ? clientConnected.username() : "unknown"));
        } catch (Exception e) {
            System.out.println("CH run: Cliente desconectou (exception): " +
                    (clientConnected != null ? clientConnected.username() : "unknown"));
        } finally {
            closeEverything();
        }
    }

    private void handleMessage(Object message) {
        switch (message) {
            case SendAnswer sa -> handleSendAnswer(sa);
            case GameStarted gs -> handleGameStarted(gs);
            default -> System.out.println("Unknown message: " + message);
        }
    }

    private synchronized void handleClientConnect(ClientConnect connect) {
        this.clientConnected = connect;
        this.gameId = connect.gameId();

        this.gameState = server.getGame(gameId);
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

        int playerId = server.generatePlayerId();
        Player player = new Player(playerId, connect.username());
        team.addPlayer(player);

        synchronized (clientHandlers) {
            clientHandlers.add(this);
        }

        broadcastConnectedPlayers();
    }

    private void broadcastConnectedPlayers() {
        if (gameState == null) return;

        ArrayList<String> connected = new ArrayList<>();
        for (Team t : gameState.getTeams()) {
            for (Player p : t.getPlayers()) {
                connected.add(p.getName());
            }
        }

        broadcastMessage(new ClientConnectAck("Server", gameId, connected), gameId);
    }

    private void handleGameStarted(GameStarted gs) {
        sendNextQuestion(gameState);
    }

    private void sendNextQuestion(GameState gameState) {
        Pergunta current = gameState.getCurrentQuestion();
        if (current == null) return;

        int timeoutSeconds = Constants.QUESTION_TIME_LIMIT;

        if (current instanceof Pergunta.PerguntaEquipa) {
            for (Team t : gameState.getTeams()) {
                t.startNewQuestion(() -> {
                    int score = t.calculateQuestionScore(current);
                    t.setPlayersRoundScore(score);
                    checkAllTeamsFinished(gameState);
                });
            }

            SendTeamQuestion questionMsg =
                    new SendTeamQuestion(current.getQuestion(), current.getOptions(),
                            gameState.getCurrentQuestionIndex(), timeoutSeconds);

            broadcastMessage(questionMsg, gameState.getGameCode());

        } else {
            SendIndividualQuestion questionMsg =
                    new SendIndividualQuestion(current.getQuestion(), current.getOptions(),
                            gameState.getCurrentQuestionIndex(), timeoutSeconds);

            broadcastMessage(questionMsg, gameState.getGameCode());
        }

        new Thread(() -> {
            try {
                Thread.sleep(timeoutSeconds * 1000);
                synchronized (gameState) {
                    RoundResult result = gameState.endRound();
                    broadcastMessage(new SendRoundStats(gameState.getGameCode(),
                            result.playerScores()), gameState.getGameCode());

                    if (!result.gameEnded()) {
                        sendNextQuestion(gameState);
                    } else {
                        broadcastMessage(gameState.getFinalScores(), gameState.getGameCode());
                    }
                }
            } catch (InterruptedException ignored) {
            }
        }).start();
    }

    private void checkAllTeamsFinished(GameState game) {
        boolean allFinished = game.getTeams().stream().allMatch(Team::isRoundFinished);
        if (!allFinished) return;

        RoundResult result = game.endRound();
        broadcastMessage(new SendRoundStats(game.getGameCode(),
                result.playerScores()), game.getGameCode());

        if (!result.gameEnded()) {
            sendNextQuestion(game);
        } else {
            broadcastMessage(game.getFinalScores(), game.getGameCode());
        }
    }

    private void handleSendAnswer(SendAnswer sa) {
        if (gameState == null) return;

        gameState.registerAnswer(sa.username(), sa.selectedOption());

        if (gameState.isCurrentQuestionIndividual()) {
            Player player = null;

            outer:
            for (Team t : gameState.getTeams()) {
                for (Player p : t.getPlayers()) {
                    if (p.getName().equals(sa.username())) {
                        player = p;
                        break outer;
                    }
                }
            }

            if (player != null) {
                responseCounter++;
                player.setResponseOrder(responseCounter);

                int baseScore = Constants.RIGHT_ANSWER_POINTS;
                if (player.getResponseOrder() <= 2 &&
                        gameState.getCurrentQuestion().verificarResposta(player.getChosenOption())) {
                    baseScore *= 2;
                }

                if (gameState.getCurrentQuestion().verificarResposta(player.getChosenOption())) {
                    player.addScore(baseScore);
                }
            }
        } else {
            for (Team t : gameState.getTeams()) {
                if (t.getPlayers().stream()
                        .anyMatch(p -> p.getName().equals(sa.username()))) {
                    t.playerAnswered();
                    break;
                }
            }
        }
    }

    public void broadcastMessage(Serializable message, int gameId) {
        synchronized (clientHandlers) {
            for (ClientHandler ch : clientHandlers) {
                if (ch.handlerRunning && ch.gameId == gameId) {
                    ch.sendMessage(message);
                }
            }
        }
    }

    public void sendMessage(Serializable message) {
        try {
            objectOutputStream.writeObject(message);
            objectOutputStream.flush();
        } catch (IOException e) {
            closeEverything();
        }
    }

    public void closeEverything() {
        if (!handlerRunning) return;
        handlerRunning = false;

        synchronized (clientHandlers) {
            clientHandlers.remove(this);
        }

        System.out.println("CH closeEverything - Closing connection for: " +
                (clientConnected != null ? clientConnected.username() : "unknown"));

        if (clientConnected != null && gameState != null) {
            Team team = gameState.getTeam(clientConnected.teamId());
            if (team != null) {
                team.getPlayers().removeIf(p ->
                        p.getName().equals(clientConnected.username()));
            }
            broadcastConnectedPlayers();
        }

        try {
            if (objectInputStream != null) objectInputStream.close();
            if (objectOutputStream != null) objectOutputStream.close();
            if (socket != null && !socket.isClosed()) socket.close();
        } catch (IOException ignored) {
        }
    }
}
