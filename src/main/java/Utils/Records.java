package Utils;

import Game.Pergunta;

import java.io.Serializable;
import java.util.List;
import java.util.Map;


public class Records {


    public record ClientConnect(String username, int gameId, int teamId) implements Serializable {
    }

    public record SendAnswer( String username,int selectedOption, int questionNumber) implements Serializable {
    }

    /**
     * @param connectedPlayers optional list of already connected players
     */
    public record ClientConnectAck(String username, int gameId, List<String> connectedPlayers) implements Serializable {
    }

    public class GameStarted implements Serializable {
        private final int gameId;
        public GameStarted(int gameId) {this.gameId = gameId;}

        public int getGameId() { return gameId; }
    }

    public record RoundResult(boolean roundEnded, boolean gameEnded, Map<String, Integer> playerScores, Pergunta nextQuestion) implements Serializable { }


    public record SendQuestion(String question, String[] options, int questionNumber, int timeLimit) implements Serializable { }

    /**
     * @param playerScores username -> pontuação
     */
    public record SendRoundStats(int gameId, Map<String, Integer> playerScores) implements Serializable {
    }

    public static class GameEnded implements Serializable {
        private final int gameId;

        public GameEnded(int gameId) { this.gameId = gameId; }

        public int getGameId() { return gameId; }
    }

    public record SendFinalScores(Map<String, Integer> finalScores) implements Serializable { // username -> pontuação
    }

    public record refuseConnection(String reason) implements Serializable {
    }

}
