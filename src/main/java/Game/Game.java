package Game;


import com.google.gson.Gson;

import java.io.FileReader;
import java.io.IOException;

public class Game {
    private GameState gameState;
    private GameStatistics statistics;

    public Game (int numEquipas, int numJogadoresEquipa, int numPerguntas) {
        this.gameState = new GameState(numEquipas, numJogadoresEquipa, numPerguntas);
        this.statistics = new GameStatistics();
    }

    public GameState getGameState() {
        return gameState;
    }

    public GameStatistics getStatistics() {
        return statistics;
    }

    public void CarregarPerguntas(String caminho) throws IOException {
        Pergunta[] perguntas = Pergunta.lerPerguntas(caminho);
        this.gameState.setPerguntas(perguntas);
    }




}
