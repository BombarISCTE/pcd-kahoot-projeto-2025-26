package Messages;

import java.io.Serializable;

public class Answer implements Serializable {
    private final String username;
    private final int equipaId;
    private final int opcaoEscolhida;

    public Answer(String username, int equipaId, int opcaoEscolhida) {
        this.username = username;
        this.equipaId = equipaId;
        this.opcaoEscolhida = opcaoEscolhida;
    }

    public String getUsername() {
        return username;
    }

    public int getEquipaId() {
        return equipaId;
    }

    public int getOpcaoEscolhida() {
        return opcaoEscolhida;
    }

}
