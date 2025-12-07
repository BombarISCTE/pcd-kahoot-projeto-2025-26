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
        timer = new Timer(barrier);
        timer.start();
    }

//public syncronized void registarRespostaEquipa(Team equipa, int opcaoEscolhida) {
//        gameState
//    }

}
