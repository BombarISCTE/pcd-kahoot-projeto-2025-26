package Server;

import Utils.Message;
import Utils.MessageType;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;

public class DealWithClient extends Thread{
    private Socket socket;
    private ObjectOutputStream out;
    private ObjectInputStream in;


    public DealWithClient(Socket socket) {
        this.socket = socket ;
        try {
            doConnections(socket);
        } catch (IOException e) {
            System.err.println("Erro ao criar canais: " + e.getMessage());
        }
    }


    private void doConnections(Socket socket) throws IOException {
        out = new ObjectOutputStream(socket.getOutputStream());
        out.flush();
        in = new ObjectInputStream(socket.getInputStream());
    }

    private void closeConnections() {
        System.out.println("A fechar conexão com " + socket.getInetAddress().getHostAddress() + "...");
        try {
            if (out != null) {
                out.close();
            }
            if (in != null) {
                in.close();
            }
            if (socket != null && !socket.isClosed()){
                socket.close();
            }
        } catch (IOException e) {
            System.err.println("Erro ao fechar conexões: " + e.getMessage());
        }
    }


    @Override
    public void run() {
        Message msg;
        try {
            while (true) {
                msg = (Message) in.readObject();
                MessageType tipo = msg.getTipo();

                if (tipo == MessageType.INICIO_JOGO) {
                    System.out.println("Inicio Jogo recebido do cliente.");
                } else if (tipo == MessageType.RESPOSTA) {
                    System.out.println("Resposta recebida do cliente.");
                } else if (tipo == MessageType.ATUALIZAR_PONTOS) {
                    System.out.println("Atualizar pontos recebido do cliente.");
                } else if (tipo == MessageType.NOVA_PERGUNTA) {
                    System.out.println("Nova pergunta recebida do cliente.");
                } else if (tipo == MessageType.FIM_JOGO) {
                    System.out.println("Fim de jogo recebido do cliente.");
                    break;
                } else {
                    System.out.println("Tipo de mensagem desconhecido.");
                }
            }
        } catch (IOException e) {
            System.err.println("Conexão perdida ou erro de I/O com o cliente " + socket.getInetAddress() + ": " + e.getMessage());
        } catch (ClassNotFoundException e) {
            System.err.println("Erro de desserialização (classe Message não encontrada): " + e.getMessage());
        } finally {
            closeConnections();
        }

    }
}

