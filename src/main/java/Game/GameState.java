package Game;

import Utils.Constants;
import Utils.Records.*;
import java.util.ArrayList;
import java.util.HashMap;

public class GameState {

    private final int gameCode;
    private final HashMap<Integer, Team> teamsMap; // teamId -> Team
    private final int numTeams;
    private final int playersPerTeam;

    private Pergunta[] questions;
    private int currentQuestionIndex = 0;

    public GameState(int numTeams, int playersPerTeam, int gameCode) {
        this.gameCode = gameCode;
        this.numTeams = numTeams;
        this.playersPerTeam = playersPerTeam;
        this.teamsMap = new HashMap<>();
    }

    // Adiciona um time manualmente
    public void addTeam(int teamId, String teamName) {
        if (teamsMap.containsKey(teamId)) {
            throw new IllegalArgumentException("Team ID already exists: " + teamId);
        }
        teamsMap.put(teamId, new Team(teamName, teamId, playersPerTeam));
    }

    public ArrayList<Team> getTeams() {return new ArrayList<>(teamsMap.values());}

    public Team getTeam(int teamId) {return teamsMap.get(teamId);}
    public int getNumTeams() {return numTeams;}
    public int getPlayersPerTeam() {return playersPerTeam;}

    public void setQuestions(Pergunta[] questions) {this.questions = questions;}

    public Pergunta getCurrentQuestion() {
        if (questions == null || currentQuestionIndex >= questions.length) return null;
        return questions[currentQuestionIndex];
    }

    public int getGameCode() {
        return gameCode;
    }

    public void registerAnswer(String username, int option) {
        for (Team team : teamsMap.values()) {
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

        for (Team team : teamsMap.values()) {
            try {
                team.awaitAll();
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

        HashMap<String, Integer> scores = new HashMap<>();
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
        HashMap<String, Integer> finalScores = new HashMap<>();
        for (Team team : teamsMap.values()) {
            for (Player p : team.getPlayers()) {
                finalScores.put(p.getName(), p.getScore());
            }
        }
        return new SendFinalScores(finalScores);
    }
}
