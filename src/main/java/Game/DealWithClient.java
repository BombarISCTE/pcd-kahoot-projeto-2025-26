package Game;

import Messages.*;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.util.List;

public class DealWithClient extends Thread{
    private final Socket socket;
    private ObjectOutputStream out;
    private ObjectInputStream in;

    private final GameState gameState;
    private final GameEngine gameEngine;

    private Player jogadorConectado;
    private int equipaId;

    public DealWithClient(Socket socket, GameState gameState, GameEngine gameEngine) {
        this.socket = socket;
        this.gameState = gameState;
        this.gameEngine = gameEngine;
        try{
            doConnections();
        }catch(IOException e){
            System.out.println("Erro ao criar os canais");
        }
    }

    public void doConnections() throws IOException {
        out = new ObjectOutputStream(socket.getOutputStream());
        out.flush();
        in = new ObjectInputStream(socket.getInputStream());
    }

    @Override
    public void run() {
        try{
            while(true) {
                Object mensagem = in.readObject();
                tratarMensagem(mensagem);
            }
        }catch (Exception e){
            System.out.println("Erro ao carregar as mensagem");
            fecharLigacao();
            e.printStackTrace();
        }

    }

    private void tratarMensagem(Object mensagem) throws IOException {
        if(mensagem instanceof JoinGame join){
            Player jogador = gameState.ocuparSlotJogador(join.getEquipaId(), join.getNomeJogador());
            if(jogador == null){
                System.out.println("Equipa cheia ou jogo ja comecou");
                out.writeObject(new EndGame("Equipa cheia ou jogo ja comecou"));
                return;
            }
            this.jogadorConectado = jogador;
            this.equipaId = join.getEquipaId();
            System.out.println("Enviar resposta de JoinGameResponse");
            out.writeObject(new JoinGameResponse(jogador.getId(), equipaId, jogador.getName()));
        }

        else if(mensagem instanceof Answer answer){
            if(jogadorConectado == null){
                System.out.println("Jogador nao conectado");
                out.writeObject(new EndGame("Jogador nao conectado"));
                return;
            }
            System.out.println("Registrar resposta do jogador " + jogadorConectado.getName());
            gameEngine.registarResposta(jogadorConectado, answer.getOpcaoEscolhida());
        }

        else if(mensagem instanceof ReadyNextQuestion nextQuestion){
            System.out.println("Jogador " + jogadorConectado.getName() + " pronto para proxima pergunta");
            gameEngine.jogadorProntoParaProximaPergunta(jogadorConectado);
        }

        else if(mensagem instanceof ExitGame exitGame){
            System.out.println("Jogador " + jogadorConectado.getName() + " a sair do jogo");
            fecharLigacao();
        }
    }

    private void fecharLigacao() {
        try{
            if(jogadorConectado != null){
                jogadorConectado.desconectarJogador();
            }
            System.out.println("A fechar ligacao com o cliente");
            socket.close();
        }catch (IOException e){
            System.out.println("Erro ao fechar a ligacao");
        }
    }

}
