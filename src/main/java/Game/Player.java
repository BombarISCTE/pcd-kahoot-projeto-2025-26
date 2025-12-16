package Game;

public class Player {
    private final int id;
    private final String name;
    private int score;
    private int chosenOption = -1;
    private boolean connected;

    public Player(int id, String name) {
        this.id = id;
        this.name = name;
        this.score = 0;
        this.connected = true;
    }

    public void addScore(int points) {
        this.score += points;
    }

    public void setChosenOption(int chosenOption) {
        this.chosenOption = chosenOption;
    }

    public int getChosenOption() {
        return chosenOption;
    }

    public void resetChosenOption() {
        this.chosenOption = -1;
    }

    public void disconnect() {
        this.connected = false;
        resetChosenOption();
    }

    public void connect() {
        this.connected = true;
    }

    public boolean isConnected() {
        return connected;
    }

    public int getScore() {
        return score;
    }

    public int getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public String toString() {
        return name + " (Score: " + score + ")";
    }
}
