package Client;

import Utils.Records.*;
import Utils.Records.SendAnswer;

import java.io.*;
import java.net.Socket;
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
    }

    public String getUsername() {
        return username;
    }

    public void start() throws IOException {
        socket = new Socket(serverIP, serverPort);
        outputStream = new ObjectOutputStream(socket.getOutputStream());
        outputStream.flush();
        inputStream = new ObjectInputStream(socket.getInputStream());

        gui = new ClientGUI(this);

        new Thread(this::listenLoop).start();

        // Envia handshake inicial para conectar ao servidor
        sendMessage(new ClientConnect(username, -1, 0)); // gameId e teamId serão atualizados pelo servidor
    }

    private void listenLoop() {
        try {
            while (true) {
                Object obj = inputStream.readObject();
                if (obj == null) continue;

                switch (obj.getClass().getSimpleName()) {
                    case "ClientConnectAck" -> {
                        ClientConnectAck ack = (ClientConnectAck) obj;
                        gui.setConnectedPlayers(ack.connectedPlayers());
                    }
                    case "SendIndividualQuestion" -> {
                        gui.mostrarNovaPergunta((SendIndividualQuestion) obj);
                    }
                    case "SendTeamQuestion" -> {
                        gui.mostrarNovaPergunta((SendTeamQuestion) obj);
                    }
                    case "SendRoundStats" -> {
                        SendRoundStats stats = (SendRoundStats) obj;
                        gui.atualizarPlacar(stats.playerScores());
                    }
                    case "SendFinalScores" -> {
                        SendFinalScores finalScores = (SendFinalScores) obj;
                        gui.gameEnded(finalScores.finalScores());
                    }
                    case "GameEnded" -> {
                        gui.setMensagemEspaco.setText("Jogo terminado pelo servidor!");
                        gui.setOptionsEnabled(false);
                    }
                    case "ErrorMessage" -> {
                        gui.setMensagemEspaco.setText("Erro: " + ((ErrorMessage) obj).message());
                    }
                    case "FatalErrorMessage" -> {
                        gui.setMensagemEspaco.setText("Erro fatal: " + ((FatalErrorMessage) obj).message());
                        closeEverything();
                        return;
                    }
                    default -> System.out.println("Cliente recebeu mensagem desconhecida: " + obj.getClass().getName());
                }
            }
        } catch (IOException | ClassNotFoundException e) {
            gui.setMensagemEspaco.setText("Conexão perdida com o servidor.");
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
        int serverPort = 8008;
        String username = "Player1";
        int teamId = 2;
        int gameId = 1000000;
        Client client = new Client(serverIP, serverPort, username, teamId, gameId);
        try {
            client.start();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
