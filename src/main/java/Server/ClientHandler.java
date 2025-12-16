package Server;

import Utils.Records.*;
import Game.GameState;
import Game.Player;
import Game.Team;

import java.io.*;
import java.net.Socket;
import java.util.ArrayList;
import java.util.HashMap;

public class ClientHandler extends Thread {

    private final Server server;
    static ArrayList<ClientHandler> clientHandlers = new ArrayList<>();
    private final Socket socket;
    private final ObjectInputStream objectInputStream;
    private final ObjectOutputStream objectOutputStream;

    public ClientConnect clientConnected;

    public int getGameId() {return gameId;}

    private int gameId;
    private GameState gameState;

    public ClientHandler(Socket socket, Server server) throws IOException {
        this.socket = socket;
        this.server = server;

        this.objectOutputStream = new ObjectOutputStream(socket.getOutputStream());
        this.objectOutputStream.flush();
        this.objectInputStream = new ObjectInputStream(socket.getInputStream());

        synchronized(clientHandlers) { clientHandlers.add(this); }
    }

    @Override
    public void run() {
        try {
            // Primeira mensagem: ClientConnect
            Object msg = objectInputStream.readObject();
            if (msg instanceof ClientConnect connect) handleClientConnect(connect);

            // Loop principal
            while (!socket.isClosed()) {
                Object message = objectInputStream.readObject();
                handleMessage(message);
            }
        } catch (Exception e) {
            System.out.println("Client disconnected: " +
                    (clientConnected != null ? clientConnected.username() : "unknown"));
        } finally {
            closeEverything();
        }
    }


    private void handleMessage(Object message) {
        switch (message) {

            case ClientConnect connect -> handleClientConnect(connect);

            case SendAnswer sa -> handleSendAnswer(sa);

            case GameStarted gs -> handleGameStarted(gs);

            default -> System.out.println("Unknown message: " + message);
        }
    }

    private void handleClientConnect(ClientConnect connect) {
        System.out.println("handleClientConnect called");
        this.clientConnected = connect;
        this.gameId = connect.gameId();
        this.gameState = server.getGame(gameId);
        Team team = gameState.getTeam(connect.teamId());
        if (team == null) {
            // Se a equipa não existir ainda, cria e adiciona ao GameState
            gameState.addTeam(connect.teamId(), "Team " + connect.teamId());
            team = gameState.getTeam(connect.teamId());
        }

        System.out.println("111111111111");

        // Pega o ID do player a partir do Server
        int playerId = server.generatePlayerId();
        System.out.println("Generated playerId: " + playerId);
        Player player = new Player(playerId, connect.username());
        System.out.println("team" + team);
        System.out.println("Created player: " + player.getName() + " with ID: " + player.getId());
        System.out.println("max players per team: " + team.getMaxPlayersPerTeam());
        System.out.println("team current players: " + team.getPlayers().size());

        team.addPlayer(player);

        System.out.println("22222222222222222222");
        // Lista de jogadores conectados no jogo
        ArrayList<String> connectedPlayers = new ArrayList<>();
        for (Team t : gameState.getTeams()) {
            for (Player p : t.getPlayers()) {
                connectedPlayers.add(p.getName());
            }
        }

        ClientConnectAck ack = new ClientConnectAck(connect.username(), gameId, connectedPlayers);
        broadcastMessage(ack, gameId);
        System.out.println("end of handleClientConnect");
    }




    private void sendNextQuestion(GameState game, int timeoutSeconds) {
        for (Team t : game.getTeams()) {
            t.startNewQuestion(() -> checkAllTeamsFinished(game));
        }
        sendQuestionWithTimer(timeoutSeconds);
    }

    private void checkAllTeamsFinished(GameState game) {
        boolean allFinished = true;
        for (Team t : game.getTeams()) {
            if (!t.isRoundFinished()) {
                allFinished = false;
                break;
            }
        }
        if (allFinished) {sendRoundResultsAndNext();}
    }



    private void sendQuestionWithTimer(int timeoutSeconds) {
        SendQuestion questionMsg = gameState.createSendQuestion(timeoutSeconds);
        if (questionMsg == null) return;

        broadcastMessage(questionMsg, gameId);

        new Thread(() -> {
            try {
                Thread.sleep(timeoutSeconds * 1000);
                synchronized (gameState) {
                    sendRoundResultsAndNext();
                }
            } catch (InterruptedException ignored) {}
        }).start();
    }

    private void sendRoundResultsAndNext() {
        RoundResult result = gameState.endRound();
        broadcastMessage(new SendRoundStats(gameId, result.playerScores()), gameId);

        if (!result.gameEnded()) {
            sendQuestionWithTimer(30);
        } else {
            broadcastMessage(new GameEnded(gameId), gameId);
            broadcastMessage(gameState.getFinalScores(), gameId);
        }
    }

    private void handleSendAnswer(SendAnswer sa) {
        gameState.registerAnswer(sa.username(), sa.selectedOption());
        boolean roundDone = true;
        for (Team t : gameState.getTeams()) {
            if (!t.isRoundFinished()) {
                roundDone = false;
                break;
            }
        }
        if (roundDone) {
            sendRoundResultsAndNext();}
    }


    private void handleGameStarted(GameStarted gs) {
        GameState game = server.getGame(gs.getGameId());
        if (game == null) return;

        // Inicializa a barrier para cada equipa com ação de enviar resultados
        for (Team t : game.getTeams()) {
            if (!t.getPlayers().isEmpty()) {
                t.startNewQuestion(this::sendRoundResultsAndNext);
            }
        }

        // Envia a primeira pergunta
        SendQuestion questionMsg = game.createSendQuestion(30);
        if (questionMsg != null) {
            broadcastMessage(questionMsg, gs.getGameId());
        }
    }



    public void broadcastMessage(Serializable message, int gameId) {
        synchronized (clientHandlers) {
            for (ClientHandler ch : clientHandlers) {
                if (ch.gameId == gameId) ch.sendMessage(message);
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

    public void connectClient(Object line) {
        if (line instanceof ClientConnect connect) {
            this.clientConnected = connect;
            this.gameId = connect.gameId();
            this.gameState = server.getGame(gameId);
        }
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
