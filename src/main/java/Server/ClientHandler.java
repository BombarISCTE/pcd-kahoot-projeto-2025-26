package Server;

import Client.Client;

import java.io.*;
import java.net.Socket;
import java.util.ArrayList;

public class ClientHandler extends Thread {

    public static ArrayList<ClientHandler> clientHandlers = new ArrayList<>(); //keep track of clients
    private Socket socket;
    private BufferedReader bufferedReader;
    private BufferedWriter bufferedWriter;
    private Client client;

    public ClientHandler(Socket socket){
        try {
            this.socket = socket;
            this.bufferedWriter = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream()));
            this.bufferedReader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            this.client = (Client) new ObjectInputStream(socket.getInputStream()).readObject(); // todo ler o cliente
            //connectClient(bufferedReader.readLine());
            clientHandlers.add(this);
            broadcastMessage("SERVER: " + client.getUsername() + " has entered the chat!");
        } catch (Exception e) {
            closeEverything(socket, bufferedReader, bufferedWriter);
        }
    }


    @Override
    public void run() {
        String messageFromClient;
        while (socket.isConnected()) {
            try {
                messageFromClient = bufferedReader.readLine();
                broadcastMessage(messageFromClient);
            } catch (IOException e) {
                closeEverything(socket, bufferedReader, bufferedWriter);
                break;
            }
        }
    }

    public void broadcastMessage(String messageToSend) {
        for (ClientHandler clientHandler : clientHandlers) {
            try {
                if (!clientHandler.client.equals(client)) {
                    clientHandler.bufferedWriter.write(messageToSend);
                    clientHandler.bufferedWriter.newLine();
                    clientHandler.bufferedWriter.flush();
                }
            } catch (IOException e) {
                closeEverything(socket, bufferedReader, bufferedWriter);
            }

        }
    }

    public void removeClientHandler() {
        clientHandlers.remove(this);
        broadcastMessage("SERVER: " + client.getUsername() + " has left the chat!");
    }

    public void closeEverything(Socket socket, BufferedReader bufferedReader, BufferedWriter bufferedWriter) {
        removeClientHandler();
        try {
            if (bufferedReader != null) {
                bufferedReader.close();
            }
            if (bufferedWriter != null) {
                bufferedWriter.close();
            }
            if (socket != null) {
                socket.close();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
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
            bufferedWriter.write("Connection refused: Invalid arguments.");
            bufferedWriter.newLine();
            bufferedWriter.flush();
        } catch (IOException e) {
            e.printStackTrace();
            closeEverything(socket, bufferedReader, bufferedWriter);
        }
    }




}
