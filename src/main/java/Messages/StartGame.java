package Messages;

import java.io.Serializable;

public class StartGame implements Serializable {
    private String codigoJogo;

    public StartGame(String codigoJogo) {
        this.codigoJogo = codigoJogo;
    }

    public String getCodigoJogo() {
        return codigoJogo;
    }
}
