package Server;

import Client.Client;
import Game.GameState;
import Utils.Messages.*;

import java.io.*;
import java.net.Socket;
import java.util.ArrayList;

public class ClientHandler extends Thread {

    public static ArrayList<ClientHandler> clientHandlers = new ArrayList<>(); // keep track of clients
    private Socket socket;
    private ObjectInputStream objectInputStream;
    private ObjectOutputStream objectOutputStream;
    private Client client;
    private int gameId; // associated game state -> nao faria sentido clientes receberem mensagens de outros jogos

    public ClientHandler(Socket socket) {
        try {
            this.socket = socket;

            this.objectOutputStream = new ObjectOutputStream(socket.getOutputStream());
            this.objectOutputStream.flush();
            this.objectInputStream = new ObjectInputStream(socket.getInputStream());

            this.client = (Client) objectInputStream.readObject();
            this.gameId = client.getGameId();

            clientHandlers.add(this);
            broadcastMessage("SERVER: " + client.getUsername() + " has entered the chat!", gameId);
        } catch (Exception e) {
            closeEverything();
        }
    }

    @Override
    public void run() {
        Object message;
        while (socket != null && socket.isConnected()) {
            try {
                message = objectInputStream.readObject();
                handleMessage(message);
            } catch (IOException | ClassNotFoundException e) {
                closeEverything();
                break;
            }
        }
    }

    public void broadcastMessage(Serializable messageToSend, int gameId) {
        synchronized (clientHandlers) {
            for (ClientHandler clientHandler : clientHandlers) {
                try {
                    if (clientHandler.client != null
                            && !clientHandler.client.equals(this.client)
                            && clientHandler.gameId == gameId) {

                        clientHandler.objectOutputStream.writeObject(messageToSend);
                        clientHandler.objectOutputStream.flush();
                    }
                } catch (IOException e) {
                    clientHandler.closeEverything();
                }
            }
        }
    }

    public void removeClientHandler() {
        clientHandlers.remove(this);
        broadcastMessage("SERVER: " + (client != null ? client.getUsername() : "A client") + " has left the chat!", gameId);
    }

    public void closeEverything() {
        removeClientHandler();
        try {
            if (objectInputStream != null) objectInputStream.close();
        } catch (IOException ignored) {}
        try {
            if (objectOutputStream != null) objectOutputStream.close();
        } catch (IOException ignored) {}
        try {
            if (socket != null) socket.close();
        } catch (IOException ignored) {}
    }

    public void connectClient(String line) {  //java clienteKahoot IP PORT Jogo Equipa Username
        String [] args = line.split(" ");
        if (args.length == 5) {
            this.client = new Client(args[0], Integer.parseInt(args[1]), Integer.parseInt(args[2]), Integer.parseInt(args[3]), args[4]);
        } else {
            refuseConnection();
        }
    }

    public void refuseConnection() {
        try {
            if (objectOutputStream != null) {
                objectOutputStream.writeObject("Connection refused: Invalid arguments.");
                objectOutputStream.flush();
            }
        } catch (IOException e) {
            closeEverything();
        }
    }

    public void sendMessage(Serializable message) {
        try {
            if (objectOutputStream != null) {
                objectOutputStream.writeObject(message);
                objectOutputStream.flush();
            }
        } catch (IOException e) {
            closeEverything();
        }
    }

//    void setGameId(int gameId) { //package-private para so ser acessivel pelo Server
//        this.gameId = gameId;
//    }

    private void handleMessage(Object message) {
        switch (message) {
//            case ClientConnectAck joined -> handleClientConnectAck(joined);
//            case SendQuestion sq -> handleSendQuestion(sq);
//            case SendRoundStats srs -> handleSendRoundStats(srs);
//            case SendFinalScores sfs -> handleSendFinalScores(sfs);
            case ClientConnectAck joined -> broadcastMessage(joined, gameId);
            case SendQuestion sm -> broadcastMessage(sm, gameId);
            case SendRoundStats srs -> broadcastMessage(srs, gameId);
            case SendFinalScores sfs -> broadcastMessage(sfs, gameId);

            default -> System.out.println("Mensagem desconhecida recebida: " + message);
        }
    }


}
