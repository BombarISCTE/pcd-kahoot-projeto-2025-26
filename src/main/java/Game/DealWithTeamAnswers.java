package Game;

import Utils.Constants;

//public class DealWithTeamAnswers {
//
//    private final GameState gameState;
//    private final int equipaId;
//    private Thread timerThread;
//
//    public DealWithTeamAnswers(GameState gameState, int equipaId) {
//        this.gameState = gameState;
//        this.equipaId = equipaId;
//    }
//
//    public void iniciarPerguntaEquipa(){
//        Team team = gameState.getTeam(equipaId);
//        if (team == null) return;
//
//        // start the team's barrier with an empty action (scoring is computed later by GameState.endRound)
//        team.startNewQuestion(() -> {});
//
//        int tempo = Constants.TIMOUT_SECS;
//
//        // start a simple timeout thread that calls team.timeout() after the timeout
//        timerThread = new Thread(() -> {
//            try {
//                Thread.sleep((long) tempo * 1000);
//                team.timeout();
//            } catch (InterruptedException ignored) {
//                Thread.currentThread().interrupt();
//            }
//        }, "TeamTimer-" + equipaId);
//
//        timerThread.start();
//    }
//
//    /**
//     * Register an answer from a player (by username). GameState.registerAnswer will locate the player
//     * and notify the team's barrier (team.playerAnswered()).
//     */
//    public void registarRespostaEquipa(String username, int opcaoEscolhida) {
//        gameState.registerAnswer(username, opcaoEscolhida);
//    }
//
//    public void esperarAteFimDaRonda(){
//        Team team = gameState.getTeam(equipaId);
//        if (team == null) return;
//        try{
//            team.awaitAll();
//        }catch (InterruptedException e){
//            Thread.currentThread().interrupt();
//        }
//    }
//
//    public void cancelTimer() {
//        if (timerThread != null && timerThread.isAlive()) {
//            timerThread.interrupt();
//        }
//    }
//
//}
