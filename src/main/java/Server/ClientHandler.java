package Server;

import Messages.*;
import java.io.*;
import java.net.Socket;
import java.util.ArrayList;

public class ClientHandler extends Thread {

    public static final ArrayList<ClientHandler> clientHandlers = new ArrayList<>();

    private final Socket socket;
    private final ObjectInputStream in;
    private final ObjectOutputStream out;

    private final int gameId;
    private final int teamId;
    private final String username;

    private final Server server;

    public ClientHandler(Socket socket, Server server) {
        this.server = server;
        this.socket = socket;

        try {
            this.out = new ObjectOutputStream(socket.getOutputStream());
            this.out.flush();
            this.in = new ObjectInputStream(socket.getInputStream());

            ClientConnect connect = (ClientConnect) in.readObject();

            this.gameId = connect.getGameId();
            this.teamId = connect.getTeamId();
            this.username = connect.getUsername();

            synchronized (clientHandlers) {
                clientHandlers.add(this);
            }

            GameEngine engine = server.getGameEngine(gameId);
            if (engine == null) {
                sendMessage("Erro: jogo ainda não iniciado.");
                closeEverything();
                throw new IllegalStateException("GameEngine inexistente");
            }

            engine.registarJogador(teamId, username);

            System.out.println("Cliente ligado: " + username +
                    " | jogo " + gameId +
                    " | equipa " + teamId);

        } catch (Exception e) {
            throw new RuntimeException("Erro ao criar ClientHandler", e);
        }
    }

    @Override
    public void run() {
        try {
            while (!socket.isClosed()) {
                Object msg = in.readObject();
                handleMessage(msg);
            }
        } catch (Exception e) {
            closeEverything();
        }
    }

    private void handleMessage(Object msg) {
        if (msg instanceof Answer a) {
            System.out.println("Resposta recebida: " +
                    a.getUsername() +
                    " | equipa " + a.getTeamId() +
                    " | opção " + a.getOpcaoEscolhida());

            server.getGameEngine(gameId)
                    .registarResposta(
                            a.getUsername(),
                            a.getTeamId(),
                            a.getOpcaoEscolhida()
                    );
        } else {
            System.out.println("Mensagem desconhecida do cliente: " + msg);
        }
    }

    public void sendMessage(Serializable msg) {
        try {
            out.writeObject(msg);
            out.flush();
        } catch (IOException e) {
            closeEverything();
        }
    }

    private void closeEverything() {
        synchronized (clientHandlers) {
            clientHandlers.remove(this);
        }
        try { in.close(); } catch (Exception ignored) {}
        try { out.close(); } catch (Exception ignored) {}
        try { socket.close(); } catch (Exception ignored) {}

        System.out.println("Cliente desligado: " + username);
    }

    public int getGameId() {
        return gameId;
    }
}
