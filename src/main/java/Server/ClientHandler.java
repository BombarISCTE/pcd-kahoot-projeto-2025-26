package Server;

import Client.Client;

import java.io.*;
import java.net.Socket;
import java.util.ArrayList;

public class ClientHandler extends Thread {

    public static ArrayList<ClientHandler> clientHandlers = new ArrayList<>(); // keep track of clients
    private Socket socket;
    private ObjectInputStream objectInputStream;
    private ObjectOutputStream objectOutputStream;
    private Client client;

    public ClientHandler(Socket socket){
        try {
            this.socket = socket;
            // create ObjectOutputStream first to avoid stream header deadlock
            this.objectOutputStream = new ObjectOutputStream(socket.getOutputStream());
            this.objectOutputStream.flush();
            this.objectInputStream = new ObjectInputStream(socket.getInputStream());

            // read initial Client object sent by the client
            this.client = (Client) objectInputStream.readObject();

            clientHandlers.add(this);
            broadcastMessage("SERVER: " + client.getUsername() + " has entered the chat!");
        } catch (Exception e) {
            closeEverything();
        }
    }

    @Override
    public void run() {
        Object messageFromClient;
        while (socket != null && socket.isConnected()) {
            try {
                messageFromClient = objectInputStream.readObject();
                if (messageFromClient != null) {
                    broadcastMessage(messageFromClient);
                }
            } catch (IOException | ClassNotFoundException e) {
                closeEverything();
                break;
            }
        }
    }

    public void broadcastMessage(Object messageToSend) {
        synchronized (clientHandlers) {
            for (ClientHandler clientHandler : clientHandlers) {
                try {
                    if (clientHandler.client != null && !clientHandler.client.equals(this.client)) {
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
        broadcastMessage("SERVER: " + (client != null ? client.getUsername() : "A client") + " has left the chat!");
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
        if (args.length == 4) {
            this.client = new Client(socket, args[0], Integer.parseInt(args[1]), args[2], Integer.parseInt(args[3]), args[4]);
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
}
