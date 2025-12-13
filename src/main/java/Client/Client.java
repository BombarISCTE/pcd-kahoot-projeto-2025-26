package Client;

import Game.*;
import Messages.*;

import java.io.*;
import java.net.InetAddress;
import java.net.Socket;
import java.util.Scanner;

public class Client {
    private ObjectInputStream in;
    private ObjectOutputStream out;
    private Socket socket;

    private final String IP;
    private final int PORT;

    private final String codigoJogo;
    private final int equipaId;
    private String nomeJogador;
    private int jogadorId = -1;
    private ClientListener listener;

    public Client (String IP , int port, String codigoJogo, int equipaId, String nomeJogador) {
        this.IP = IP;
        this.PORT = port;
        this.codigoJogo = codigoJogo;
        this.equipaId = equipaId;
        this.nomeJogador = nomeJogador;
    }

    public void runClient(){
        try{
            connectToServer();
            enviarJoinGame();
            receberMensagens();
        } catch (Exception e){
            System.err.println("Erro no cliente");
        } finally {
           disconnect();
        }
    }

    private void connectToServer() {
        try {
            InetAddress endereco = InetAddress.getByName(IP);
            System.out.println("Endereço do servidor: " + endereco.getHostAddress());

            socket = new Socket(endereco, PORT);
            System.out.println("Ligação estabelecida ao servidor: " + IP + ":" + PORT);

            System.out.println("Socket:" + socket);
            out = new ObjectOutputStream(socket.getOutputStream());
            out.flush();
            in = new ObjectInputStream(socket.getInputStream());
            System.out.println("Ligado ao servidor");
        }catch (IOException e) {
            System.err.println("Falha em estabelecer ligação ao servidor: " + IP + " - " + e.getMessage());
        }
    }

    private void enviarJoinGame() {
        JoinGame joinGame = new JoinGame(codigoJogo, equipaId, nomeJogador);
        try {
            out.writeObject(joinGame);
            out.flush();
            System.out.println("JoinGame enviado ao servidor");
        } catch (IOException e) {
            System.err.println("Erro ao enviar JoinGame");
        }
    }

    private void receberMensagens() {
        while(true){
            try {
                Object mensagem = in.readObject();
                System.out.println("Mensagem recebida do servidor: " + mensagem);

                if(mensagem instanceof JoinGameResponse resposta){
                    jogadorId = resposta.getJogadorId();
                    nomeJogador = resposta.getNomeJogador();
                    System.out.println("Jogador " + jogadorId + " - " + nomeJogador + "entrou no jogo");
                }

                else if(mensagem instanceof NewQuestion novaQuestao){
                    tratarNovaQuestao(novaQuestao);
                }

                else if(mensagem instanceof EndGame fimJogo){
                    tratarFimJogo(fimJogo);
                    break;
                }

            } catch (IOException | ClassNotFoundException e) {
                System.err.println("Erro ao receber mensagem: " + e.getMessage());
                break;
            }
        }
    }

    public void setListener(ClientListener listener) {
        this.listener = listener;
    }

    private void tratarNovaQuestao(NewQuestion novaQuestao) {
        if(listener != null){
            listener.onNewQuestion(novaQuestao.getPergunta(), novaQuestao.getOpcoes(), novaQuestao.getNumeroPergunta(), novaQuestao.getTempoLimite());
        }
    }

    private void tratarFimJogo(EndGame fimJogo) {
        if(listener != null){
            listener.onEndGame(fimJogo.getMensagem());
        }
    }

    public void enviarResposta(int opcao) {
        try{
            Answer resposta = new Answer(jogadorId, equipaId, opcao);
            out.writeObject(resposta);
            out.flush();
            System.out.println("Resposta enviada: " + opcao);
        }catch (IOException e) {
            System.err.println("Erro ao enviar resposta");
        }
    }


    public void disconnect() {
        try {
            if (socket != null) {
                socket.close();
            }
            if (in != null) {
                in.close();
            }
            if (out != null) {
                out.close();
            }
            System.out.println("Desligado do servidor.");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }


}
