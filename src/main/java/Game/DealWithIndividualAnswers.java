package Game;

import Utils.ModifiedCountdownLatch;

public class DealWithIndividualAnswers {
    private GameState gameState;
    private ModifiedCountdownLatch countDownLatch;

    public DealWithIndividualAnswers(GameState gameState, ModifiedCountdownLatch countDownLatch) {
        this.gameState = gameState;
        this.countDownLatch = countDownLatch;
    }

    public synchronized int registarRespostaIndividual(Player jogador, int opcaoEscolhida) {
        gameState.registarRespostaIndividual();

        int ordemResposta = gameState.getOrdemRespostas();

        boolean respostaCorreta = gameState.getPerguntaAtual().verificarResposta(opcaoEscolhida);

        int pontosPergunta = gameState.getPerguntaAtual().getPontos();
        int pontosGanhos;

        if(respostaCorreta) {
            if(ordemResposta == 1 || ordemResposta == 2) {
                pontosGanhos = pontosPergunta;
            } else if(ordemResposta == 3 || ordemResposta == 4) {
                pontosGanhos = pontosPergunta / 2;
            } else {
                pontosGanhos = 0;
            }
            jogador.adicionarPontos(pontosGanhos);
        } else {
            pontosGanhos = 0;
        }

        countDownLatch.countDown();

        return pontosGanhos;
    }
}
