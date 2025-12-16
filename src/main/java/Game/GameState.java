package Game;

import Utils.Constants;
import Utils.Records.*;
import java.util.ArrayList;

public class GameState {

    private final int gameCode;
    private final ArrayList<Team> teams = new ArrayList<>();
    private Pergunta[] questions;
    private int currentQuestionIndex = 0;

    public GameState(int numTeams, int playersPerTeam, int gameCode) {
        this.gameCode = gameCode;
        for (int i = 0; i < numTeams; i++) {
            teams.add(new Team("Team " + (i + 1), i));
        }
    }

    public ArrayList<Team> getTeams() {
        return teams;
    }

    public void setQuestions(Pergunta[] questions) {
        this.questions = questions;
    }

    public Pergunta getCurrentQuestion() {
        if (questions == null || currentQuestionIndex >= questions.length) return null;
        return questions[currentQuestionIndex];
    }

    public int getGameCode() {
        return gameCode;
    }

    public void registerAnswer(String username, int option) {
        for (Team team : teams) {
            for (Player p : team.getPlayers()) {
                if (p.getName().equals(username)) {
                    p.setChosenOption(option);
                    team.playerAnswered();
                    return;
                }
            }
        }
    }

    public RoundResult endRound() {
        Pergunta current = getCurrentQuestion();
        if (current == null) return null;

        ArrayList<String> playerNames = new ArrayList<>();
        ArrayList<Integer> playerScores = new ArrayList<>();

        for (Team team : teams) {
            try {
                team.awaitAll(); // espera que todos da equipa respondam ou timeout
            } catch (InterruptedException ignored) {}
            int teamScore = team.calculateQuestionScore(current);
            for (Player p : team.getPlayers()) {
                p.addScore(teamScore);
                playerNames.add(p.getName());
                playerScores.add(p.getScore());
            }
            team.resetChosenOptions();
        }

        currentQuestionIndex++;
        boolean gameEnded = currentQuestionIndex >= questions.length;

        // Constrói hashmap para SendRoundStats
        java.util.HashMap<String, Integer> scores = new java.util.HashMap<>();
        for (int i = 0; i < playerNames.size(); i++) {
            scores.put(playerNames.get(i), playerScores.get(i));
        }

        return new RoundResult(true, gameEnded, scores, getCurrentQuestion());
    }

    public SendQuestion createSendQuestion(int timeoutSeconds) {
        Pergunta current = getCurrentQuestion();
        if (current == null) return null;
        return new SendQuestion(current.getQuestion(), current.getOptions(), currentQuestionIndex, timeoutSeconds);
    }

    public SendFinalScores getFinalScores() {
        java.util.HashMap<String, Integer> finalScores = new java.util.HashMap<>();
        for (Team team : teams) {
            for (Player p : team.getPlayers()) {
                finalScores.put(p.getName(), p.getScore());
            }
        }
        return new SendFinalScores(finalScores);
    }
}
