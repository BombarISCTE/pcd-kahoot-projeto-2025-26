package Game;

public class GameState {

    private final String gameCode;
    private int numEquipas;
    private int numJogadoresEquipa;

    public GameState(int numEquipas, int numJogadoresEquipa) {
        this.numEquipas = numEquipas;
        this.numJogadoresEquipa = numJogadoresEquipa;
        gameCode = GeradorCodigo.gerarCodigo();
    }

    public int getNumEquipas() {
        return numEquipas;
    }

    public int getNumJogadoresEquipa() {
        return numJogadoresEquipa;
    }



}
