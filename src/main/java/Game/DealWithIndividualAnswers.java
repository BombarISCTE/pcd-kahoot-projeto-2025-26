package Game;

import Utils.ModifiedCountdownLatch;
import Utils.Timer;

public class DealWithIndividualAnswers {
    private GameState gameState;
    private ModifiedCountdownLatch latch;
    private Timer timer;

    public DealWithIndividualAnswers(GameState gameState, ModifiedCountdownLatch countDownLatch) {
        this.gameState = gameState;
        this.latch = countDownLatch;
    }

    public void iniciarPerguntaIndividual(){
        timer = new Timer(latch);
        timer.start();
    }

    public synchronized int registarRespostaIndividual(Player jogador, int opcaoEscolhida) {
        gameState.registarRespostaIndividual();

        boolean respostaCorreta = gameState.getPerguntaAtual().verificarResposta(opcaoEscolhida);

        int pontosPergunta = gameState.getPerguntaAtual().getPontos();
        int pontosGanhos = 0;

        int fator = latch.countDown();

        if(respostaCorreta) {
            pontosGanhos = pontosPergunta * fator;
            jogador.adicionarPontos(pontosGanhos);
        }

        return pontosGanhos;
    }

    public void esperarRespostasIndividuais(){
        try {
            latch.await();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

}
