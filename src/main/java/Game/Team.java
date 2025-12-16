package Game;

import Utils.*;
import Utils.Records.*;

import java.util.ArrayList;

public class Team {
    private final int id;
    private final String name;
    private final ArrayList<Player> players = new ArrayList<>();
    private ModifiedBarrier barrier;

    public Team(String name, int id) {
        this.name = name;
        this.id = id;
    }

    public void addPlayer(Player p) {
        players.add(p);
    }

    public ArrayList<Player> getPlayers() {
        return players;
    }

    public int getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public void setupBarrier(int timeoutSeconds, Runnable action) {
        barrier = new ModifiedBarrier(players.size(), action);
        // Timer para expirar o tempo da equipa
        new Thread(() -> {
            try {
                Thread.sleep(timeoutSeconds * 1000);
                barrier.tempoExpirado();
            } catch (InterruptedException ignored) {}
        }).start();
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

    public int calculateQuestionScore(Pergunta question) {
        int maxScore = 0;
        boolean allCorrect = true;
        for (Player p : players) {
            if (question.verificarResposta(p.getChosenOption())) {
                maxScore = Math.max(maxScore, question.getPoints());
            } else {
                allCorrect = false;
            }
        }
        // Se todos acertaram, pontuação duplicada
        return allCorrect ? question.getPoints() * 2 : maxScore;
    }

    public void resetChosenOptions() {
        for (Player p : players) {
            p.resetChosenOption();
        }
    }

    public int size() {
        return players.size();
    }
}
