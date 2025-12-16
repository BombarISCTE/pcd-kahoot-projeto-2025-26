package Utils;

import Game.Pergunta;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;


public class Records {


    public record ClientConnect(String username, int gameId, int teamId) implements Serializable {
    }

    public record SendAnswer( String username,int selectedOption, int questionNumber) implements Serializable {
    }

    /**
     * @param connectedPlayers optional list of already connected players
     */
    public record ClientConnectAck(String username, int gameId, ArrayList<String> connectedPlayers) implements Serializable {
    }

    public class GameStarted implements Serializable {
        private final int gameId;
        public GameStarted(int gameId) {this.gameId = gameId;}

        public int getGameId() { return gameId; }
    }

    public record RoundResult(boolean roundEnded, boolean gameEnded, HashMap<String, Integer> playerScores,
                              Pergunta nextQuestion) implements Serializable { }


    public record SendQuestion(String question, String[] options, int questionNumber, int timeLimit) implements Serializable { }

    public record SendTeamQuestion(String question, String[] options, int questionNumber, int timeLimit) implements Serializable {}


    public record SendIndividualQuestion(String question, String[] options, int questionNumber, int timeLimit) implements Serializable {}

    /**
     * @param playerScores username -> pontuação
     */
    public record SendRoundStats(int gameId, HashMap<String, Integer> playerScores) implements Serializable {
    }

    public record ErrorMessage(String message) implements Serializable { }

    public record FatalErrorMessage(String message) implements Serializable { }

    public static class GameEnded implements Serializable {
        private final int gameId;

        public GameEnded(int gameId) { this.gameId = gameId; }

        public int getGameId() { return gameId; }
    }

    public record SendFinalScores(HashMap<String, Integer> finalScores) implements Serializable { // username -> pontuação
    }

    public record refuseConnection(String reason) implements Serializable {
    }

}
