package Game;

public class Player {
    private int id;
    private String name;
    // private boolean isHost;
    private int score;
    //private int rank;
    //private boolean isPlaying;
    private int opcaoEscolhida = -1;
    private boolean jogadorConectado = false;

    public Player(int id, String name) {
        this.id = id;
        this.name = name;
    }

    @Override
    public String toString() {
        return "Player " + id + "- " + name;
    }

    public void adicionarPontos(int pontosGanhos) {
        this.score += pontosGanhos;
    }

    public void setOpcaoEscolhida(int opcaoEscolhida) {
        this.opcaoEscolhida = opcaoEscolhida;
    }

    public int getOpcaoEscolhida() {
        return opcaoEscolhida;
    }

    public int getScore() {
        return score;
    }

    public void resetOpcaoEscolhida() {
        this.opcaoEscolhida = -1;
    }

    public int getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public void jogadorAtivo(String nomeJogador) {
        this.name = nomeJogador;
        this.jogadorConectado = true;
    }

    public boolean isJogadorConectado() {
        return jogadorConectado;
    }

    public void desconectarJogador() {
        this.jogadorConectado = false;
        this.opcaoEscolhida = -1;
        this.name = null;
    }

}