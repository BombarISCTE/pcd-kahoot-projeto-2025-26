package Messages;

import java.io.Serializable;

public class JoinGame implements Serializable {
    private String codigoJogo;
    private String nomeJogador;
    private int equipaId;

    public JoinGame(String nomeJogador, int equipaId, String codigoJogo) {
        this.nomeJogador = nomeJogador;
        this.equipaId = equipaId;
        this.codigoJogo = codigoJogo;
    }

    public String getNomeJogador() {
        return nomeJogador;
    }

    public int getEquipaId() {
        return equipaId;
    }

    public String getCodigoJogo() {
        return codigoJogo;
    }

}
