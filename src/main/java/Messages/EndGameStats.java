package Messages;

import java.io.Serializable;
import java.util.Map;

public class EndGameStats implements Serializable {
    private Map<Integer, Integer> pontosJogadores;

    public EndGameStats(Map<Integer, Integer> pontosJogadores) {
        this.pontosJogadores = pontosJogadores;
    }

    public Map<Integer, Integer> getPontosJogadores() {
        return pontosJogadores;
    }

}
