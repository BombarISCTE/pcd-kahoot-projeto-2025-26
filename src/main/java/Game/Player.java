package Game;

public class Player {
    private final int id;
    private final String name;
    private int score = 0;
    private int chosenOption = -1;
    private int responseOrder = Integer.MAX_VALUE;
    private boolean connected = true;

    public Player(int id, String name) { this.id = id; this.name = name; }

    public void addScore(int points) { this.score += points; }

    public void setChosenOption(int chosenOption) { this.chosenOption = chosenOption; }

    public int getChosenOption() { return chosenOption; }

    public void resetChosenOption() { this.chosenOption = -1; }

    public int getScore() { return score; }

    public void setResponseOrder(int order) { this.responseOrder = order; } //precisamos de saber os 2 primeiros para atribuir bónus
    public int getResponseOrder() { return responseOrder; }

    public int getId() { return id; }
    public String getName() { return name; }

    public boolean isConnected() { return connected; }
    public void disconnect() { connected = false; resetChosenOption(); }
    public void connect() { connected = true; }

    @Override
    public String toString() { return name + " (Score: " + score + ")"; }
}
