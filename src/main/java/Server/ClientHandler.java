package Server;

import Utils.Records;
import Utils.Records.*;
import java.io.*;
import java.net.Socket;
import java.util.ArrayList;


public class ClientHandler extends Thread {

    public static ArrayList<ClientHandler> clientHandlers = new ArrayList<>(); // keep track of clients
    private Socket socket;
    private ObjectInputStream objectInputStream;
    private ObjectOutputStream objectOutputStream;
    private ClientConnect clientConnected;
    private int gameId; // associated game state -> nao faria sentido clientes receberem mensagens de outros jogos
    //private Map<String, GameState> gameStates = new HashMap<>();


    public ClientHandler(Socket socket) throws IOException, ClassNotFoundException {
            this.socket = socket;
            this.objectOutputStream = new ObjectOutputStream(socket.getOutputStream());
            this.objectOutputStream.flush();
            this.objectInputStream = new ObjectInputStream(socket.getInputStream());

            Object line = objectInputStream.readObject(); // bloqueante
            connectClient(line);
            this.gameId = clientConnected.gameId();

            synchronized (clientHandlers) {clientHandlers.add(this);}

            broadcastMessage("SERVER: " + clientConnected.username() + " has entered the chat!", gameId);

    }

    @Override
    public void run() {
            while (socket != null && !socket.isClosed()) {
                try {
                    Object message = objectInputStream.readObject();
                    handleMessage(message);
                } catch (ClassNotFoundException e) {
                    System.out.println("Mensagem desconhecida recebida: " + e.getMessage());
                } catch (IOException e) {
                    System.out.println("Client disconnected: " + clientConnected.username());
                    break; // sair do loop mas não fechar tudo ainda
                }
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

    public synchronized void removeClientHandler() {clientHandlers.remove(this);}

    public void closeEverything() {
        try {
            removeClientHandler();
            if (objectInputStream != null) objectInputStream.close();
            if (objectOutputStream != null) objectOutputStream.close();
            if (socket != null && !socket.isClosed()) socket.close();

            broadcastMessage("SERVER: " + clientConnected.username() + " has left the game", gameId);
        } catch (IOException ignored) {}
    }


    public void connectClient(Object line) {  //java clienteKahoot IP PORT Jogo Equipa Username
        if(line instanceof ClientConnect connect) {
            this.clientConnected = connect;
            this.gameId = connect.gameId();
        } else {
            refuseConnection("unable to connect client. arg passed: "+line);
        }
    }

    public void refuseConnection(String reason) {
        try {
            if (objectOutputStream != null) {
                objectOutputStream.writeObject(new Records.refuseConnection(reason));
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
            System.err.println("Erro ao enviar mensagem para " + clientConnected.username());
            closeEverything();
        }
    }


    private void handleMessage(Object message) {
        switch (message) {
            case GameStarted gameStarted -> broadcastMessage(gameStarted, gameId);
            case ClientConnectAck ack -> broadcastMessage(ack, gameId);
            case SendQuestion sq -> broadcastMessage(sq, gameId);
            case SendRoundStats srs -> broadcastMessage(srs, gameId);
            case SendFinalScores sfs -> broadcastMessage(sfs, gameId);
            case GameEnded gameEnded -> broadcastMessage(gameEnded, gameId);
            case null, default -> System.out.println("Mensagem desconhecida recebida: " + message);
        }
    }


}
