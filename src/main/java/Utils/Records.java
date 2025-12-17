package Utils;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;

public class Records {


    public record ClientConnect(String username, int gameId, int teamId) implements Serializable {
    }

    public record SendAnswer(String username, int selectedOption, int questionNumber) implements Serializable {
    }

    // PlayerInfo record to transfer minimal player state without serializing Game.Player
    public record PlayerInfo(String name, int teamId, int score) implements Serializable {
    }

    public record ClientConnectAck(String username, int gameId,
                                   ArrayList<PlayerInfo> connectedPlayers) implements Serializable {
    }

    // Notify other clients in the lobby that a new player joined
    public record NewPlayerConnected(PlayerInfo player) implements Serializable {
    }


    public record GameStarted(int gameId) implements Serializable {
    }

    public record GameEnded(int gameId) implements Serializable {
    }

    public record GameStartedWithPlayers(int gameId, ArrayList<PlayerInfo> connectedPlayers) implements Serializable {
    }


    public record RoundResult(boolean roundEnded, boolean gameEnded,
                              HashMap<String, Integer> playerScores) implements Serializable {
    }


    public record SendQuestion(String question, String[] options, int questionNumber,
                               int timeLimit) implements Serializable {
    }

    public record SendTeamQuestion(String question, String[] options, int questionNumber,
                                   int timeLimit) implements Serializable {
    }


    public record SendIndividualQuestion(String question, String[] options, int questionNumber,
                                         int timeLimit) implements Serializable {
    }

    /**
     * @param playerScores username -> pontuação
     */
    public record SendRoundStats(int gameId, HashMap<String, Integer> playerScores) implements Serializable {
    }

    public record ErrorMessage(String message) implements Serializable {
    }

    public record FatalErrorMessage(String message) implements Serializable {
    }


    public record SendFinalScores(
            HashMap<String, Integer> finalScores) implements Serializable { // username -> pontuação
    }

    public record refuseConnection(String reason) implements Serializable {
    }

    // Sent to a client to inform it of the server-assigned player name/ID
    public record AssignedName(String assignedName) implements Serializable {
    }
}

