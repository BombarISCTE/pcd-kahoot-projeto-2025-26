package Game;

import Utils.ModifiedBarrier;
import Utils.Timer;

public class DealWithTeamAnswers {

    private final GameState gameState;
    private final ModifiedBarrier barrier;
    private Timer timer;
    private final int equipaId;
    private final GameEngine gameEngine;

    public DealWithTeamAnswers(GameState gameState, int equipaId, GameEngine gameEngine) {
        this.gameState = gameState;
        this.equipaId = equipaId;
        this.gameEngine = gameEngine;

        int totalJogadoresEquipa = gameState.getNumJogadoresEquipa();

        this.barrier = new ModifiedBarrier(
                totalJogadoresEquipa, () -> aplicarPontuacao()
        );

    }

    public void iniciarPerguntaEquipa(){
        barrier.reset();
        gameState.reporRespostasEquipa();
        gameState.reporOpcoesEscolhidas();
        int tempo = gameState.getPerguntaAtual().getMaxTimer();
        timer = new Timer(tempo, barrier);
        timer.start();
    }

    public void registarRespostaEquipa(Player jogador, int opcaoEscolhida) {
        if (jogador.getOpcaoEscolhida() != -1) {
            return;
        }

        jogador.setOpcaoEscolhida(opcaoEscolhida);
        gameState.registarRespostaEquipa(equipaId);
        barrier.chegouJogador();

        bloquearAteFimDaRonda();
    }

    public void bloquearAteFimDaRonda(){
        try{
            barrier.await();
        }catch (InterruptedException e){
            Thread.currentThread().interrupt();
        }
    }

    private void aplicarPontuacao(){
        Pergunta perguntaAtual = gameState.getPerguntaAtual();
        Player[] jogadores = gameState.getJogadoresDaEquipa(equipaId);
        Team[] equipas = gameState.getEquipas();

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
            jogador.adicionarPontos(pontosGanhos);
            gameEngine.getGameStatistics().atualizaPontosJogadores(jogador.getId(), pontosGanhos);
        }

        equipas[equipaId - 1].addPoints(pontosGanhos);

        gameEngine.equipaTerminou(equipaId);
    }



}
