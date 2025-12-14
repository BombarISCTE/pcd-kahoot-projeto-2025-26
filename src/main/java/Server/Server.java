package Server;


import Game.GameEngine;
import Utils.Constants;
import Utils.IdCodeGenerator;

import java.io.*;
import java.net.*;

import java.util.HashMap;

public class Server {
    private static final int PORT = Constants.SERVER_PORT;
    private ServerSocket serverSocket;

    private HashMap<String, GameEngine> gameList;

    public Server(ServerSocket serverSocket) {
        gameList = new HashMap<>();
        this.serverSocket = serverSocket;
    }


    public void startServer() {

        try {

            while (!serverSocket.isClosed()) {
                Socket socket = serverSocket.accept();
                System.out.println("Server - New client connected: " + socket.getInetAddress().getHostAddress());
                ClientHandler clientHandler = new ClientHandler(socket);

                Thread thread = new Thread(clientHandler);
                thread.start();
            }
        } catch (IOException e) {
            System.out.println("Erro ao iniciar o servidor: " + e.getMessage());
            e.printStackTrace();
        }
    }

    public void closeServer() {
        try {
            if (serverSocket != null){
                serverSocket.close();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public synchronized GameEngine getGame(String codigoJogo) {
        return gameList.get(codigoJogo);
    }

    public synchronized String criarNovoJogo(GameEngine gameEngine) {
        String codigoJogo = IdCodeGenerator.gerarCodigo();
        gameList.put(codigoJogo, gameEngine);
        return codigoJogo;
    }

    public static void main(String[] args) throws IOException {
        ServerSocket serverSocket = new ServerSocket(PORT);
        Server server = new Server(serverSocket);
        server.startServer();
    }

}
