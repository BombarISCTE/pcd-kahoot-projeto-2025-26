package Game;

public class Player {

    private final String name;
    private int score = 0;
    private int chosenOption = -1; //indice da opção escolhida
    private int responseOrder = -1; //quem respondeu primeiro, segundo, etc
    private int teamId; //adicionar referência à equipa

    public Player(String name, int teamId) {
        this.name = name;
        this.teamId = teamId;
    }

    public void addScore(int points) { this.score += points; }

    public void setChosenOption(int chosenOption) { this.chosenOption = chosenOption; }

    public int getChosenOption() { return chosenOption; }

    public void resetChosenOption() { this.chosenOption = -1; }

    public int getTeamId() { return teamId; }

    public int getScore() { return score; }

    public void setResponseOrder(int order) { this.responseOrder = order; } //precisamos de saber os 2 primeiros para atribuir bónus
    public int getResponseOrder() { return responseOrder; }

    public String getName() { return name; }

    @Override
    public String toString() { return name + " (Score: " + score + ")"; }
}
