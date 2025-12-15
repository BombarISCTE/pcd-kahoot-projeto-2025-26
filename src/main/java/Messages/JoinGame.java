package Messages;

import java.io.Serializable;

public class JoinGame implements Serializable {
    private int gameId;
    private String nomeJogador;
    private int equipaId;

    public JoinGame(String nomeJogador, int equipaId, int gameId) {
        this.nomeJogador = nomeJogador;
        this.equipaId = equipaId;
        this.gameId = gameId;
    }


    public String getNomeJogador() {
        return nomeJogador;
    }

    public int getEquipaId() {
        return equipaId;
    }

    public int getGameId() {
        return gameId;
    }

}
