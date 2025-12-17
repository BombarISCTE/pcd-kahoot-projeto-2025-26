package Server;

import Game.*;
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
    private ClientConnect clientConnected;
    private int gameId = -1;
    private GameState gameState;

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
        if (!(firstMsg instanceof ClientConnect connect)) return;

        this.clientConnected = connect;
        this.gameId = connect.gameId();
        GameState gameState = server.getGame(gameId);

        // Adiciona jogador à equipa
        Team team = gameState.getTeam(connect.teamId());
        if (team == null) {
            server.addTeam(gameId, connect.teamId());
            team = gameState.getTeam(connect.teamId());
        }
        Player player = new Player(connect.username(), connect.teamId());
        team.addPlayer(player);

        // Envia apenas aos outros clientes do jogo
        synchronized (ClientHandler.clientHandlers) {
            for (ClientHandler ch : ClientHandler.clientHandlers) {
                if (ch != this && ch.gameId == gameId && ch.handlerRunning) {
                    ch.sendMessage(new NewPlayerConnected(connect.username(), connect.teamId()));
                }
            }
        }
    }

    private void listenLoop() { //todo ver se este metodo certo
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
        if (message instanceof SendAnswer sa) {
            handleSendAnswer(sa);
        } else if (message instanceof GameStarted) {
            // GameStarted só enviado quando startGame é chamado
            sendNextQuestion();
        } else {
            System.out.println("CH - Mensagem desconhecida: " + message);
        }
    }

    private void handleSendAnswer(SendAnswer sa) {
        if (gameState == null || !gameState.isActive()) return;

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
        if (gameState == null || !gameState.isActive()) return;

        Question current = gameState.getCurrentQuestion();
        if (current == null) return;

        // Envia GUI para todos os clientes do jogo
        synchronized (clientHandlers) {
            for (ClientHandler ch : clientHandlers) {
                if (ch.handlerRunning && ch.gameId == gameId) {
                    if (current instanceof IndividualQuestion) {
                        ch.sendMessage(gameState.createSendIndividualQuestion());
                    } else if (current instanceof TeamQuestion) {
                        ch.sendMessage(gameState.createSendTeamQuestion());
                    }
                }
            }
        }

        // Não criar barrier/latch aqui; já está inicializado no GameState.startGame()
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

    private void broadcastConnectedPlayers() {
        if (gameState == null) return;

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
