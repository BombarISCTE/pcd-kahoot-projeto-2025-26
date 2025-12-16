package Game;

import Utils.ModifiedBarrier;
import Utils.Records.*;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.concurrent.atomic.AtomicInteger;

public class GameState {

    private final int gameCode;
    private final int numTeams;
    private final int playersPerTeam;
    private final HashMap<Integer, Team> teamsMap;

    private Pergunta[] questions;
    private int currentQuestionIndex = 0;

    /* Atomic counter para ordem de resposta em perguntas individuais */
    private final AtomicInteger responseCounter = new AtomicInteger(0);

    public GameState(int numTeams, int playersPerTeam, int gameCode) {
        this.numTeams = numTeams;
        this.playersPerTeam = playersPerTeam;
        this.gameCode = gameCode;
        this.teamsMap = new HashMap<>();
    }

    /* =========================
       Equipas
       ========================= */

    public void addTeam(int teamId, String teamName) {
        if (teamsMap.containsKey(teamId)) throw new IllegalArgumentException("Team exists: " + teamId);
        teamsMap.put(teamId, new Team(teamName, teamId, playersPerTeam));
    }

    public Team getTeam(int teamId) { return teamsMap.get(teamId); }

    public ArrayList<Team> getTeams() { return new ArrayList<>(teamsMap.values()); }

    public int getGameCode() { return gameCode; }

    /* =========================
       Perguntas
       ========================= */

    public void setQuestions(Pergunta[] questions) { this.questions = questions; }

    public Pergunta getCurrentQuestion() {
        if (questions == null || currentQuestionIndex >= questions.length) return null;
        return questions[currentQuestionIndex];
    }

    public boolean isCurrentQuestionIndividual() { return currentQuestionIndex % 2 == 0; }

    /* =========================
       Registro de respostas
       ========================= */

    public void registerAnswer(String username, int option) {
        Pergunta current = getCurrentQuestion();
        if (current == null) return;

        for (Team team : teamsMap.values()) {
            for (Player p : team.getPlayers()) {
                if (p.getName().equals(username)) {

                    p.setChosenOption(option);

                    if (isCurrentQuestionIndividual() && current instanceof Pergunta.PerguntaIndividual) {
                        int order = responseCounter.incrementAndGet();
                        p.setResponseOrder(order);

                        int base = current.getPointsForAnswer(option);
                        if (order <= 2 && base > 0) base *= 2; // bónus 2 primeiros
                        p.addScore(base);
                    } else if (!isCurrentQuestionIndividual()) {
                        // Pergunta de equipa → chama barrier
                        team.playerAnswered();
                    }

                    return;
                }
            }
        }
    }

    /* =========================
       Fim de ronda
       ========================= */

    public RoundResult endRound() {

        Pergunta current = getCurrentQuestion();
        if (current == null) return null;

        if (!isCurrentQuestionIndividual()) {
            // Pergunta de equipa → aguardar barreira e distribuir pontuação
            for (Team team : teamsMap.values()) {
                try { team.awaitAll(); } catch (InterruptedException ignored) {}
            }
        }

        HashMap<String, Integer> scores = getCurrentScores();

        responseCounter.set(0);
        currentQuestionIndex++;
        boolean gameEnded = currentQuestionIndex >= questions.length;

        return new RoundResult(true, gameEnded, scores, getCurrentQuestion());
    }

    /* =========================
       Envio de perguntas
       ========================= */

    public Serializable createSendQuestion(int timeoutSeconds) {
        Pergunta current = getCurrentQuestion();
        if (current == null) return null;

        if (isCurrentQuestionIndividual()) {
            return new SendIndividualQuestion(
                    current.getQuestion(),
                    current.getOptions(),
                    currentQuestionIndex,
                    timeoutSeconds
            );
        }

        return new SendTeamQuestion(
                current.getQuestion(),
                current.getOptions(),
                currentQuestionIndex,
                timeoutSeconds,
                playersPerTeam
        );
    }

    /* =========================
       Pontuações
       ========================= */

    private HashMap<String, Integer> getCurrentScores() {
        HashMap<String, Integer> map = new HashMap<>();
        for (Team t : teamsMap.values()) {
            for (Player p : t.getPlayers()) {
                map.put(p.getName(), p.getScore());
            }
        }
        return map;
    }

    public SendFinalScores getFinalScores() { return new SendFinalScores(getCurrentScores()); }
}
