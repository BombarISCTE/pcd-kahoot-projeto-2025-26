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
    }

    public void iniciarPerguntaEquipa(){
        int tempo = gameState.getPerguntaAtual().getTempoLimite();
        timer = new Timer(tempo, barrier);
        timer.start();
    }

    public synchronized void registarRespostaEquipa(int equipaId, Player jogador, int opcaoEscolhida) throws InterruptedException {
        boolean respostaCorreta = gameState.getPerguntaAtual().verificarResposta(opcaoEscolhida);
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



}
