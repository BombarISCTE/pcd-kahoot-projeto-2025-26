package Game;

import Utils.ModifiedBarrier;
import java.util.ArrayList;

public class Team {

    private final String name;
    private final int teamId;
    private final int maxPlayersPerTeam;
    private final ArrayList<Player> players = new ArrayList<>();
    private ModifiedBarrier barrier;

    public Team(String name, int teamId, int maxPlayersPerTeam) {
        this.name = name;
        this.teamId = teamId;
        this.maxPlayersPerTeam = maxPlayersPerTeam;
    }

    public void addPlayer(Player player) {
        if (players.size() >= maxPlayersPerTeam) throw new IllegalStateException("Team full");
        players.add(player);
    }

    public ArrayList<Player> getPlayers() { return players; }

    /* Inicia uma nova pergunta com ação da barreira */
    public void startNewQuestion(Runnable barrierAction) {
        barrier = new ModifiedBarrier(players.size(), barrierAction);
        resetChosenOptions();
    }

    public void playerAnswered() {
        if (barrier != null) barrier.chegouJogador();
    }

    public void awaitAll() throws InterruptedException {
        if (barrier != null) barrier.await();
    }

    public void timeout() {
        if (barrier != null) barrier.tempoExpirado();
    }

    public boolean isRoundFinished() {
        return barrier != null && barrier.isCompleted();
    }

    public void setPlayersRoundScore(int score) {
        for (Player p : players) p.addScore(score);
    }

    public void resetChosenOptions() {
        for (Player p : players) p.resetChosenOption();
    }

    public int calculateQuestionScore(Pergunta pergunta) {
        int correctCount = 0;
        int bestScore = Integer.MIN_VALUE;

        for (Player p : players) {
            if (pergunta.verificarResposta(p.getChosenOption())) {
                correctCount++;
                bestScore = Math.max(bestScore, pergunta.getPoints());
            }
        }

        if (players.size() > 1 && correctCount == players.size()) return bestScore * 2;
        if (correctCount > 0) return bestScore;
        return 0;
    }

    public String getName() { return name; }
    public int getTeamId() { return teamId; }
    public int getMaxPlayersPerTeam() { return maxPlayersPerTeam; }
}
