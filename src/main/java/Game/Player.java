package Game;

public class Player {
    private int id;
    private String name;
    // private boolean isHost;
    private int score;
    //private int rank;
    //private boolean isPlaying;

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

}