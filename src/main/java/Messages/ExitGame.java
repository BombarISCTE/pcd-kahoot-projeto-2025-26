package Messages;

import java.io.Serializable;

public class ExitGame implements Serializable {
    private int jogadorId;

    public ExitGame(int jogadorId) {
        this.jogadorId = jogadorId;
    }

    public int getJogadorId() {
        return jogadorId;
    }
}
