package Game;

import Utils.ModifiedBarrier;
import Utils.Timer;

public class DealWithTeamAnswers {

    private final GameState gameState;
    private final ModifiedBarrier barrier;
    private Timer timer;
    private final int indiceEquipa;

    public DealWithTeamAnswers(GameState gameState, int equipaId) {
        this.gameState = gameState;
        int totalJogadores = gameState.getNumJogadoresEquipa();
        this.indiceEquipa = equipaId - 1;

        this.barrier = new ModifiedBarrier(
                totalJogadores, () -> aplicarPontuacao(indiceEquipa)
        );

    }

    public void iniciarPerguntaEquipa(){
        int tempo = gameState.getPerguntaAtual().getTempoLimite();
        timer = new Timer(tempo, barrier);
        timer.start();
    }

    public void registarRespostaEquipa(Player jogador, int opcaoEscolhida) throws InterruptedException {
        jogador.setOpcaoEscolhida(opcaoEscolhida);
        gameState.registarRespostaEquipa(indiceEquipa + 1);
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
        Player[] jogadores = gameState.getJogadoresDaEquipa(equipaId);

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
        }


    }



}
