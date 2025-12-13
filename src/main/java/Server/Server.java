package Server;


import Game.GameEngine;
import Utils.IdCodeGenerator;

import java.io.*;
import java.net.*;

import java.util.HashMap;

public class Server {
    private static final int PORT = 8080;

    private HashMap<String, GameEngine> jogosAtivos;

    public Server() {
        jogosAtivos = new HashMap<>();
    }

    public void startServer() {
        System.out.println("Server started on port " + PORT);
        try(ServerSocket serverSocket = new ServerSocket(PORT)) {
            while (true) {
                Socket socket = serverSocket.accept();
                System.out.println("Server - New client connected: " + socket.getInetAddress().getHostAddress());

                DealWithClient clientHandler = new DealWithClient(socket, this);
                clientHandler.start();
            }
        } catch (IOException e) {
            System.err.println("Erro no servidor: " + e.getMessage());
            e.printStackTrace();
        }
    }

    public synchronized GameEngine getGame(String codigoJogo) {
        return jogosAtivos.get(codigoJogo);
    }

    public synchronized String criarNovoJogo(GameEngine gameEngine) {
        String codigoJogo = IdCodeGenerator.gerarCodigo();
        jogosAtivos.put(codigoJogo, gameEngine);
        return codigoJogo;
    }

}
