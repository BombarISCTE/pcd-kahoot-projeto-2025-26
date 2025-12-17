package Game;

import Utils.Constants;
import Utils.ModifiedBarrier;

import java.util.ArrayList;
import java.util.HashMap;

/**
 * Pergunta de equipa: basta um jogador acertar para a equipa ganhar pontos,
 * se todos acertarem, a pontuação é duplicada
 */
/*
Class: TeamQuestion (extends Question)

Public constructors:
 - TeamQuestion(String questionText, int correct, int points, String[] options)

Public / synchronized methods (signatures):
 - synchronized void addTeam(Team team)
 - synchronized void playerAnswered(Player player, boolean correct)
 - synchronized void initializeBarrier()
 - synchronized void processResponses(Team team)
 - ModifiedBarrier getBarrier()
 - void setBarrier(ModifiedBarrier barrier)
 - StringBuilder formatedClassName()

Notes:
 - Maintains internal map of teams and a ModifiedBarrier to wait for all responses or timeout.
 - processResponses assigns team score and resets per-player temporary states.
*/

public class TeamQuestion extends Question {

    private HashMap<Integer, Team> teams;
    private ModifiedBarrier barrier;

    public TeamQuestion(String questionText, int correct, int points, String[] options) {
        super(questionText, correct, points, options);
        this.teams = new HashMap<>();
        this.barrier = null;
        //this.barrier = new ModifiedBarrier(countTotalPlayers(teams), () -> {}); // barrier sem ação, GameState chama processResponses
    }

    private synchronized int countTotalPlayers(HashMap<Integer, Team> teams) {
        int total = 0;
        for (Team team : teams.values()) {
            total += team.getCurrentSize();
        }
        return total;
    }

    public synchronized void addTeam(Team team) {
        if (teams.containsKey(team.getTeamId())) {
            System.out.println("Team " + team.getTeamId() + " already exists, not readding and processing.");
            return;
        }
        teams.put(team.getTeamId(), team);
    }

    /**
     * Chamado quando um jogador responde
     */
    public synchronized void playerAnswered(Player player, boolean correct) {
        player.setChosenOption(correct ? this.correct : -1);
        barrier.chegouJogador();
    }

    /// vai ter de ser chamado no gamestate quando
    public synchronized void initializeBarrier() {
        if (this.barrier != null) {
            System.out.println("Barrier já foi inicializada.");
            return;
        }
        this.barrier = new ModifiedBarrier(countTotalPlayers(teams), () -> {});
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
        // Reset chosenOption for next question
        for (Player p : players) { p.resetChosenOption(); }
    }


    public ModifiedBarrier getBarrier() {
        return barrier;
    }
    public void setBarrier(ModifiedBarrier barrier) {this.barrier = barrier;}

    public StringBuilder formatedClassName(){return new StringBuilder("Pergunta de Equipa: ");}
}
