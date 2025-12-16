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
    private final HashMap<Integer, Team> teamsMap;

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

    public void addTeam(int teamId, String teamName) {
        if (teamsMap.containsKey(teamId)) throw new IllegalArgumentException("Team exists: " + teamId);
        teamsMap.put(teamId, new Team(teamName, teamId, playersPerTeam));
    }

    public Team getTeam(int teamId) { return teamsMap.get(teamId); }
    public ArrayList<Team> getTeams() { return new ArrayList<>(teamsMap.values()); }
    public int getGameCode() { return gameCode; }

    public void setQuestions(Question[] questions) { this.questions = questions; }

    public Question getCurrentQuestion() {
        if (questions == null || currentQuestionIndex >= questions.length) return null;
        return questions[currentQuestionIndex];
    }

    public boolean isCurrentQuestionIndividual() { return currentQuestionIndex % 2 == 0; }

    // --- Inicializar temporizador e latch/barrier para a pergunta atual ---
    public void startCurrentQuestion() {
        Question current = getCurrentQuestion();
        if (current == null) return;

        int totalPlayers = totalPlayersInGame();

        if (isCurrentQuestionIndividual() && current instanceof IndividualQuestion) {
            countdownLatch = new ModifiedCountdownLatch(
                    2, // bonusFactor
                    2, // bonusCount
                    Constants.QUESTION_TIME_LIMIT,
                    totalPlayers
            );
        } else if (!isCurrentQuestionIndividual() && current instanceof TeamQuestion) {
            teamBarrier = new ModifiedBarrier(
                    totalPlayers,
                    () -> assignTeamQuestionScores((TeamQuestion) current)
            );
        }

        // Start timer
        questionTimer = new Timer();
        questionTimer.schedule(new TimerTask() {
            @Override
            public void run() {
                if (isCurrentQuestionIndividual() && countdownLatch != null) {
                    countdownLatch.tempoExpirado();
                } else if (!isCurrentQuestionIndividual() && teamBarrier != null) {
                    teamBarrier.tempoExpirado();
                }
            }
        }, Constants.QUESTION_TIME_LIMIT * 1000L);
    }

    // --- Registrar resposta de um jogador ---
    public void registerAnswer(String username, int option) {
        Question current = getCurrentQuestion();
        if (current == null) return;

        for (Team team : teamsMap.values()) {
            for (Player p : team.getPlayers()) {
                if (p.getName().equals(username)) {
                    p.setChosenOption(option);

                    if (isCurrentQuestionIndividual() && current instanceof IndividualQuestion) {
                        int order = responseCounter.incrementAndGet();
                        p.setResponseOrder(order);

                        int base = (option == current.getCorrect()) ? current.getPoints() : 0;
                        if (order <= 2 && base > 0) base *= 2; // bónus 2 primeiros
                        p.addScore(base);

                        if (countdownLatch != null) countdownLatch.countDown();

                    } else if (!isCurrentQuestionIndividual() && current instanceof TeamQuestion) {
                        teamBarrier.chegouJogador();
                    }
                    return;
                }
            }
        }
    }

    // --- Fim de ronda ---
    public RoundResult endRound() {
        Question current = getCurrentQuestion();
        if (current == null) return null;

        if (!isCurrentQuestionIndividual() && teamBarrier != null) {
            try {
                teamBarrier.await();
            } catch (InterruptedException ignored) {}
        }

        if (isCurrentQuestionIndividual() && countdownLatch != null) {
            assignIndividualScores((IndividualQuestion) current);
        }

        HashMap<String, Integer> scores = getCurrentScores();
        responseCounter.set(0);
        currentQuestionIndex++;
        if (questionTimer != null) questionTimer.cancel();

        boolean gameEnded = currentQuestionIndex >= questions.length;

        return new RoundResult(true, gameEnded, scores, getCurrentQuestion());
    }

    // --- Atribuir pontuação da pergunta individual por equipa ---
    private void assignIndividualScores(IndividualQuestion question) {
        for (Team team : teamsMap.values()) {
            int total = 0;
            for (Player p : team.getPlayers()) {
                total += p.getScore();
            }
            for (Player p : team.getPlayers()) {
                p.addScore(total); // todos recebem pontuação da equipa
            }
        }
    }

    // --- Atribuir pontuação da pergunta de equipa ---
    private void assignTeamQuestionScores(TeamQuestion question) {
        for (Team team : teamsMap.values()) {
            ArrayList<Player> players = team.getPlayers();
            boolean todosAcertaram = true;
            boolean peloMenosUmAcertou = false;

            for (Player p : players) {
                if (p.getChosenOption() != question.getCorrect()) todosAcertaram = false;
                else peloMenosUmAcertou = true;
            }

            int score = 0;
            if (peloMenosUmAcertou) score = todosAcertaram ? question.getPoints() * 2 : question.getPoints();

            for (Player p : players) p.addScore(score);
        }
    }

    private int totalPlayersInGame() {
        int total = 0;
        for (Team t : teamsMap.values()) total += t.getCurrentSize();
        return total;
    }

    private HashMap<String, Integer> getCurrentScores() {
        HashMap<String, Integer> map = new HashMap<>();
        for (Team t : teamsMap.values()) {
            for (Player p : t.getPlayers()) map.put(p.getName(), p.getScore());
        }
        return map;
    }

    public SendIndividualQuestion createSendIndividualQuestion() {
        Question current = getCurrentQuestion();
        if (current == null || !(current instanceof IndividualQuestion)) return null;
        return new SendIndividualQuestion(
                current.getQuestionText(),
                current.getOptions(),
                currentQuestionIndex,
                Constants.QUESTION_TIME_LIMIT
        );
    }

    public SendTeamQuestion createSendTeamQuestion() {
        Question current = getCurrentQuestion();
        if (current == null || !(current instanceof TeamQuestion)) return null;
        return new SendTeamQuestion(
                current.getQuestionText(),
                current.getOptions(),
                currentQuestionIndex,
                Constants.QUESTION_TIME_LIMIT
        );
    }

    public SendFinalScores getFinalScores() { return new SendFinalScores(getCurrentScores()); }
}
