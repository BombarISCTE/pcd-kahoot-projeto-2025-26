package Game;

public class Player {
    private final int id;
    private final String name;
    private int score = 0;
    private int chosenOption = -1; //indice da opção escolhida
    private int responseOrder = -1; //quem respondeu primeiro, segundo, etc
    private Team team; //adicionar referência à equipa

    public Player(int id, String name, Team team) {
        this.id = id;
        this.name = name;
        this.team = team;
    }

    public void addScore(int points) { this.score += points; }

    public void setChosenOption(int chosenOption) { this.chosenOption = chosenOption; }

    public int getChosenOption() { return chosenOption; }

    public void resetChosenOption() { this.chosenOption = -1; }

    public Team getTeam() { return team; }
    public int getTeamId() { return team.getTeamId(); }

    public int getScore() { return score; }

    public void setResponseOrder(int order) { this.responseOrder = order; } //precisamos de saber os 2 primeiros para atribuir bónus
    public int getResponseOrder() { return responseOrder; }

    public int getId() { return id; }
    public String getName() { return name; }

    @Override
    public String toString() { return name + " (Score: " + score + ")"; }
}
