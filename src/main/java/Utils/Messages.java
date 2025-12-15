package Utils;

import java.io.Serializable;
import java.util.List;
import java.util.Map;


public class Messages {


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


//    public record SendQuestion(int questionNumber, String question, List<String> options,
//                               int timeLimit) implements Serializable {
//    }

    public class SendQuestion implements Serializable {

        private final String question;
        private final String[] options;
        private final int questionNumber;
        private final int timeLimit;

        public SendQuestion(String question, String[] options, int questionNumber, int timeLimit) {
            this.question = question;
            this.options = options;
            this.questionNumber = questionNumber;
            this.timeLimit = timeLimit;
        }

        public String getQuestion() {return question;}

        public String[] getOptions() {return options;}

        public int getQuestionNumber() {return questionNumber;}

        public int getTimeLimit() {return timeLimit;}


    }

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
