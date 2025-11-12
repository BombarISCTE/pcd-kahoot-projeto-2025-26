package Game;


import com.google.gson.Gson;

import java.io.FileReader;
import java.io.IOException;

public class Game {
    private GameState gameState;
    private GameStatistics statistics;

    public Game (int numEquipas, int numJogadoresEquipa, int numPerguntas) {
        this.gameState = new GameState(numEquipas, numJogadoresEquipa);
        this.statistics = new GameStatistics();
    }




}
