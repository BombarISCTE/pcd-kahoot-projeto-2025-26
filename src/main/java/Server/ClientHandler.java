package Server;

import Client.Client;

import Utils.Records.*;
import java.io.*;
import java.net.Socket;
import java.util.ArrayList;


public class ClientHandler extends Thread {

    public static ArrayList<ClientHandler> clientHandlers = new ArrayList<>(); // keep track of clients
    private Socket socket;
    private ObjectInputStream objectInputStream;
    private ObjectOutputStream objectOutputStream;
    private ClientConnect client;
    private int gameId; // associated game state -> nao faria sentido clientes receberem mensagens de outros jogos
    //private Map<String, GameState> gameStates = new HashMap<>();


    public ClientHandler(Socket socket) throws IOException, ClassNotFoundException {
            this.socket = socket;
            this.objectOutputStream = new ObjectOutputStream(socket.getOutputStream());
            this.objectOutputStream.flush();
            this.objectInputStream = new ObjectInputStream(socket.getInputStream());

            Object line = objectInputStream.readObject(); // bloqueante
            connectClient(line);
            this.gameId = client.gameId();

            synchronized (clientHandlers) {clientHandlers.add(this);}

            broadcastMessage("SERVER: " + client.username() + " has entered the chat!", gameId);

    }

    @Override
    public void run() {
        try {
            while (socket != null && !socket.isClosed()) {
                Object message = objectInputStream.readObject(); // bloqueante
                handleMessage(message);
            }
        } catch (IOException | ClassNotFoundException e) {
            System.out.println("Client disconnected: " + client.username());
        } finally {
            closeEverything();
        }
    }

    public void broadcastMessage(Serializable message, int gameId) {
        synchronized (clientHandlers) {
            for (ClientHandler ch : clientHandlers) {
                if (ch != this && ch.gameId == gameId) {
                    ch.sendMessage(message);
                }
            }
        }
    }

    public void removeClientHandler() {
        clientHandlers.remove(this);
        broadcastMessage("SERVER: " + (client != null ? client.username() : "A client") + " has left the chat!", gameId);
    }

    public void closeEverything() {
        try { synchronized (clientHandlers) {clientHandlers.remove(this);}

            if (objectInputStream != null) objectInputStream.close();
            if (objectOutputStream != null) objectOutputStream.close();
            if (socket != null && !socket.isClosed()) socket.close();

            broadcastMessage("SERVER: " + client.username() + " has left the game ", gameId);
        } catch (IOException ignored) {}
    }

    public void connectClient(Object line) {  //java clienteKahoot IP PORT Jogo Equipa Username
        if(line instanceof ClientConnect connect) {
            this.client = connect;
            this.gameId = connect.gameId();
        } else {
            refuseConnection(client);
        }
    }

    public void refuseConnection(ClientConnect client) {
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
            System.err.println("Erro ao enviar mensagem para " + client.username());
            closeEverything();
        }
    }


    private void handleMessage(Object message) {
        if (message instanceof GameStarted ){
            broadcastMessage((GameStarted) message, gameId);

        } else if (message instanceof ClientConnectAck ack) {
            broadcastMessage(ack, gameId);

        } else if (message instanceof SendQuestion sq) {
            broadcastMessage(sq, gameId);

        } else if (message instanceof SendRoundStats srs) {
            broadcastMessage(srs, gameId);

        } else if (message instanceof SendFinalScores sfs) {
            broadcastMessage(sfs, gameId);

        } else {
            System.out.println("Mensagem desconhecida recebida: " + message);
        }
    }


}
