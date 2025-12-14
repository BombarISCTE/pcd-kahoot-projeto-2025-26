package Messages;

import java.io.Serializable;

public class Answer implements Serializable {
    private final int jogadorId;
    private final int equipaId;
    private final int opcaoEscolhida;

    public Answer(int jogadorId, int equipaId, int opcaoEscolhida) {
        this.jogadorId = jogadorId;
        this.equipaId = equipaId;
        this.opcaoEscolhida = opcaoEscolhida;
    }

    public int getJogadorId() {
        return jogadorId;
    }

    public int getEquipaId() {
        return equipaId;
    }

    public int getOpcaoEscolhida() {
        return opcaoEscolhida;
    }

}
