package Game;

/*
Class: Player

Public constructors:
 - Player(String name, int teamId)

Public / synchronized methods (signatures):
 - void addScore(int points)
 - void setChosenOption(int chosenOption)
 - int getChosenOption()
 - void resetChosenOption()
 - int getTeamId()
 - int getScore()
 - void setResponseOrder(int order)
 - int getResponseOrder()
 - String getName()
 - String toString()
*/

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

    public synchronized void addScore(int points) { this.score += points; }

    public synchronized void setChosenOption(int chosenOption) { this.chosenOption = chosenOption; }

    public synchronized int getChosenOption() { return chosenOption; }

    public synchronized void resetChosenOption() { this.chosenOption = -1; }

    public int getTeamId() { return teamId; }

    public synchronized int getScore() { return score; }

    public synchronized void setResponseOrder(int order) { this.responseOrder = order; } //precisamos de saber os 2 primeiros para atribuir bónus
    public synchronized int getResponseOrder() { return responseOrder; }

    public String getName() { return name; }

    @Override
    public String toString() { return name + " (Score: " + score + ")"; }
}
