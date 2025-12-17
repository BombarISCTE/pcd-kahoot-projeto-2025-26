package Game;

import Utils.Constants;
import Utils.ModifiedBarrier;
import Utils.ModifiedCountdownLatch;
import Utils.Records.*;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.Timer;
import java.util.TimerTask;

public class GameState {

    private final int gameCode;
    private final int numTeams;
    private final int playersPerTeam;
    private final HashMap<Integer, Team> teamsMap; // teamId -> Team

    private Question[] questions;
    private int currentQuestionIndex = 0;
    public int getCurrentQuestionIndex() { return currentQuestionIndex; }

    private AtomicInteger responseCounter = new AtomicInteger(0);
    private ModifiedCountdownLatch countdownLatch;
    private ModifiedBarrier teamBarrier;
    private Timer questionTimer;

    public GameState(int numTeams, int playersPerTeam, int gameCode) {
        this.numTeams = numTeams;
        this.playersPerTeam = playersPerTeam;
        this.gameCode = gameCode;
        this.teamsMap = new HashMap<>();
    }

    // --- Teams ---
    public void addTeam(int teamId) {
        if (teamsMap.containsKey(teamId)) throw new IllegalArgumentException("Team exists: " + teamId);
        teamsMap.put(teamId, new Team(teamId, playersPerTeam));
    }

    public Team getTeam(int teamId) { return teamsMap.get(teamId); }
    public ArrayList<Team> getTeams() { return new ArrayList<>(teamsMap.values()); }
    public int getGameCode() { return gameCode; }

    // --- Questions ---
    public void setQuestions(Question[] questions) { this.questions = questions; }

    public Question getCurrentQuestion() {
        if (questions == null || currentQuestionIndex >= questions.length) return null;
        return questions[currentQuestionIndex];
    }

    public boolean isCurrentQuestionIndividual() {
        Question current = getCurrentQuestion();
        return current instanceof IndividualQuestion;
    }

    // --- Start Question ---
    public void startCurrentQuestion() {
        Question current = getCurrentQuestion();
        if (current == null) return;

        int totalPlayers = totalPlayersInGame();

        if (current instanceof IndividualQuestion iq) {
            countdownLatch = new ModifiedCountdownLatch(
                    Constants.BONUS_FACTOR,
                    2,
                    Constants.QUESTION_TIME_LIMIT,
                    totalPlayers
            );
            iq.setCountdownLatch(countdownLatch);
        } else if (current instanceof TeamQuestion tq) {
            teamBarrier = new ModifiedBarrier(totalPlayers, () -> assignTeamQuestionScores((TeamQuestion) current));
            tq.setBarrier(teamBarrier);
        }

        // Timer para fim de pergunta
        questionTimer = new Timer();
        questionTimer.schedule(new TimerTask() {
            @Override
            public void run() {
                if (current instanceof IndividualQuestion && countdownLatch != null) countdownLatch.tempoExpirado();
                else if (current instanceof TeamQuestion && teamBarrier != null) teamBarrier.tempoExpirado();
            }
        }, Constants.QUESTION_TIME_LIMIT * 1000);
    }

    // --- Register Answer ---
    public void registerAnswer(String username, int option) {
        Question current = getCurrentQuestion();
        if (current == null) return;

        for (Team team : teamsMap.values()) {
            for (Player p : team.getPlayers()) {
                if (p.getName().equals(username)) {
                    p.setChosenOption(option);

                    if (current instanceof IndividualQuestion iq) {
                        int order = responseCounter.incrementAndGet();
                        p.setResponseOrder(order);
                        int base = (option == iq.getCorrect()) ? iq.getPoints() : 0;
                        if (order <= 2 && base > 0) base *= Constants.BONUS_FACTOR;
                        p.addScore(base);
                        if (countdownLatch != null) countdownLatch.countDown();

                    } else if (current instanceof TeamQuestion) {
                        if (teamBarrier != null) teamBarrier.chegouJogador();
                    }
                    return;
                }
            }
        }
    }

    // --- End Round ---
    public RoundResult endRound() {
        Question current = getCurrentQuestion();
        if (current == null) return null;

        if (current instanceof TeamQuestion && teamBarrier != null) {
            try { teamBarrier.await(); } catch (InterruptedException ignored) {}
        }

        if (current instanceof IndividualQuestion && countdownLatch != null) {
            assignIndividualScores((IndividualQuestion) current);
        }

        HashMap<String, Integer> scores = getCurrentScores();
        responseCounter.set(0);
        currentQuestionIndex++;
        if (questionTimer != null) questionTimer.cancel();

        boolean gameEnded = currentQuestionIndex >= questions.length;
        return new RoundResult(true, gameEnded, scores, getCurrentQuestion());
    }

    // --- Individual Scores ---
    private void assignIndividualScores(IndividualQuestion question) {
        for (Team team : teamsMap.values()) {
            int total = 0;
            for (Player p : team.getPlayers()) total += p.getScore();
            for (Player p : team.getPlayers()) p.addScore(total);
        }
    }

    // --- Team Scores ---
    private void assignTeamQuestionScores(TeamQuestion question) {
        for (Team team : teamsMap.values()) {
            ArrayList<Player> players = team.getPlayers();
            boolean allCorrect = true;
            boolean atLeastOneCorrect = false;
            for (Player p : players) {
                if (p.getChosenOption() != question.getCorrect()) allCorrect = false;
                else atLeastOneCorrect = true;
            }
            int score = 0;
            if (atLeastOneCorrect) score = allCorrect ? question.getPoints() * 2 : question.getPoints();
            for (Player p : players) p.addScore(score);
        }
    }

    private int totalPlayersInGame() {
        int total = 0;
        for (Team t : teamsMap.values()) total += t.getCurrentSize();
        return total;
    }

    // --- Scores ---
    public HashMap<String, Integer> getCurrentScores() {
        HashMap<String, Integer> map = new HashMap<>();
        for (Team t : teamsMap.values()) {
            for (Player p : t.getPlayers()) map.put(p.getName(), p.getScore());
        }
        return map;
    }

    public SendIndividualQuestion createSendIndividualQuestion() {
        Question current = getCurrentQuestion();
        if (current == null || !(current instanceof IndividualQuestion)) return null;
        IndividualQuestion iq = (IndividualQuestion) current;
        return new SendIndividualQuestion(
                iq.getQuestionText(),
                iq.getOptions(),
                currentQuestionIndex,
                Constants.QUESTION_TIME_LIMIT
        );
    }

    public SendTeamQuestion createSendTeamQuestion() {
        Question current = getCurrentQuestion();
        if (current == null || !(current instanceof TeamQuestion)) return null;
        TeamQuestion tq = (TeamQuestion) current;
        return new SendTeamQuestion(
                tq.getQuestionText(),
                tq.getOptions(),
                currentQuestionIndex,
                Constants.QUESTION_TIME_LIMIT
        );
    }

    public SendFinalScores getFinalScores() {
        return new SendFinalScores(getCurrentScores());
    }

    public boolean isCurrentQuestionComplete() {
        Question current = getCurrentQuestion();
        if (current instanceof IndividualQuestion) return countdownLatch != null && countdownLatch.getCount() <= 0;
        if (current instanceof TeamQuestion) return teamBarrier != null && teamBarrier.isComplete();
        return true;
    }
}
