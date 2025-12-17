package Server;

import Game.*;
import Utils.Records.*;

import java.io.*;
import java.net.Socket;
import java.net.SocketException;

public class ClientHandler extends Thread {

    private final Server server;
    private final Socket socket;
    private ObjectInputStream objectInputStream;
    private ObjectOutputStream objectOutputStream;

    boolean handlerRunning = true;
    private ClientConnect clientConnected;
    private int gameId = -1;
    private GameState gameState;

    // Use a normal ArrayList with explicit synchronization for iteration/modification
    public static final java.util.ArrayList<ClientHandler> clientHandlers = new java.util.ArrayList<>();
    private static final java.util.concurrent.atomic.AtomicInteger HANDLER_ID_COUNTER = new java.util.concurrent.atomic.AtomicInteger(1);
    private final int handlerId = HANDLER_ID_COUNTER.getAndIncrement();

    public ClientHandler(Socket socket, Server server) {
        this.socket = socket;
        this.server = server;

        try {
            this.objectOutputStream = new ObjectOutputStream(socket.getOutputStream());
            this.objectOutputStream.flush();
            this.objectInputStream = new ObjectInputStream(socket.getInputStream());

            synchronized (clientHandlers) { clientHandlers.add(this); }
            System.out.println("CH constructor - handler added (id=" + handlerId + "). Total handlers: " + clientHandlers.size());
            synchronized (clientHandlers) {
                for (ClientHandler ch : clientHandlers) {
                    System.out.println("  Handler -> id=" + ch.handlerId + " username=" + (ch.clientConnected != null ? ch.clientConnected.username() : "(handshake pending)") + " gameId=" + ch.gameId);
                }
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

        this.gameId = connect.gameId();
        GameState gameState = server.getGame(gameId);
        this.gameState = gameState;
        System.out.println("CH handleClientConnect - handshake received (requested username=" + connect.username() + ", teamId=" + connect.teamId() + ", gameId=" + connect.gameId() + ") from " + socket.getRemoteSocketAddress());

        if (gameState == null) {
            sendMessage(new FatalErrorMessage("Game not found"));
            closeEverything();
            return;
        }

        // Assign a server-controlled player name that is unique within the game (Player1, Player2, ...)
        String assignedName;
        synchronized (gameState) {
            assignedName = gameState.assignNextPlayerName();

            // send assignment to client
            sendMessage(new AssignedName(assignedName));

            // update clientConnected to the assigned name for server-side bookkeeping
            connect = new ClientConnect(assignedName, connect.gameId(), connect.teamId());
            this.clientConnected = connect;

            System.out.println("CH handleClientConnect - assigned name " + assignedName + " for connection from " + socket.getRemoteSocketAddress());

            // Reject if the game has already started
            if (gameState.isActive()) {
                sendMessage(new refuseConnection("Game already started"));
                closeEverything();
                return;
            }

            // Ensure team exists and attempt to add player
            Team team = gameState.getTeam(connect.teamId());
            if (team == null) {
                server.addTeam(gameId, connect.teamId());
                team = gameState.getTeam(connect.teamId());
            }
            Player player = new Player(connect.username(), connect.teamId());
            int before = team.getCurrentSize();
            team.addPlayer(player);
            int after = team.getCurrentSize();
            if (after == before) {
                // team was full; inform client and close
                sendMessage(new refuseConnection("Team " + connect.teamId() + " is full"));
                closeEverything();
                return;
            }
        }

        // clientConnected and team population done inside synchronized(gameState) block above
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

        // Atomically try to end the round; only the caller that succeeds will get a non-null result
        RoundResult result = gameState.tryEndRoundIfComplete();
        if (result != null) {
            broadcastMessage(result, gameId);

            if (!result.gameEnded()) {
                // Broadcast next question to all clients
                // Initialize next question (timers/latches) and then send it
                gameState.startCurrentQuestion();
                Question next = gameState.getCurrentQuestion();
                Object nextMsg = null;
                if (next instanceof IndividualQuestion) nextMsg = gameState.createSendIndividualQuestion();
                else if (next instanceof TeamQuestion) nextMsg = gameState.createSendTeamQuestion();

                if (nextMsg != null) broadcastMessage(nextMsg, gameId);
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
        ClientHandler[] snapshot;
        synchronized (clientHandlers) {
            snapshot = clientHandlers.toArray(new ClientHandler[0]);
        }
        for (ClientHandler ch : snapshot) {
            if (ch == null) continue;
            if (!ch.handlerRunning || ch.gameId != gameId) continue;
            if (ch.clientConnected == null) continue; // handshake not completed
            String uname = ch.clientConnected != null ? ch.clientConnected.username() : "(unknown)";
            if (current instanceof IndividualQuestion) {
                System.out.println("CH sendNextQuestion - sending question to: id=" + ch.handlerId + " uname=" + uname + " (game " + gameId + ")");
                ch.sendMessage(gameState.createSendIndividualQuestion());
            } else if (current instanceof TeamQuestion) {
                System.out.println("CH sendNextQuestion - sending team question to: id=" + ch.handlerId + " uname=" + uname + " (game " + gameId + ")");
                ch.sendMessage(gameState.createSendTeamQuestion());
            }
        }

        // Não criar barrier/latch aqui; já está inicializado no GameState.startGame()
    }

    public void broadcastMessage(Object message, int gameId) {
        ClientHandler[] snapshot;
        synchronized (clientHandlers) {
            snapshot = clientHandlers.toArray(new ClientHandler[0]);
        }
        for (ClientHandler ch : snapshot) {
            if (ch == null) continue;
            if (!ch.handlerRunning || ch.gameId != gameId) continue;
            if (ch.clientConnected == null) continue;
            // simplified log per user request
            System.out.println("sending " + message.getClass().getSimpleName() + " to " + ch.clientConnected.username() + " in game " + gameId);
            ch.sendMessage(message);
        }
        System.out.println("CH broadcastMessage - broadcast completed for game " + gameId);
    }

    public synchronized void sendMessage(Object message) {
        try {
            objectOutputStream.writeObject(message);
            objectOutputStream.flush();
        } catch (IOException e) {
            System.err.println("CH sendMessage - error sending to " + (clientConnected != null ? clientConnected.username() : "unknown") + ": " + e.getMessage());
            e.printStackTrace();
            closeEverything();
        }
    }

    public void closeEverything() {
        if (!handlerRunning) return;
        handlerRunning = false;

        synchronized (clientHandlers) { clientHandlers.remove(this); }
        System.out.println("CH closeEverything - handler removed (id=" + handlerId + "). Total handlers: " + clientHandlers.size());
        synchronized (clientHandlers) {
            for (ClientHandler ch : clientHandlers) {
                System.out.println("  Remaining Handler -> id=" + ch.handlerId + " username=" + (ch.clientConnected != null ? ch.clientConnected.username() : "(handshake pending)") + " gameId=" + ch.gameId);
            }
        }

        System.out.println("CH closeEverything - Fechando conexão para: " +
                (clientConnected != null ? clientConnected.username() : "unknown"));

        if (clientConnected != null && gameState != null) {
            Team team = gameState.getTeam(clientConnected.teamId());
            if (team != null) team.removePlayer(clientConnected.username());
            // Do NOT broadcast full players list on disconnect; list is sent only on game start.
        }

        try {
            if (objectInputStream != null) objectInputStream.close();
            if (objectOutputStream != null) objectOutputStream.close();
            if (socket != null && !socket.isClosed()) socket.close();
        } catch (IOException ignored) {}
    }

    public int getGameId() { return gameId; }
}
