package Client;

import Utils.Constants;
import Utils.Records.*;

import javax.swing.*;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.lang.reflect.InvocationTargetException;
import java.net.Socket;

/*
Class: Client

Public constructors:
 - Client(String serverIP, int serverPort, String username, int teamId, int gameId)

Public methods (signatures):
 - String getUsername()
 - void setUsername(String username)
 - void start() throws IOException
 - void listenLoop()
 - synchronized void sendMessage(Serializable msg)
 - void closeEverything()
 - static void main(String[] args)

Notes:
 - Client manages socket connection, reads incoming Records and updates ClientGUI on the EDT.
 - Handles AssignedName, GameStartedWithPlayers, SendIndividualQuestion, SendTeamQuestion, RoundResult, SendFinalScores, refuseConnection and error messages.
*/

public class Client {

    private final String serverIP;
    private final int serverPort;
    private String username;
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

    public void setUsername(String username) { this.username = username; }

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

                System.out.println("Client: received message -> " + obj.getClass().getSimpleName());

                switch (obj.getClass().getSimpleName()) {
                    case "AssignedName" -> {
                        AssignedName an = (AssignedName) obj;
                        System.out.println("Client: assigned name from server -> " + an.assignedName());
                        setUsername(an.assignedName());
                    }
                    case "ClientConnectAck" -> {
                        ClientConnectAck ack = (ClientConnectAck) obj;
                        if (gui != null) {
                            SwingUtilities.invokeLater(() -> gui.setConnectedPlayers(ack.connectedPlayers()));
                        } else {
                            // GUI not created yet; ignore lobby ack – server will send GameStartedWithPlayers on start
                        }
                    }
                    case "NewPlayerConnected" -> {
                        NewPlayerConnected npc = (NewPlayerConnected) obj;
                        if (gui != null) {
                            SwingUtilities.invokeLater(() -> gui.addPlayer(npc.player()));
                        } else {
                            // GUI not created yet; ignore – will receive authoritative list at GameStartedWithPlayers
                        }
                    }
                    case "SendIndividualQuestion" -> {
                        SendIndividualQuestion siq = (SendIndividualQuestion) obj;
                        if (gui != null) SwingUtilities.invokeLater(() -> gui.mostrarNovaPergunta(siq));
                        else System.out.println("Received SendIndividualQuestion before GUI creation; ignoring.");
                    }
                    case "SendTeamQuestion" -> {
                        if (gui != null) SwingUtilities.invokeLater(() -> gui.mostrarNovaPergunta((SendTeamQuestion) obj));
                        else System.out.println("Received SendTeamQuestion before GUI creation; ignoring.");
                    }
                    case "SendRoundStats" -> {
                        SendRoundStats stats = (SendRoundStats) obj;
                        if (gui != null) SwingUtilities.invokeLater(() -> gui.atualizarPlacar(stats.playerScores()));
                        else System.out.println("Received SendRoundStats before GUI creation; ignoring.");
                    }
                    case "RoundResult" -> {
                        RoundResult rr = (RoundResult) obj;
                        if (gui != null) SwingUtilities.invokeLater(() -> {
                            gui.atualizarPlacar(rr.playerScores());
                            gui.setMensagemEspaco.setText("Round ended — scores updated.");
                        });
                        else System.out.println("Received RoundResult before GUI creation; ignoring.");
                    }
                    case "SendFinalScores" -> {
                        SendFinalScores finalScores = (SendFinalScores) obj;
                        if (gui != null) SwingUtilities.invokeLater(() -> {
                            gui.atualizarPlacar(finalScores.finalScores());
                            gui.showRoundStats(finalScores.finalScores()); // popup for final stats
                            gui.gameEnded(finalScores.finalScores());
                        });
                        else System.out.println("Received SendFinalScores before GUI creation; ignoring.");
                    }
                    case "GameEnded" -> {
                        if (gui != null) SwingUtilities.invokeLater(() -> {
                            gui.setMensagemEspaco.setText("Jogo terminado pelo servidor!");
                            gui.setOptionsEnabled(false);
                        });
                        else System.out.println("Received GameEnded before GUI creation.");
                    }
                    case "ErrorMessage" -> {
                        ErrorMessage em = (ErrorMessage) obj;
                        if (gui != null) SwingUtilities.invokeLater(() -> gui.setMensagemEspaco.setText("Erro: " + em.message()));
                        else System.out.println("Server error: " + em.message());
                    }
                    case "FatalErrorMessage" -> {
                        FatalErrorMessage fm = (FatalErrorMessage) obj;
                        if (gui != null) SwingUtilities.invokeLater(() -> gui.setMensagemEspaco.setText("Erro fatal: " + fm.message()));
                        else System.out.println("Fatal error from server: " + fm.message());
                        closeEverything();
                        return;
                    }
                    case "refuseConnection" -> {
                        Utils.Records.refuseConnection rc = (Utils.Records.refuseConnection) obj;
                        // show message to user and close
                        if (gui != null) SwingUtilities.invokeLater(() -> gui.setMensagemEspaco.setText("Conexão recusada: " + rc.reason()));
                        else System.out.println("Conexão recusada: " + rc.reason());
                        closeEverything();
                        return;
                    }
                    case "GameStartedWithPlayers" -> {
                        GameStartedWithPlayers gs = (GameStartedWithPlayers) obj;
                        // create GUI now and populate player list synchronously on EDT so we don't miss the immediately following question
                        if (gui == null) {
                            try {
                                SwingUtilities.invokeAndWait(() -> {
                                    gui = new ClientGUI(this);
                                    if (gs.connectedPlayers() != null) gui.setConnectedPlayers(gs.connectedPlayers());
                                });
                            } catch (InterruptedException | InvocationTargetException e) {
                                System.out.println("Failed to create GUI on EDT: " + e.getMessage());
                                Thread.currentThread().interrupt();
                            }
                        } else {
                            if (gs.connectedPlayers() != null) SwingUtilities.invokeLater(() -> gui.setConnectedPlayers(gs.connectedPlayers()));
                        }
                    }
                    default -> System.out.println("Cliente recebeu mensagem desconhecida: " + obj.getClass().getName());
                }
            }
        } catch (IOException | ClassNotFoundException e) {
            e.printStackTrace();
            if (gui != null) SwingUtilities.invokeLater(() -> gui.setMensagemEspaco.setText("Conexão perdida com o servidor."));
        }
    }

    public synchronized void sendMessage(Serializable msg) {
        try {
            outputStream.writeObject(msg);
            outputStream.flush();
        } catch (IOException e) {
            e.printStackTrace();
            if (gui != null) SwingUtilities.invokeLater(() -> gui.setMensagemEspaco.setText("Erro ao enviar mensagem ao servidor."));
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
        String username = "Player" + (int)(Math.random()*1000);
        int teamId = 1;
        int gameId = 1;
        if (args.length >= 1) username = args[0];
        if (args.length >= 2) teamId = Integer.parseInt(args[1]);
        if (args.length >= 3) gameId = Integer.parseInt(args[2]);
        Client client = new Client(serverIP, serverPort, username, teamId, gameId);
        try {
            client.start();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
