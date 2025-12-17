package Client;

import Utils.Constants;
import Utils.Records.*;
import Utils.Records.SendAnswer;

import java.io.*;
import java.net.Socket;
import java.net.SocketException;
import java.util.ArrayList;
import java.util.HashMap;

public class Client {

    private final String serverIP;
    private final int serverPort;
    private final String username;
    private int teamId; //wants to belong to
    private int gameId; //wants to join
    private Socket socket;
    private ObjectOutputStream outputStream;
    private ObjectInputStream inputStream;

    private ClientGUI gui;

    public Client(String serverIP, int serverPort, String username, int teamId, int gameId) {
        this.serverIP = serverIP;
        this.serverPort = serverPort;
        this.username = username;
        this.teamId = teamId;
        this.gameId = gameId;
        System.out.println(username +  " esolheu a equipa " + teamId + " para o jogo " + gameId);
    }

    public String getUsername() {
        return username;
    }

    public void start() throws IOException {
        socket = new Socket(serverIP, serverPort);
        outputStream = new ObjectOutputStream(socket.getOutputStream());
        outputStream.flush();
        inputStream = new ObjectInputStream(socket.getInputStream());

        // GUI is created only after server signals GameStartedWithPlayers
        // gui = new ClientGUI(this);

        new Thread(this::listenLoop).start();

        // Envia handshake inicial para conectar ao servidor
        sendMessage(new ClientConnect(username, gameId, teamId));
    }

    private void listenLoop() {
        try {
            while (true) {
                Object obj = inputStream.readObject();
                if (obj == null) continue;

                switch (obj.getClass().getSimpleName()) {
                    case "ClientConnectAck" -> {
                        ClientConnectAck ack = (ClientConnectAck) obj;
                        if (gui != null) {
                            gui.setConnectedPlayers(ack.connectedPlayers());
                        } else {
                            // GUI not created yet; ignore lobby ack – server will send GameStartedWithPlayers on start
                        }
                    }
                    case "NewPlayerConnected" -> {
                        NewPlayerConnected npc = (NewPlayerConnected) obj;
                        if (gui != null) {
                            gui.addPlayer(npc.player());
                        } else {
                            // GUI not created yet; ignore – will receive authoritative list at GameStartedWithPlayers
                        }
                    }
                    case "SendIndividualQuestion" -> {
                        if (gui != null) gui.mostrarNovaPergunta((SendIndividualQuestion) obj);
                        else System.out.println("Received SendIndividualQuestion before GUI creation; ignoring.");
                    }
                    case "SendTeamQuestion" -> {
                        if (gui != null) gui.mostrarNovaPergunta((SendTeamQuestion) obj);
                        else System.out.println("Received SendTeamQuestion before GUI creation; ignoring.");
                    }
                    case "SendRoundStats" -> {
                        SendRoundStats stats = (SendRoundStats) obj;
                        if (gui != null) gui.atualizarPlacar(stats.playerScores());
                        else System.out.println("Received SendRoundStats before GUI creation; ignoring.");
                    }
                    case "SendFinalScores" -> {
                        SendFinalScores finalScores = (SendFinalScores) obj;
                        if (gui != null) gui.gameEnded(finalScores.finalScores());
                        else System.out.println("Received SendFinalScores before GUI creation; ignoring.");
                    }
                    case "GameEnded" -> {
                        if (gui != null) {
                            gui.setMensagemEspaco.setText("Jogo terminado pelo servidor!");
                            gui.setOptionsEnabled(false);
                        } else System.out.println("Received GameEnded before GUI creation.");
                    }
                    case "ErrorMessage" -> {
                        ErrorMessage em = (ErrorMessage) obj;
                        if (gui != null) gui.setMensagemEspaco.setText("Erro: " + em.message());
                        else System.out.println("Server error: " + em.message());
                    }
                    case "FatalErrorMessage" -> {
                        FatalErrorMessage fm = (FatalErrorMessage) obj;
                        if (gui != null) gui.setMensagemEspaco.setText("Erro fatal: " + fm.message());
                        else System.out.println("Fatal error from server: " + fm.message());
                        closeEverything();
                        return;
                    }
                    case "refuseConnection" -> {
                        Utils.Records.refuseConnection rc = (Utils.Records.refuseConnection) obj;
                        // show message to user and close
                        if (gui != null) gui.setMensagemEspaco.setText("Conexão recusada: " + rc.reason());
                        else System.out.println("Conexão recusada: " + rc.reason());
                        closeEverything();
                        return;
                    }
                    case "GameStartedWithPlayers" -> {
                        GameStartedWithPlayers gs = (GameStartedWithPlayers) obj;
                        // create GUI now and populate player list
                        if (gui == null) {
                            gui = new ClientGUI(this);
                        }
                        if (gs.connectedPlayers() != null) {
                            gui.setConnectedPlayers(gs.connectedPlayers());
                        }
                    }
                    default -> System.out.println("Cliente recebeu mensagem desconhecida: " + obj.getClass().getName());
                }
            }
        } catch (IOException | ClassNotFoundException e) {
            if (gui != null) gui.setMensagemEspaco.setText("Conexão perdida com o servidor.");
        }
    }

    public void sendMessage(Serializable msg) {
        try {
            outputStream.writeObject(msg);
            outputStream.flush();
        } catch (IOException e) {
            gui.setMensagemEspaco.setText("Erro ao enviar mensagem ao servidor.");
        }
    }

    public void closeEverything() {
        try {
            if (inputStream != null) inputStream.close();
            if (outputStream != null) outputStream.close();
            if (socket != null && !socket.isClosed()) socket.close();
        } catch (IOException ignored) {}
    }

    public static void main(String[] args) {
        String serverIP = "localhost";
        int serverPort = Constants.SERVER_PORT;
        String username = "Player2";
        int teamId = 2;
        int gameId = 1;
        Client client = new Client(serverIP, serverPort, username, teamId, gameId);
        try {
            client.start();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
