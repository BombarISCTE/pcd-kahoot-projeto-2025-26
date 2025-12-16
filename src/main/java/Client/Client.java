package Client;

import Game.Pergunta;
import Utils.Records.*;
import java.io.*;
import java.net.Socket;
import java.util.HashMap;

public class Client implements Runnable, Serializable {

    private Socket socket;
    private ObjectOutputStream objectOut;
    private ObjectInputStream objectIn;

    private final String serverIP;
    private final int serverPort;

    private final int gameId;
    private final int teamId;
    private final String username;

    private ClientGUI gui;

    public Client(String serverIP, int serverPort, int gameId, int teamId, String username) {
        this.serverIP = serverIP;
        this.serverPort = serverPort;
        this.gameId = gameId;
        this.teamId = teamId;
        this.username = username;
    }

    @Override
    public void run() {
        try {
            connectToServer();
            listenForMessages();
            gui = new ClientGUI(this);
        } catch (Exception e) {
            System.err.println("Cliente encerrou: " + e.getMessage());
        } finally {
            closeEverything();
        }
    }

    private void connectToServer() throws IOException {
        socket = new Socket(serverIP, serverPort);
        System.out.println("Conectado ao servidor: " + serverIP + ":" + serverPort);

        objectOut = new ObjectOutputStream(socket.getOutputStream());
        objectOut.flush();
        objectIn = new ObjectInputStream(socket.getInputStream());

        sendMessage(new ClientConnect(username, gameId, teamId));
    }

    private void listenForMessages() {
        new Thread(() -> {
            while (socket != null && socket.isConnected()) {
                try {
                    Object msg = objectIn.readObject();

                    switch (msg.getClass().getSimpleName()) {
                        case "SendQuestion" -> {
                            SendQuestion sq = (SendQuestion) msg;
                            Pergunta p = new Pergunta(sq.question(), -1, 0, sq.options());
                            gui.mostrarNovaPergunta(p, sq.questionNumber());
                        }

                        case "SendRoundStats" -> {
                            SendRoundStats srs = (SendRoundStats) msg;
                            gui.atualizarPlacar(new HashMap<>(srs.playerScores()));
                        }

                        case "SendFinalScores" -> {
                            SendFinalScores sfs = (SendFinalScores) msg;
                            gui.gameEnded(new HashMap<>(sfs.finalScores()));
                        }

                        case "ClientConnectAck" -> {
                            ClientConnectAck ack = (ClientConnectAck) msg;
                            gui.setConnectedPlayers(ack.connectedPlayers());
                        }


                        default -> System.out.println("Mensagem recebida de tipo desconhecido: " + msg);
                    }

                } catch (IOException e) {
                    System.err.println("Erro ao receber mensagem do servidor: " + e.getMessage());
                    closeEverything();
                    break;
                } catch (ClassNotFoundException e) {
                    System.err.println("Classe não encontrada ao receber mensagem: " + e.getMessage());
                }
            }
        }).start();
    }

    public void sendMessage(Serializable message) {
        try {
            if (objectOut != null) {
                objectOut.writeObject(message);
                objectOut.flush();
            }
        } catch (IOException e) {
            System.err.println("Erro ao enviar mensagem: " + e.getMessage());
            closeEverything();
        }
    }

    public void closeEverything() {
        try {
            if (objectIn != null) objectIn.close();
            if (objectOut != null) objectOut.close();
            if (socket != null) socket.close();
        } catch (IOException ignored) {}
    }

    public String getUsername() {
        return username;
    }

    public static void main(String[] args) {
        String serverIP = "localhost";
        int serverPort = 8008;

        int gameId = 1;
        int teamId = 2;
        String username = "2";
        Client client = new Client(serverIP, serverPort, gameId, teamId, username);
        new Thread(client).start();


    }
}
