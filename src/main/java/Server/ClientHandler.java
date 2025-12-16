package Server;

import Game.Pergunta;
import Utils.Constants;
import Utils.Records.*;
import Game.GameState;
import Game.Player;
import Game.Team;

import java.io.*;
import java.net.Socket;
import java.net.SocketException;
import java.util.ArrayList;
import java.util.HashMap;

public class ClientHandler extends Thread {

    private final Server server;
    private final Socket socket;
    private final ObjectInputStream objectInputStream;
    private final ObjectOutputStream objectOutputStream;
    private boolean handlerRunning=true;
    public ClientConnect clientConnected;

    private GameState gameState;
    private int gameId;
    public int getGameId() {return gameId;}

    public static ArrayList<ClientHandler> clientHandlers = new ArrayList<>();

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
            // Primeira mensagem tem de ser ClientConnect
            Object firstMsg = objectInputStream.readObject();
            if (firstMsg instanceof ClientConnect connect) {
                handleClientConnect(connect);
            }

            while (handlerRunning && !socket.isClosed()) {
                Object msg = objectInputStream.readObject();
                handleMessage(msg);
            }

        } catch (SocketException | EOFException e) {
            System.out.println("Cliente desconectou: " +
                    (clientConnected != null ? clientConnected.username() : "unknown"));
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            closeEverything();
        }
    }




        private void handleMessage(Object message) {
        switch (message) { //todo msg.getClass().getSimpleName() ?

            //case ClientConnect connect -> handleClientConnect(connect);

            case SendAnswer sa -> handleSendAnswer(sa);

            case GameStarted gs -> handleGameStarted(gs);

            default -> System.out.println("Unknown message: " + message);
        }
    }

    private synchronized void handleClientConnect(ClientConnect connect) {
        this.clientConnected = connect;
        this.gameId = connect.gameId();
        this.gameState = server.getGame(gameId);

        // Se equipa não existir, cria
        Team team = gameState.getTeam(connect.teamId());
        if (team == null) {
            gameState.addTeam(connect.teamId(), "Team " + connect.teamId());
            team = gameState.getTeam(connect.teamId());
        }

        // Cria player
        int playerId = server.generatePlayerId();
        Player player = new Player(playerId, connect.username());
        team.addPlayer(player);

        // Envia lista atualizada de jogadores conectados
        broadcastConnectedPlayers();
    }

    private void broadcastConnectedPlayers() {
        ArrayList<String> connected = new ArrayList<>();
        for (Team t : gameState.getTeams()) {
            for (Player p : t.getPlayers()) connected.add(p.getName());
        }
        broadcastMessage(new ClientConnectAck("Server", gameId, connected), gameId);
    }

//    private synchronized void handleClientConnect(ClientConnect connect) {
//        System.out.println("handleClientConnect called");
//        this.clientConnected = connect;
//        this.gameId = connect.gameId();
//        this.gameState = server.getGame(gameId);
//        Team team = gameState.getTeam(connect.teamId());
//        if (team == null) {
//            // Se a equipa não existir ainda, cria e adiciona ao GameState
//            gameState.addTeam(connect.teamId(), "Team " + connect.teamId());
//            team = gameState.getTeam(connect.teamId());
//        }
//
//        System.out.println("111111111111");
//
//        // Pega o ID do player a partir do Server
//        int playerId = server.generatePlayerId();
//        System.out.println("Generated playerId: " + playerId);
//        Player player = new Player(playerId, connect.username());
//        System.out.println("team" + team);
//        System.out.println("Created player: " + player.getName() + " with ID: " + player.getId());
//        System.out.println("max players per team: " + team.getMaxPlayersPerTeam());
//        System.out.println("team current players: " + team.getPlayers().size());
//
//        team.addPlayer(player);
//
//        System.out.println("22222222222222222222");
//        // Lista de jogadores conectados no jogo
//        ArrayList<String> connectedPlayers = new ArrayList<>();
//        for (Team t : gameState.getTeams()) {
//            for (Player p : t.getPlayers()) {
//                connectedPlayers.add(p.getName());
//            }
//        }
//        System.out.println("33333333333333");
//
//        ClientConnectAck ack = new ClientConnectAck(connect.username(), gameId, connectedPlayers);
//        System.out.println("44444444444444");
//        broadcastMessage(ack, gameId);
//        System.out.println("end of handleClientConnect");
//    }




    private void checkAllTeamsFinished(GameState game) {
        boolean allFinished = game.getTeams().stream().allMatch(Team::isRoundFinished);
        if (allFinished) {
            RoundResult result = game.endRound();
            broadcastMessage(new SendRoundStats(game.getGameCode(), result.playerScores()), game.getGameCode());

            if (!result.gameEnded()) sendNextQuestion(game);
            else broadcastMessage(game.getFinalScores(), game.getGameCode());
        }
    }



    private void sendNextQuestion(GameState gameState) {
        // Inicializa barreiras para perguntas de equipa
        int timeoutSeconds = Constants.QUESTION_TIME_LIMIT;
        Pergunta current = gameState.getCurrentQuestion();
        if (current != null && !(current instanceof Pergunta.PerguntaIndividual)) {
            for (Team t : gameState.getTeams()) {
                t.startNewQuestion(() -> {
                    // Esta ação é chamada quando todos os jogadores da equipa responderam ou timeout
                    int score = t.calculateQuestionScore(current);
                    t.setPlayersRoundScore(score);
                    // Verifica se todas as equipas terminaram a ronda
                    checkAllTeamsFinished(gameState);
                });
            }
        }
        Serializable questionMsg = gameState.createSendQuestion(timeoutSeconds);
        if (questionMsg != null) broadcastMessage(questionMsg, gameState.getGameCode());

        // Timeout global
        new Thread(() -> {
            try {
                Thread.sleep(timeoutSeconds * 1000);
                synchronized (gameState) {
                    RoundResult result = gameState.endRound();
                    broadcastMessage(new SendRoundStats(gameState.getGameCode(), result.playerScores()), gameState.getGameCode());
                    if (!result.gameEnded()) sendNextQuestion(gameState);
                    else broadcastMessage(gameState.getFinalScores(), gameState.getGameCode());
                }
            } catch (InterruptedException ignored) {}
        }).start();
    }


    private void sendRoundResultsAndNext() {
        RoundResult result = gameState.endRound();
        broadcastMessage(new SendRoundStats(gameId, result.playerScores()), gameId);

        if (!result.gameEnded()) {
            sendNextQuestion(gameState);
        } else {
            broadcastMessage(new GameEnded(gameId), gameId);
            broadcastMessage(gameState.getFinalScores(), gameId);
        }
    }

    private void handleSendAnswer(SendAnswer sa) {
        gameState.registerAnswer(sa.username(), sa.selectedOption());

        if (!gameState.isCurrentQuestionIndividual()) {
            Team team = null;
            for (Team t : gameState.getTeams()) {
                if (t.getPlayers().stream().anyMatch(p -> p.getName().equals(sa.username()))) {
                    team = t;
                    break;
                }
            }
            if (team != null) team.playerAnswered(); // ativa barreira
        }
    }


    private void handleGameStarted(GameStarted gs) {
        GameState game = server.getGame(gs.getGameId());
        if (game == null) return;

        sendNextQuestion(game);
    }





    public void broadcastMessage(Serializable message, int gameId) {
        synchronized (clientHandlers) {
            for (ClientHandler ch : clientHandlers) {
                if (ch.gameId == gameId && ch.isHandlerRunning()) ch.sendMessage(message);
            }
        }
    }
    public boolean isHandlerRunning() {return handlerRunning;}

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

//    public void closeEverything() {
//        if (!handlerRunning) return;
//        handlerRunning = false;
//
//        synchronized (clientHandlers) {
//            clientHandlers.remove(this);
//        }
//
//        if (clientConnected != null && gameState != null) {
//            Team team = gameState.getTeam(clientConnected.teamId());
//            if (team != null) {
//                team.getPlayers().removeIf(p ->
//                        p.getName().equals(clientConnected.username()));
//            }
//
//            ArrayList<String> connectedPlayers = new ArrayList<>();
//            for (Team t : gameState.getTeams()) {
//                for (Player p : t.getPlayers()) {
//                    connectedPlayers.add(p.getName());
//                }
//            }
//
//            broadcastMessage(
//                    new ClientConnectAck("Server", gameId, connectedPlayers),
//                    gameId
//            );
//        }
//
//        try {
//            if (objectInputStream != null) objectInputStream.close();
//            if (objectOutputStream != null) objectOutputStream.close();
//            if (socket != null && !socket.isClosed()) socket.close();
//        } catch (IOException ignored) {}
//    }

    public void closeEverything() {
        if (!handlerRunning) return;
        handlerRunning = false;

        synchronized (clientHandlers) { clientHandlers.remove(this); }

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
