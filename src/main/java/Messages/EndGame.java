package Messages;

import java.io.Serializable;

public class EndGame implements Serializable {
    private final String mensagem;

    public EndGame(String mensagem) {
        this.mensagem = mensagem;
    }

    public String getMensagem() {
        return mensagem;
    }
}
