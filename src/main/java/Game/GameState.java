package Game;

import Server.GeradorCodigo;

public class GameState {

    private String codigo;
    private int numEquipas;
    private int numJogadoresEquipa;

    public GameState(int numEquipas, int numJogadoresEquipa) {
        this.numEquipas = numEquipas;
        this.numJogadoresEquipa = numJogadoresEquipa;
        this.codigo = GeradorCodigo.gerarCodigo();
    }

    public int getNumEquipas() {
        return numEquipas;
    }

    public int getNumJogadoresEquipa() {
        return numJogadoresEquipa;
    }



}
