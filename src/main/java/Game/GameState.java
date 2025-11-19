package Game;

import java.util.HashMap;

public class GameState {

    private final String gameCode;

    private final int numEquipas;
    private int numJogadoresEquipa;

    private Team[] equipas;
    private Player[][] jogadores;

    private Pergunta[] perguntas;
    private int indicePerguntaAtual = 0;

    private HashMap<Player, Integer> respostasJogadores;


    public GameState(int numEquipas, int numJogadoresEquipa, int numPerguntas) {
        this.numEquipas = numEquipas;
        this.numJogadoresEquipa = numJogadoresEquipa;

        gameCode = GeradorCodigo.gerarCodigo();

        this.equipas = new Team[numEquipas];
        this.jogadores = new Player[numEquipas][numJogadoresEquipa];

        this.perguntas = new Pergunta[numEquipas];

        this.respostasJogadores = new HashMap<>();

        for(int i = 0; i < numEquipas; i++) {
            equipas[i] = new Team("Equipa " + (i + 1), i + 1);
            for(int j = 0; j < numJogadoresEquipa; j++) {
                jogadores[i][j] = new Player(j, "Jogador " + (j + 1) + " da Equipa " + (i + 1));
            }
        }
    }

    public int getNumEquipas() {
        return numEquipas;
    }

    public int getNumJogadoresEquipa() {
        return numJogadoresEquipa;
    }

    public String getGameCode() {
        return gameCode;
    }

    public Pergunta[] getPerguntas() {
        return perguntas;
    }

    public int getIndicePerguntaAtual() {
        return indicePerguntaAtual;
    }

    public Team[] getEquipas() {
        return equipas;
    }

    public Player[][] getJogadores() {
        return jogadores;
    }

    public Pergunta getPerguntaAtual() {
        if(indicePerguntaAtual < perguntas.length) {
            return perguntas[indicePerguntaAtual];
        }
        return null;
    }

    public boolean acabouJogo() {
        return indicePerguntaAtual >= perguntas.length;
    }

    private void limparRespostas() {
        respostasJogadores.clear();
    }

    public boolean avancarProximaPergunta(){
        indicePerguntaAtual++;
        if(acabouJogo()){
            return false;
        }
        limparRespostas();
        return true;
    }

    public boolean registrarResposta(Player jogador, int opcaoEscolhida) {
        if(respostasJogadores.containsKey(jogador)) {
            return false;
        }
        respostasJogadores.put(jogador, opcaoEscolhida);
        return true;
    }

    public boolean respostaCorreta(Player jogador) {
        if(!respostasJogadores.containsKey(jogador)) {
            return false;
        }
        Pergunta perguntaAtual = getPerguntaAtual();
        int opcaoEscolhida = respostasJogadores.get(jogador);
        return perguntaAtual.verificarResposta(opcaoEscolhida);
    }

    public boolean todasRespostasRecebidas(){
        int totalJogadores = numEquipas * numJogadoresEquipa;
        if(respostasJogadores.size() >= totalJogadores){
            return true;
        }
        return false;
    }

    public boolean equipaRespondeu(int equipaID){
        int count = 0;
        for(Player jogador : jogadores[equipaID]){
            if(respostasJogadores.containsKey(jogador)){
                count++;
            }
        }
        if(count == numJogadoresEquipa){
            return true;
        }
        return false;
    }



}
