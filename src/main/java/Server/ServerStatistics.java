package Server;

import Game.Team;

import java.util.ArrayList;
import java.util.Map;

public class ServerStatistics {
    private final Map<Integer, Integer> rankingJogadores;

    public ServerStatistics(Map<Integer, Integer> rankingJogadores) {
        this.rankingJogadores = rankingJogadores;
    }

    public Map<Integer, Integer> getRankingJogadores() {
        return rankingJogadores;
    }

}
