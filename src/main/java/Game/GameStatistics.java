package Game;

import java.util.HashMap;
import java.util.Map;

public class GameStatistics {
    private final Map<Integer, Integer> pontosJogadores = new HashMap<>();

    public void atualizaPontosJogadores(int jogadorId, int pontos) {
        pontosJogadores.merge(jogadorId, pontos, Integer::sum);
    }

    public Map<Integer, Integer> getPontosJogadores() {
        return pontosJogadores;
    }

    public synchronized Map<Integer, Integer> snapshot() {
        return new HashMap<>(pontosJogadores);
    }
}
