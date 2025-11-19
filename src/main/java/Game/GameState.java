package Game;

import Utils.IdCodeGenerator;

public class GameState {

    private final String gameCode;
    private int numEquipas;
    private int numJogadoresEquipa;

    public GameState(int numEquipas, int numJogadoresEquipa) {
        this.numEquipas = numEquipas;
        this.numJogadoresEquipa = numJogadoresEquipa;
        gameCode = IdCodeGenerator.gerarCodigo();
    }

    public int getNumEquipas() {
        return numEquipas;
    }

    public int getNumJogadoresEquipa() {
        return numJogadoresEquipa;
    }



}
