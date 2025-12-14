package Messages;

import java.io.Serializable;

public class JoinGameResponse implements Serializable {
    private String nomeJogador;
    private int jogadorId;
    private int equipaId;

    public JoinGameResponse(int jogadorId, int equipaId, String nomeJogador) {
        this.jogadorId = jogadorId;
        this.equipaId = equipaId;
        this.nomeJogador = nomeJogador;
    }

    public String getNomeJogador() {
        return nomeJogador;
    }

    public int getJogadorId() {
        return jogadorId;
    }

    public int getEquipaId() {
        return equipaId;
    }

}
