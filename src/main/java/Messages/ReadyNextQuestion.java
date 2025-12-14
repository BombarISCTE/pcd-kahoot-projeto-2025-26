package Messages;

import java.io.Serializable;

public class ReadyNextQuestion implements Serializable {
    int jogadorId;

    public ReadyNextQuestion(int jogadorId) {
        this.jogadorId = jogadorId;
    }

    public int getJogadorId() {
        return jogadorId;
    }
}
