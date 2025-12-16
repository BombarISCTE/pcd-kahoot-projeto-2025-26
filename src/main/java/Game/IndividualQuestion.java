package Game;

import Utils.Constants;
import Utils.ModifiedCountdownLatch;

import java.util.ArrayList;
import java.util.HashMap;

public class IndividualQuestion extends Question {

    // Lista de jogadores que responderam a esta pergunta
    private final ArrayList<Player> answeredPlayers = new ArrayList<>();
    private final int totalPlayers;
    private ModifiedCountdownLatch countdownLatch;

    public IndividualQuestion(String questionText, int correct, int points, String[] options, int totalPlayers) {
        super(questionText, correct, points, options);
        this.totalPlayers = totalPlayers;
    }

    // Regista a resposta de um jogador e retorna a pontuação imediata (com bónus se for um dos 2 primeiros)
    public synchronized void registerAnswer(Player player, int chosenOption) {
        player.setChosenOption(chosenOption);
        answeredPlayers.add(player); //adiciona por ordem de resposta na lista
    }


        // No fim da ronda, somar a pontuação total da equipa a todos os jogadores da equipa
    public synchronized void processResponses(Team team) {
        ArrayList<Player> players = new ArrayList<>(team.getPlayers());
        int totalScore = 0;

        for (Player p : players) {
            int score = (p.getChosenOption() == correct) ? points : 0;
            int index = answeredPlayers.indexOf(p);
            if (index >= 0 && index < 2 && score > 0) {
                score *= Constants.BONUS_FACTOR;
            }
            totalScore += score;
        }
        for (Player p : players) {p.addScore(totalScore);}
    }




    public ArrayList<Player> getAnsweredPlayers() {
        return answeredPlayers;
    }

    public void setCountdownLatch(ModifiedCountdownLatch latch) {
        this.countdownLatch = latch;
    }
}
