package Game;


public class Game {
    private GameState gameState;
    private GameStatistics statistics;

    public Game (int numEquipas, int numJogadoresEquipa, int numPerguntas) {
        this.gameState = new GameState(numEquipas, numJogadoresEquipa);
        this.statistics = new GameStatistics();
    }



}
