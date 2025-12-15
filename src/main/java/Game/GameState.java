package Game;

import Utils.Constants;
import Utils.Records;
import Utils.Records.*;
import java.util.*;

public class GameState {

    private final int gameCode;
    private final int numEquipas;
    private final int numJogadoresEquipa;

    private final Team[] equipas;
    private Pergunta[] perguntas;
    private int indicePerguntaAtual = 0;

    private final Map<String, Player> players = new HashMap<>();

    public Map<Integer, List<Player>> getPlayersByTeam() {
        return playersByTeam;
    }

    public Map<String, Integer> getTeamByPlayer() {
        return teamByPlayer;
    }

    public Map<String, Player> getPlayers() {
        return players;
    }

    private final Map<Integer, List<Player>> playersByTeam = new HashMap<>();
    private final Map<String, Integer> teamByPlayer = new HashMap<>();

    private int respostasRecebidas = 0;
    private final HashSet<String> respondedPlayers = new HashSet<>();
    private int timoutSeconds = Constants.TIMOUT_SECS;

    public GameState(int numEquipas, int numJogadoresEquipa, int gameCode) {
        this.numEquipas = numEquipas;
        this.numJogadoresEquipa = numJogadoresEquipa;
        this.gameCode = gameCode;

        equipas = new Team[numEquipas];
        for (int i = 0; i < numEquipas; i++) {
            equipas[i] = new Team("Equipa " + (i + 1), i);
            playersByTeam.put(i, new ArrayList<>());
        }
    }

    public void setPerguntas(Pergunta[] perguntas) {
        this.perguntas = perguntas;
    }

    public Pergunta getPerguntaAtual() {
        if (perguntas == null || indicePerguntaAtual >= perguntas.length) return null;
        return perguntas[indicePerguntaAtual];
    }

    public synchronized int getIndicePerguntaAtual() {
        return indicePerguntaAtual;
    }

    public synchronized int getGameCode() {
        return gameCode;
    }

    public synchronized Player addPlayer(String username, int teamId) {
        if (players.containsKey(username)) return null;

        List<Player> team = playersByTeam.get(teamId);
        if (team.size() >= numJogadoresEquipa) return null;

        Player p = new Player(players.size(), username);
        players.put(username, p);
        team.add(p);
        teamByPlayer.put(username, teamId);

        return p;
    }

    //GAME LOGIC --------------------------------------------------

    public synchronized SendQuestion sendQuestionToAllPlayers() {
        Pergunta atual = getPerguntaAtual();
        if (atual == null) return null;

        return new SendQuestion(
                atual.getQuestao(),
                atual.getOpcoes(),
                indicePerguntaAtual,
                timoutSeconds
        );
    }

    public synchronized void registerAnswer(String username, int option) {
        if (respondedPlayers.contains(username)) return;

        Player p = players.get(username);
        if (p == null) return;

        p.setOpcaoEscolhida(option);
        respondedPlayers.add(username);
        respostasRecebidas++;
    }


    public synchronized boolean roundEnded() {
        return respostasRecebidas >= players.size()  ; // todo || roudTimeout
    }

//    public synchronized RoundResult endRound() {
//        Map<String, Integer> scores = new HashMap<>();
//
//        Pergunta atual = getPerguntaAtual();
//        for (Player p : players.values()) {
//            if (p.getOpcaoEscolhida() == atual.getRespostaCorreta()) {
//                p.incrementarPontuacao();
//            }
//            scores.put(p.getName(), p.getPontuacao());
//        }
//
//        indicePerguntaAtual++;
//        respondedPlayers.clear();
//        respostasRecebidas = 0;
//
//        boolean gameEnded = indicePerguntaAtual >= perguntas.length;
//        Pergunta next = gameEnded ? null : getPerguntaAtual();
//
//        return new RoundResult(true, gameEnded, scores, next);
//    }
}
