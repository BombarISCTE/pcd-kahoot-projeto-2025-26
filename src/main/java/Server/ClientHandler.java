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
    public int getGameId() { return gameId; }

    public static ArrayList<ClientHandler> clientHandlers = new ArrayList<>();
    private int responseCounter = 0;

    public ClientHandler(Socket socket, Server server) {
        this.socket = socket;
        this.server = server;

        try {
            // Criar streams
            this.objectOutputStream = new ObjectOutputStream(socket.getOutputStream());
            this.objectOutputStream.flush();
            this.objectInputStream = new ObjectInputStream(socket.getInputStream());

            synchronized(clientHandlers) { clientHandlers.add(this); }

        } catch (IOException e) {
            System.err.println("CH constructor - Erro ao criar streams para o cliente: " + e.getMessage());
            closeEverything();
        }
    }

    @Override
    public void run() {
        try {
            handleClientConnect();  // handshake inicial
            listenLoop();           // loop principal de mensagens

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

    private void handleClientConnect() throws IOException, ClassNotFoundException {
        Object firstMsg = objectInputStream.readObject();
        if (!(firstMsg instanceof ClientConnect connect)) {
            sendMessage(new FatalErrorMessage("Mensagem inicial inválida"));
            closeEverything();
            return;
        }

        this.clientConnected = connect;
        this.gameId = connect.gameId();
        this.gameState = server.getGame(gameId);

        if (gameState == null) {
            sendMessage(new FatalErrorMessage("Game not found"));
            System.out.println("CH - handleClientConnect: game not found para " + connect.username());
            closeEverything(); // só fecha este cliente, não o servidor
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

        broadcastConnectedPlayers();
    }

    private void handleMessage(Object message) {
        switch (message) {
            case SendAnswer sa -> handleSendAnswer(sa);
            case GameStarted gs -> handleGameStarted(gs);
            default -> System.out.println("CH - Mensagem desconhecida: " + message);
        }
    }

    private void handleGameStarted(GameStarted gs) {
        sendNextQuestion(gameState);
    }

    private void sendNextQuestion(GameState gameState) {
        // lógica de envio de perguntas (igual à sua implementação atual)
    }

    private void handleSendAnswer(SendAnswer sa) {
        if (gameState == null) return;

        gameState.registerAnswer(sa.username(), sa.selectedOption());
        if (gameState.isCurrentQuestionIndividual()) {
            Player player = null;
            outer: for (Team t : gameState.getTeams()) {
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
                if (t.getPlayers().stream().anyMatch(p -> p.getName().equals(sa.username()))) {
                    t.playerAnswered();
                    break;
                }
            }
        }
    }

    public void broadcastMessage(Serializable message, int gameId) {
        synchronized(clientHandlers) {
            for (ClientHandler ch : clientHandlers) {
                if (ch.handlerRunning && ch.gameId == gameId) ch.sendMessage(message);
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


    public void closeEverything() {
        if (!handlerRunning) return;
        handlerRunning = false;

        synchronized(clientHandlers) { clientHandlers.remove(this); }

        System.out.println("CH closeEverything - Fechando conexão para: " +
                (clientConnected != null ? clientConnected.username() : "unknown"));

        if (clientConnected != null && gameState != null) {
            Team team = gameState.getTeam(clientConnected.teamId());
            if (team != null) {
                team.getPlayers().removeIf(p -> p.getName().equals(clientConnected.username()));
            }
            broadcastConnectedPlayers();
        }

        try {
            if (objectInputStream != null) objectInputStream.close();
            if (objectOutputStream != null) objectOutputStream.close();
            if (socket != null && !socket.isClosed()) socket.close();
        } catch (IOException ignored) {}
    }
}
