//package Utils;
//
//import java.io.Serializable;
//import java.util.List;
//import java.util.Map;
//
//
//public class Messages {
//
//
//    public static class ClientConnect implements Serializable {
//        private final String username;
//        private final int gameId;
//        private final int teamId;
//
//        public ClientConnect(String username, int gameId, int teamId) {
//            this.username = username;
//            this.gameId = gameId;
//            this.teamId = teamId;
//        }
//
//        public String getUsername() { return username; }
//        public int getGameId() { return gameId; }
//        public int getTeamId() { return teamId; }
//    }
//
//    public record SendAnswer( String username,int selectedOption, int questionNumber) implements Serializable {
//    }
//
//    /**
//     * @param connectedPlayers optional list of already connected players
//     */
//    public record ClientConnectAck(String username, int gameId, List<String> connectedPlayers) implements Serializable {
//    }
//
//    public static class GameStarted implements Serializable {
//        private final int gameId;
//        public GameStarted(int gameId) {this.gameId = gameId;}
//
//        public int getGameId() { return gameId; }
//    }
//
////    /**
////     * @param timeLimit segundos
////     */
////    public record SendQuestion(int questionNumber, String question, List<String> options,
////                               int timeLimit) implements Serializable {
////    }
//
//    public class SendQuestion implements Serializable {
//
//        private final String question;
//        private final String[] options;
//        private final int questionNumber;
//        private final int timeLimit = Constants.TEMPO_LIMITE_QUESTAO;
//
//        public SendQuestion(String question, String[] options, int questionNumber) {
//            this.question = question;
//            this.options = options;
//            this.questionNumber = questionNumber;
//        }
//
//        public String getQuestion() {return question;}
//
//        public String[] getOptions() {return options;}
//
//        public int getQuestionNumber() {return questionNumber;}
//
//        public int getTimeLimit() {return timeLimit;}
//
//
//
//    }
//
//    public static class SendNextQuestion implements Serializable {
//        private final int questionNumber;
//        private final SendQuestion question;
//
//        public SendNextQuestion(int questionNumber, SendQuestion question) {
//            this.questionNumber = questionNumber;
//            this.question = question;
//        }
//
//        public int getQuestionNumber() { return questionNumber; }
//        public SendQuestion getQuestion() { return question; }
//    }
//
//    /**
//     * @param playerScores username -> pontuação
//     */
//    public record SendRoundStats(int gameId, Map<String, Integer> playerScores) implements Serializable {
//    }
//
//    public static class GameEnded implements Serializable {
//        private final int gameId;
//
//        public GameEnded(int gameId) { this.gameId = gameId; }
//
//        public int getGameId() { return gameId; }
//    }
//
//    public record SendFinalScores(Map<String, Integer> finalScores) implements Serializable {
//    }
//}
