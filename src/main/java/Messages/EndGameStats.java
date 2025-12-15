package Messages;

import java.io.Serializable;
import java.util.Map;

public class Statistic implements Serializable {
    private final Map<Integer, Integer> pontosJogadores;

    public Statistic(Map<Integer, Integer> pontosJogadores) {
        this.pontosJogadores = pontosJogadores;
    }

    public Map<Integer, Integer> getPontosJogadores() {
        return pontosJogadores;
    }

}
