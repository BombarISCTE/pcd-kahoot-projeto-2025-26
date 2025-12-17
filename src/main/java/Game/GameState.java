package Game;

import Utils.Constants;
import Utils.ModifiedBarrier;
import Utils.ModifiedCountdownLatch;
import Utils.Records.*;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.atomic.AtomicInteger;

public class GameState {

    private final int gameCode;
    private final int numTeams;
    private final int playersPerTeam;
    private final HashMap<Integer, Team> teamsMap;

    private Question[] questions;
    private int currentQuestionIndex = 0;
    private boolean isActive = false;

    private final AtomicInteger responseCounter = new AtomicInteger(0);
    private ModifiedCountdownLatch countdownLatch;
    private Timer questionTimer;

    public GameState(int numTeams, int playersPerTeam, int gameCode) {
        this.numTeams = numTeams;
        this.playersPerTeam = playersPerTeam;
        this.gameCode = gameCode;
        this.teamsMap = new HashMap<>();
    }

    /* =========================
       TEAMS
       ========================= */

    public synchronized void addTeam(int teamId) {
        if (teamsMap.containsKey(teamId)) {
            System.out.println("[GameState] Team " + teamId + " already exists.");
            return;
        }
        teamsMap.put(teamId, new Team(teamId, playersPerTeam));
        System.out.println("[GameState] Team " + teamId + " added.");
    }

    public synchronized Team getTeam(int teamId) {return teamsMap.get(teamId);}

    public synchronized ArrayList<Team> getTeams() {return new ArrayList<>(teamsMap.values());}

    public synchronized int getGameCode() {return gameCode;}

    public synchronized ArrayList<Player> getAllPlayers() {
        ArrayList<Player> players = new ArrayList<>();
        for (Team team : teamsMap.values()) {
            players.addAll(team.getPlayers());
        }
        return players;
    }

    /* =========================
       QUESTIONS
       ========================= */

    public synchronized void setQuestions(Question[] questions) {this.questions  =   questions;}

    public synchronized Question[] getQuestions() {return questions;}


    public synchronized void startGame() {

        int totalPlayers = totalPlayersInGame();
        if (totalPlayers <= 0)
            throw new IllegalStateException("Cannot start game with 0 players");

        for (Question q : questions) {

            if (q instanceof TeamQuestion tq) {
                for (Team team : teamsMap.values()) {
                    tq.addTeam(team);
                }
                tq.initializeBarrier();
            }

            if (q instanceof IndividualQuestion iq) {
                iq.setTotalPlayers(totalPlayers);
                iq.initializeLatch();
            }
        }
    }


    public synchronized Question getCurrentQuestion() {
        if (questions == null || currentQuestionIndex >= questions.length) return null;
        return questions[currentQuestionIndex];
    }

    public synchronized int getCurrentQuestionIndex() {return currentQuestionIndex;}

    /* =========================
       START QUESTION
       ========================= */

    public synchronized void startCurrentQuestion() {
        if (!isActive) throw new IllegalStateException("GameState startCurrentQuestion - Cannot start question in inactive game.");

        Question current = getCurrentQuestion();
        if (current == null) return;

        if (current instanceof IndividualQuestion iq) {
            countdownLatch = iq.getCountdownLatch();
        }

        if (current instanceof TeamQuestion tq) {
            // barrier já está inicializada no startGame()
        }

        // Timer
        questionTimer = new Timer();
        questionTimer.schedule(new TimerTask() {
            @Override
            public void run() {
                if (current instanceof IndividualQuestion && countdownLatch != null) {
                    countdownLatch.tempoExpirado();
                } else if (current instanceof TeamQuestion tq) {
                    tq.getBarrier().tempoExpirado();
                }
            }
        }, Constants.QUESTION_TIME_LIMIT * 1000);
    }


    /* =========================
       REGISTER ANSWER
       ========================= */

    public synchronized void registerAnswer(String username, int option) {
        if(!isActive) throw new IllegalStateException("GameState registerAnswer - Cannot register answer in inactive game.");

        Question current = getCurrentQuestion();
        if (current == null) return;

        for (Team team : teamsMap.values()) {
            for (Player p : team.getPlayers()) {
                if (!p.getName().equals(username)) continue;

                p.setChosenOption(option);

                if (current instanceof IndividualQuestion iq) {
                    int order = responseCounter.incrementAndGet();
                    p.setResponseOrder(order);

                    int base = (option == iq.getCorrect()) ? iq.getPoints() : 0;
                    if (order <= 2 && base > 0) base *= Constants.BONUS_FACTOR;

                    p.addScore(base);
                    countdownLatch.countDown();
                }

                if (current instanceof TeamQuestion tq) {
                    tq.playerAnswered(p, option == tq.getCorrect());
                }
                return;
            }
        }
    }

    /* =========================
       END ROUND
       ========================= */

    public synchronized RoundResult endRound() {
        if(!isActive) throw new IllegalStateException("GameState endRound - Cannot end round in inactive game.");

        Question current = getCurrentQuestion();
        if (current == null) return null;

        if (current instanceof TeamQuestion tq) {
            try {
                tq.getBarrier().await();
            } catch (InterruptedException ignored) {}
        }

        responseCounter.set(0);
        currentQuestionIndex++;

        if (questionTimer != null) questionTimer.cancel();

        boolean gameEnded = currentQuestionIndex >= questions.length;
        return new RoundResult(true, gameEnded, getCurrentScores(), getCurrentQuestion());
    }

    /* =========================
       AUX
       ========================= */

    private synchronized int totalPlayersInGame() {
        int total = 0;
        for (Team t : teamsMap.values()) total += t.getCurrentSize();
        return total;
    }

    public synchronized HashMap<String, Integer> getCurrentScores() {
        HashMap<String, Integer> map = new HashMap<>();
        for (Team t : teamsMap.values()) {
            for (Player p : t.getPlayers()) {
                map.put(p.getName(), p.getScore());
            }
        }
        return map;
    }

    public synchronized boolean isCurrentQuestionComplete() {
        Question current = getCurrentQuestion();
        if (current == null) return true;

        if (current instanceof IndividualQuestion iq) {
            ModifiedCountdownLatch latch = iq.getCountdownLatch();
            return latch != null && latch.getCount() <= 0;
        }

        if (current instanceof TeamQuestion tq) {
            ModifiedBarrier barrier = tq.getBarrier();
            return barrier != null && barrier.isComplete();
        }

        return true;
    }

    public synchronized boolean isActive() {return isActive;}
    public synchronized void setActive(boolean active) {isActive = active;}


    /* =========================
       SEND OBJECTS
       ========================= */

    public synchronized SendIndividualQuestion createSendIndividualQuestion() {
        if (!(getCurrentQuestion() instanceof IndividualQuestion iq)) return null;
        return new SendIndividualQuestion(
                iq.getQuestionText(),
                iq.getOptions(),
                currentQuestionIndex,
                Constants.QUESTION_TIME_LIMIT
        );
    }

    public synchronized SendTeamQuestion createSendTeamQuestion() {
        if (!(getCurrentQuestion() instanceof TeamQuestion tq)) return null;
        return new SendTeamQuestion(
                tq.getQuestionText(),
                tq.getOptions(),
                currentQuestionIndex,
                Constants.QUESTION_TIME_LIMIT
        );
    }

    public synchronized SendFinalScores getFinalScores() {
        return new SendFinalScores(getCurrentScores());
    }
}
