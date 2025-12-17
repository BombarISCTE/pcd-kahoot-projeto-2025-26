package Messages;

import java.io.Serializable;

//add
public class JoinRejected implements Serializable {
    private final String motivo;

    public JoinRejected(String motivo) {
        this.motivo = motivo;
    }

    public  String getMotivo() {
        return motivo;
    }
}
