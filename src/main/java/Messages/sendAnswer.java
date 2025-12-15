package Messages;

import java.io.Serializable;

public class sendAnswer implements Serializable {
    private final String username;
    private final int equipaId;
    private final int opcaoEscolhida;

    public sendAnswer(String username, int equipaId, int opcaoEscolhida) {
        this.username = username;
        this.equipaId = equipaId;
        this.opcaoEscolhida = opcaoEscolhida;
    }

    public sendAnswer(String username, int opcaoEscolhida) { // Para jogadores individuais
        this.username = username;
        this.equipaId = -1;
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
