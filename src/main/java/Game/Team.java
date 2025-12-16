package Game;

import Utils.ModifiedBarrier;
import Utils.Constants;
import java.util.ArrayList;
import java.util.Collection;

public class Team {

    private final String name;
    private final int teamId;
    private final ArrayList<Player> players = new ArrayList<>();
    private ModifiedBarrier barrier;
    private final int maxPlayersPerTeam;

    public Team(String name, int teamId, int maxPlayersPerTeam) {
        this.name = name;
        this.teamId = teamId;
        this.maxPlayersPerTeam = maxPlayersPerTeam;
    }

    public void addPlayer(Player player) {
        if (players.size() >= maxPlayersPerTeam) {
            throw new IllegalStateException("Team is full");
        }
        players.add(player);
    }

    public ArrayList<Player> getPlayers() {
        return players;
    }

    public void startNewQuestion() {
        barrier = new ModifiedBarrier(players.size(), () -> {});
    }

    public void playerAnswered() {
        if (barrier != null) {
            barrier.chegouJogador();
        }
    }

    public void awaitAll() throws InterruptedException {
        if (barrier != null) {
            barrier.await();
        }
    }

    public void timeout() {
        if (barrier != null) {
            barrier.tempoExpirado();
        }
    }

    public boolean isRoundFinished() {
        return barrier != null && barrier.isCompleted();
    }

    public void startNewQuestion(Runnable action) {
        barrier = new ModifiedBarrier(players.size(), action);
    }


    public int calculateQuestionScore(Pergunta pergunta) {
        int correctCount = 0;
        int bestScore = Integer.MIN_VALUE;

        for (Player p : players) {
            if (pergunta.verificarResposta(p.getChosenOption())) {
                correctCount++;
                bestScore = Math.max(bestScore, Constants.RIGHT_ANSWER_POINTS);
            }
        }

        // equipa de 1 pessoa → nunca há bónus
        if (players.size() > 1 && correctCount == players.size()) {
            return Constants.RIGHT_ANSWER_POINTS * 2;
        }

        if (correctCount > 0) {
            return Constants.RIGHT_ANSWER_POINTS;
        }

        return 0;
    }

    public void resetChosenOptions() {
        for (Player p : players) {
            p.resetChosenOption();
        }
    }

    public String getName() {
        return name;
    }

    public int getTeamId() {
        return teamId;
    }

    public int getMaxPlayersPerTeam() {
        return maxPlayersPerTeam;
    }


}
