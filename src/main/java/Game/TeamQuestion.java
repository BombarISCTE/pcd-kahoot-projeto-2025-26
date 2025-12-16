package Game;

import Utils.Constants;
import Utils.ModifiedBarrier;
import java.util.ArrayList;

/**
 * Pergunta de equipa: basta um jogador acertar para a equipa ganhar pontos,
 * se todos acertarem, a pontuação é duplicada
 */
public class TeamQuestion extends Question {

    private final ArrayList<Team> teams;
    private ModifiedBarrier barrier;

    public TeamQuestion(String questionText, int correct, int points, String[] options, ArrayList<Team> teams) {
        super(questionText, correct, points, options);
        this.teams = teams;
        this.barrier = new ModifiedBarrier(countTotalPlayers(teams), () -> {}); // barrier sem ação, GameState chama processResponses
    }

    private synchronized int countTotalPlayers(ArrayList<Team> teams) {
        int total = 0;
        for (Team t : teams) total += t.getCurrentSize();
        return total;
    }

    /**
     * Chamado quando um jogador responde
     */
    public synchronized void playerAnswered(Player player, boolean correct) {
        player.setChosenOption(correct ? this.correct : -1);
        barrier.chegouJogador();
    }

    /**
     * Processa a pontuação de uma equipa específica
     */
    @Override
    public synchronized void processResponses(Team team) {
        ArrayList<Player> players = new ArrayList<>(team.getPlayers());

        boolean todosAcertaram = true;
        boolean peloMenosUmAcertou = false;

        // Verificar quem acertou
        for (Player p : players) {
            if (p.getChosenOption() != correct) {
                todosAcertaram = false;
            } else {
                peloMenosUmAcertou = true;
            }
        }
        int score = 0;
        if (peloMenosUmAcertou) {score = todosAcertaram ? points * Constants.BONUS_FACTOR : points;}

        for (Player p : players) {p.addScore(score);}
    }


    public ModifiedBarrier getBarrier() {
        return barrier;
    }
    public void setBarrier(ModifiedBarrier barrier) {this.barrier = barrier;}
}
