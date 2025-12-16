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
            gui = new ClientGUI(this);
            listenForMessages();
        } catch (Exception e) {
            System.err.println("C run - Cliente encerrou: " + e.getMessage());
        } finally {
            closeEverything();
        }
    }

    private void connectToServer() throws IOException {
        socket = new Socket(serverIP, serverPort);
        System.out.println("C connectedToServer - Conectado ao servidor: " + serverIP + ":" + serverPort);

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
                    System.out.println("C listenForMessages - Mensagem do tipo " + msg.getClass().getSimpleName() + " recebida do servidor.");

                    switch (msg.getClass().getSimpleName()) {
                        case "SendTeamQuestion" -> {
                            SendTeamQuestion question = (SendTeamQuestion) msg;
                            gui.mostrarNovaPergunta(question);
                        }

                        case "SendIndividualQuestion" -> {
                            SendIndividualQuestion question = (SendIndividualQuestion) msg;
                            gui.mostrarNovaPergunta(question);
                        }

                        case "SendRoundStats" -> {
                            SendRoundStats srs = (SendRoundStats) msg;
                            gui.atualizarPlacar(new HashMap<>(srs.playerScores()));
                        }

                        case "SendFinalScores" -> {
                            SendFinalScores sfs = (SendFinalScores) msg;
                            gui.gameEnded(new HashMap<>(sfs.finalScores()));
                            closeEverything();
                        }

                        case "ClientConnectAck" -> {
                            ClientConnectAck ack = (ClientConnectAck) msg;
                            gui.setConnectedPlayers(ack.connectedPlayers());
                        }
                        case "FatalErrorMessage" -> {
                            FatalErrorMessage error = (FatalErrorMessage) msg;
                            System.err.println("C - listenForMessages - Erro do servidor: " + error.message());
                            closeEverything();
                        }

                        default -> System.out.println("C listenForMessages - Mensagem recebida de tipo desconhecido: " + msg);
                    }

                } catch (IOException e) { //esta a lancar ioexception
                    System.err.println("C listenForMessages - Erro ao receber mensagem do servidor: " + e.getMessage());
                    closeEverything();
                    break;
                } catch (ClassNotFoundException e) {
                    System.err.println("C listenForMessages - Classe não encontrada ao receber mensagem: " + e.getMessage());
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
            System.err.println("C sendMessage- Erro ao enviar mensagem: " + e.getMessage());
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

        int gameId = 1000000;
        int teamId = 2;
        String username = "2";
        Client client = new Client(serverIP, serverPort, gameId, teamId, "player1");
        new Thread(client).start();

        Client client2 = new Client(serverIP, serverPort, gameId+1, teamId, "Player2");
        new Thread(client2).start();


    }
}
