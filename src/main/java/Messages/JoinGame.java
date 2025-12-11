package Messages;

import java.io.Serializable;

public class JoinGame implements Serializable {
    private String nomeJogador;
    private int equipaId;

    public JoinGame(String nomeJogador, int equipaId) {
        this.nomeJogador = nomeJogador;
        this.equipaId = equipaId;
    }

    public String getNomeJogador() {
        return nomeJogador;
    }

    public int getEquipaId() {
        return equipaId;
    }

}
