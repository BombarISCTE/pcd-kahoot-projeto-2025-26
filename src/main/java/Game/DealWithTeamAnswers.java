package Game;

import Utils.ModifiedBarrier;
import Utils.Timer;

public class DealWithTeamAnswers {

    private final GameState gameState;
    private final ModifiedBarrier barrier;
    private Timer timer;

    public DealWithTeamAnswers(GameState gameState, ModifiedBarrier barrier) {
        this.gameState = gameState;
        this.barrier = barrier;

//        this.barrier = new ModifiedBarrier(
//                totalJogadores, () -> aplicarPontuacao(equipaId)
//        );
    }

    public void iniciarPerguntaEquipa(){
        int tempo = gameState.getPerguntaAtual().getTempoLimite();
        timer = new Timer(tempo, barrier);
        timer.start();
    }

    public synchronized void registarRespostaEquipa(int equipaId, Player jogador, int opcaoEscolhida) throws InterruptedException {
        jogador.setOpcaoEscolhida(opcaoEscolhida);
        gameState.registarRespostaEquipa(equipaId);
        barrier.chegouJogador();
    }

    public void esperarRespostasEquipa(){
        try{
            barrier.await();
        }catch (InterruptedException e){
            Thread.currentThread().interrupt();
        }
    }

    private void aplicarPontuacao(int equipaId){
        Pergunta perguntaAtual = gameState.getPerguntaAtual();
        Player[] jogadores = gameState.getJogadores()[equipaId];

        int pontosPergunta = perguntaAtual.getPontos();
        int numRespostasCorretas = 0;

        for(Player jogador : jogadores){
            if(perguntaAtual.verificarResposta(jogador.getOpcaoEscolhida())){
                numRespostasCorretas++;
            }
        }
        int pontosGanhos;
        if(numRespostasCorretas == jogadores.length){
            pontosGanhos = pontosPergunta * 2;
        }else if(numRespostasCorretas > 0){
            pontosGanhos = pontosPergunta;
        }else {
            pontosGanhos = 0;
        }

        for(Player jogador : jogadores){
            jogador.setPontos(pontosGanhos);
        }

    }



}
