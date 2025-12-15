package Messages;

import java.io.Serializable;

public class Answer implements Serializable {

    private final String username;
    private final int teamId;
    private final int opcaoEscolhida;

    public Answer(String username, int teamId, int opcaoEscolhida) {
        this.username = username;
        this.teamId = teamId;
        this.opcaoEscolhida = opcaoEscolhida;
    }

    public String getUsername() {
        return username;
    }

    public int getTeamId() {
        return teamId;
    }

    public int getOpcaoEscolhida() {
        return opcaoEscolhida;
    }
}
