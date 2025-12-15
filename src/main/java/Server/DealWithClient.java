package Server;

//import Game.GameEngine;
import Game.Player;
import Utils.Messages.*;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;

//public class DealWithClient extends Thread{
//    private final Socket socket;
//    private ObjectOutputStream out;
//    private ObjectInputStream in;
//
//    private volatile boolean running = true;
//
//    private Player jogadorConectado;
//    private int equipaId;
//
//    private final Server server;
//    private GameEngine gameEngine;
//
//    public DealWithClient(Socket socket, Server server) {
//        this.socket = socket;
//        this.server = server;
//        try{
//            doConnections(socket);
//        }catch(IOException e){
//            System.out.println("Erro ao criar os canais");
//        }
//    }
//
//    private void doConnections(Socket socket) throws IOException {
//        out = new ObjectOutputStream(socket.getOutputStream());
//        out.flush();
//        in = new ObjectInputStream(socket.getInputStream());
//    }
//
//    @Override
//    public void run() {
//        try{
//            while(running) {
//                Object mensagem = in.readObject();
//                tratarMensagem(mensagem);
//            }
//        }catch (Exception e){
//            System.out.println("Erro ao carregar as mensagem");
//        }finally {
//            closeConnection();
//        }
//
//    }
//
//    private void tratarMensagem(Object mensagem) throws IOException {
//        if(mensagem instanceof JoinGame join){
//            if (this.gameEngine != null) {
//                System.out.println("Jogador já associado a um jogo");
//                enviarMensagem(new EndGame("Jogador já associado a um jogo"));
//                return;
//            }
//            GameEngine gameEngine = server.getGame(join.getCodigoJogo());
//            if(gameEngine == null){
//                System.out.println("Jogo nao encontrado");
//                enviarMensagem(new EndGame("Jogo nao encontrado"));
//                return;
//            }
//            Player jogador = gameEngine.registarJogador(join.getEquipaId(), join.getNomeJogador());
//            if(jogador == null){
//                System.out.println("Equipa cheia ou jogo ja comecou");
//                enviarMensagem(new EndGame("Equipa cheia ou jogo ja comecou"));
//                return;
//            }
//            this.gameEngine = gameEngine;
//            gameEngine.adicionarCliente(this);
//            this.jogadorConectado = jogador;
//            this.equipaId = join.getEquipaId();
//            System.out.println("Enviar resposta de JoinGameResponse");
//            enviarMensagem(new JoinGameResponse(jogador.getId(), equipaId, jogador.getName()));
//        }
//
//        else if(mensagem instanceof sendAnswer answer){
//            if(gameEngine == null || jogadorConectado == null){
//                System.out.println("Jogador nao conectado");
//                enviarMensagem(new EndGame("Jogador nao conectado"));
//                return;
//            }
//            System.out.println("Registrar resposta do jogador " + jogadorConectado.getName());
//            gameEngine.registarResposta(jogadorConectado, answer.getOpcaoEscolhida());
//        }
//
////        else if(mensagem instanceof ReadyNextQuestion nextQuestion){
////            System.out.println("Jogador " + jogadorConectado.getName() + " pronto para proxima pergunta");
////        }
//
//        else if(mensagem instanceof ExitGame exitGame){
//            System.out.println("Jogador " + jogadorConectado.getName() + " a sair do jogo");
//            running = false;
//            closeConnection();
//        }
//    }
//
//    private void closeConnection() {
//        try{
//            if(jogadorConectado != null){
//                jogadorConectado.desconectarJogador();
//            }
//            System.out.println("A fechar ligacao com o cliente");
//            socket.close();
//        }catch (IOException e){
//            System.out.println("Erro ao fechar a ligacao");
//        }
//    }
//
//    public synchronized void enviarMensagem(Object mensagem){
//        try{
//            out.writeObject(mensagem);
//            out.flush();
//        }catch (IOException e){
//            System.out.println("Erro ao enviar mensagem ao cliente");
//            closeConnection();
//        }
//    }
//
//}

