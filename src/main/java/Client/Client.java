package Client;


import Utils.Constants;
import Utils.Messages.*;

import java.io.*;
import java.net.InetAddress;
import java.net.Socket;
import java.util.Scanner;

public class Client implements Runnable, Serializable {

    private Socket socket;
    private ObjectOutputStream objectOut;
    private ObjectInputStream objectIn;

    private String serverIP;
    private int serverPort;

    private int gameId;
    private int teamId;
    private String username;

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
            sendMessage(new ClientConnect(username, gameId, teamId));
            listenForMessages();
        } catch (Exception e) {
            System.err.println("Cliente encerrou: " + e.getMessage());
        } finally {
            closeEverything();
        }
    }

    private void connectToServer() throws IOException {
        InetAddress address = InetAddress.getByName(serverIP);
        socket = new Socket(address, serverPort);
        System.out.println("Conectado ao servidor: " + serverIP + ":" + serverPort);

        // Criar streams de objeto
        objectOut = new ObjectOutputStream(socket.getOutputStream());
        objectOut.flush();
        objectIn = new ObjectInputStream(socket.getInputStream());
    }



    private void listenForMessages() {
        new Thread(() -> {
            while (socket != null && socket.isConnected()) {
                try {
                    Object msg = objectIn.readObject();

                    if (msg instanceof SendQuestion sq) {
                        System.out.println("Pergunta #" + sq.getQuestionNumber() + ": " + sq.getQuestion());
                        String[] options = sq.getOptions();
                        for (int i = 0; i < options.length; i++) {
                            System.out.println((i + 1) + ". " + options[i]);
                        }
                        System.out.println("Tempo limite: " + sq.getTimeLimit() + "s");
                    } else if (msg instanceof String s) {
                        System.out.println("Mensagem do servidor: " + s);
                    } else {
                        System.out.println("Mensagem recebida de tipo desconhecido: " + msg);
                    }

                } catch (IOException | ClassNotFoundException e) {
                    System.err.println("Erro ao receber mensagem do servidor: " + e.getMessage());
                    closeEverything();
                    break;
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

    public int getGameId() {
        return gameId;
    }
    public int getTeamId() {return teamId;}

    // Exemplo de main para teste
    public static void main(String[] args) {
        Scanner sc = new Scanner(System.in);
        String ip = Constants.SERVER_IP;
        int port = Constants.SERVER_PORT;
        System.out.print("Enter game ID: ");
        int gameId = Integer.parseInt(sc.nextLine());
        System.out.print("Enter team ID: ");
        int teamId = Integer.parseInt(sc.nextLine());
        System.out.print("Enter your username: ");
        String username = sc.nextLine();

        Client client = new Client(ip, port, gameId, teamId, username);
        new Thread(client).start();
    }
}
