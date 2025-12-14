package Game;

public class Team {

    private int totalPoints;
    private String teamName;
    private int teamCode;

    public Team (String teamName, int teamCode) {
        this.teamName = teamName;
        this.teamCode = teamCode;
        this.totalPoints = 0;
    }

    public Team (int teamCode) {
        this.teamCode = teamCode;
    }

    public int getTotalPoints() {
        return totalPoints;
    }
    public void addPoints(int points) {
        this.totalPoints += points;
    }
    public int getTeamCode() {
        return teamCode;
    }
}
