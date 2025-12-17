package Game;

import Utils.Constants;
import Utils.ModifiedBarrier;
import Utils.Timer;

public class DealWithTeamAnswers {

    private final GameState gameState;
    private final ModifiedBarrier barrier;
    private Timer timer;
    private final int equipaId;
    private int pontosGanhos; //add
//    private final GameStatistics gameStatistics; //del

    public DealWithTeamAnswers(GameState gameState, int equipaId /*, GameStatistics gameStatistics*/) {
        this.gameState = gameState;
        this.equipaId = equipaId;
//        this.gameStatistics = gameStatistics; //del


        int totalJogadoresEquipa = gameState.getNumJogadoresEquipa();

        this.barrier = new ModifiedBarrier(
                totalJogadoresEquipa, () -> aplicarPontuacao()
        );

    }

    public void iniciarPerguntaEquipa(){
        barrier.reset();
        pontosGanhos = 0; //add
        gameState.reporRespostasEquipa();
        gameState.reporOpcoesEscolhidas();
        int tempo = Constants.TIMOUT_SECS;
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
    }

    public void esperarAteFimDaRonda(){
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
//        int pontosGanhos; //del
        if(numRespostasCorretas == jogadores.length){
            pontosGanhos = pontosPergunta * 2;
        }else if(numRespostasCorretas > 0){
            pontosGanhos = pontosPergunta;
        }else {
            pontosGanhos = 0;
        }

        for(Player jogador : jogadores){
            jogador.adicionarPontos(pontosGanhos);
//            gameStatistics.atualizaPontosJogadores(jogador.getId(), pontosGanhos);
        }

        equipas[equipaId - 1].addPoints(pontosGanhos);

        //gameEngine.equipaTerminou(equipaId);
    }


    public int getPontosEquipa() {
        return pontosGanhos;
    }

    public Player[] getJogadoresEquipa() {
        return gameState.getJogadoresDaEquipa(equipaId);
    }
}