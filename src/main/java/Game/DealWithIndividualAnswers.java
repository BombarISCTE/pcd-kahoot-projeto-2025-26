package Game;

import Utils.ModifiedCountdownLatch;
import Utils.Timer;

//public class DealWithIndividualAnswers {
//    private GameState gameState;
//    private ModifiedCountdownLatch latch;
//    private Timer timer;
//    private GameStatistics gameStatistics;
//
//    public DealWithIndividualAnswers(GameState gameState, ModifiedCountdownLatch countDownLatch, GameStatistics gameStatistics) {
//        this.gameState = gameState;
//        this.latch = countDownLatch;
//        this.gameStatistics = gameStatistics;
//    }
//
//    public void iniciarPerguntaIndividual(){
//        int totalJogadores = gameState.getNumEquipas() * gameState.getNumJogadoresEquipa();
//        latch.reset(totalJogadores);
//        gameState.reporRespostas();
//        gameState.reporOpcoesEscolhidas();
//        timer = new Timer(latch);
//        timer.start();
//    }
//
//    public int registarRespostaIndividual(Player jogador, int opcaoEscolhida) {
//        if(jogador.getOpcaoEscolhida() != -1){
//            return 0;
//        }
//
//        jogador.setOpcaoEscolhida(opcaoEscolhida);
//        int ordemResposta = gameState.registarRespostaIndividual();
//
//        boolean respostaCorreta = gameState.getPerguntaAtual().verificarResposta(opcaoEscolhida);
//
//        int pontosPergunta = gameState.getPerguntaAtual().getPontos();
//        int pontosGanhos = 0;
//
//        int fator = latch.countDown();
//
//        if(respostaCorreta) {
//            pontosGanhos = pontosPergunta * fator;
//            jogador.adicionarPontos(pontosGanhos);
//            gameStatistics.atualizaPontosJogadores(jogador.getId(), pontosGanhos);
//        }
//
//        return pontosGanhos;
//    }
//
//    public void esperarRespostasIndividuais(){
//        try {
//            latch.await();
//        } catch (InterruptedException e) {
//            e.printStackTrace();
//        }
//    }
//
//}