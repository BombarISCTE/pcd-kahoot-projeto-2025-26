package Game;

import java.util.ArrayList;

public class Team {
    private final String name;
    private final int teamId;
    private final int maxPlayersPerTeam;
    private final ArrayList<Player> players;

    public Team(String name, int teamId, int maxPlayersPerTeam) {
        if (maxPlayersPerTeam < 1) {
            throw new IllegalArgumentException("Uma equipa deve ter pelo menos 1 jogador.");
        }
        this.name = name;
        this.teamId = teamId;
        this.maxPlayersPerTeam = maxPlayersPerTeam;
        this.players = new ArrayList<>();
    }

    public synchronized void addPlayer(Player player) {
        if (players.size() >= maxPlayersPerTeam) {
            System.out.println("Não é possível adicionar mais jogadores, equipa cheia.");
            return;
        }
        players.add(player);
    }

    // Remove um jogador da equipa pelo username
    public synchronized void removePlayer(String name) {
        for (int i = 0; i < players.size(); i++) {
            if (players.get(i).getName().equals(name)) {
                players.remove(i);
                return;
            }
        }
    }

    public synchronized ArrayList<Player> getPlayers() {return new ArrayList<>(players);}

    // Retorna pontuação total da equipa
    public synchronized int getTotalScore() {
        int total = 0;
        for (int i = 0; i < players.size(); i++) {
            total += players.get(i).getScore();
        }
        return total;
    }

    // Verifica se a equipa está completa
    public synchronized boolean isFull() {return players.size() == maxPlayersPerTeam;}

    // Número atual de jogadores
    public synchronized int getCurrentSize() {return players.size();}

    public String getName() {return name;}

    public int getTeamId() {return teamId;}

    @Override
    public synchronized String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("Team ").append(name).append(" (ID: ").append(teamId).append(")\n");
        sb.append("Players (").append(players.size()).append("/").append(maxPlayersPerTeam).append("):\n");
        for (int i = 0; i < players.size(); i++) {
            sb.append(" - ").append(players.get(i)).append("\n");
        }
        sb.append("Total Score: ").append(getTotalScore());
        return sb.toString();
    }

    public Player getPlayer(String username) {
        for (Player p : players) {
            if (p.getName().equals(username)) {
                return p;
            }
        }
        return null;
    }
}
