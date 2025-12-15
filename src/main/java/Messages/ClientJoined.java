package Messages;

import java.io.Serializable;

public class ClientJoined implements Serializable {
    private final String username;
    private final int gameId;
    private final int teamId;

    public ClientJoined(String username, int gameId, int teamId) {
        this.username = username;
        this.gameId = gameId;
        this.teamId = teamId;
    }

    public String getUsername() { return username; }
    public int getGameId() { return gameId; }
    public int getTeamId() { return teamId; }
}
