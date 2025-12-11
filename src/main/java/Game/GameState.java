package Game;

import Utils.IdCodeGenerator;

import java.util.*;

public class GameState {

    private final String gameCode;

    private final int numEquipas;
    private int numJogadoresEquipa;
    private int numPerguntas;

    private Team[] equipas;
    private Player[][] jogadores;

    private Pergunta[] perguntas;
    private int indicePerguntaAtual = 0;

    private int respostasRecebidas = 0;
    private int respostasEquipa[];

    private int ordemRespostas = 0;

    public GameState(int numEquipas, int numJogadoresEquipa, int numPerguntas) {
        this.numEquipas = numEquipas;
        this.numJogadoresEquipa = numJogadoresEquipa;
        this.numPerguntas = numPerguntas;

        gameCode = IdCodeGenerator.gerarCodigo();

        this.equipas = new Team[numEquipas];
        this.jogadores = new Player[numEquipas][numJogadoresEquipa];

        this.respostasEquipa = new int[numEquipas];

        for(int i = 0; i < numEquipas; i++) {
            equipas[i] = new Team("Equipa " + (i + 1), i + 1);
            for(int j = 0; j < numJogadoresEquipa; j++) {
                jogadores[i][j] = new Player(i * numJogadoresEquipa + j, "Jogador " + (j + 1) + " da Equipa " + (i + 1));
            }
        }
    }

    public int getNumEquipas() {
        return numEquipas;
    }

    public int getNumJogadoresEquipa() {
        return numJogadoresEquipa;
    }

    public int getRespostasRecebidas() {
        return respostasRecebidas;
    }

    public int getNumPerguntas() {
        return numPerguntas;
    }

    public int getOrdemRespostas() {
        return ordemRespostas;
    }

    public int getIndicePerguntaAtual() {
        return indicePerguntaAtual;
    }

    public int getTotalJogadores() {
        return numEquipas * numJogadoresEquipa;
    }

    public Player[] getJogadoresDaEquipa(int equipaID){
        int indiceEquipa = equipaID - 1;
        if(indiceEquipa < 0 || indiceEquipa >= jogadores.length){
            throw new IllegalArgumentException("Equipa ID inválido: " + equipaID);
        }
        return jogadores[indiceEquipa];
    }

    public Player getJogador(int equipaId, int jogadorId){
        int indiceEquipa = equipaId - 1;
        if(indiceEquipa < 0 || indiceEquipa >= jogadores.length || jogadorId < 0 || jogadorId >= jogadores[indiceEquipa].length){
            throw new IllegalArgumentException("Equipa ID ou Jogador ID inválido: " + equipaId + ", " + jogadorId);
        }
        return jogadores[indiceEquipa][jogadorId];
    }

    public Team[] getEquipas() {
        return equipas;
    }

    public Pergunta getPerguntaAtual() {
        if(indicePerguntaAtual < perguntas.length) {
            return perguntas[indicePerguntaAtual];
        }
        return null;
    }

    public int getEquipaDoJogador(Player jogador){
        for(int equipa = 0; equipa < equipas.length; equipa++){
            for(Player p : jogadores[equipa]){
                if(p.getId() == jogador.getId()){
                    return equipa + 1;
                }
            }
        }
        return -1;
    }

    public void reporRespostasEquipa(){
        for(int i = 0; i < respostasEquipa.length; i++) {
            respostasEquipa[i] = 0;
        }
    }

    public void reporOpcoesEscolhidas(){
        for(int i = 0; i < jogadores.length; i++) {
            for(int j = 0; j < jogadores[i].length; j++) {
                jogadores[i][j].resetOpcaoEscolhida();
            }
        }
    }

    public void incrementarIndicePerguntaAtual(){
        indicePerguntaAtual++;
    }

    public void incrementarOrdemRespostas(){
        ordemRespostas++;
    }


    public void setPerguntas(Pergunta[] perguntas) {
        this.perguntas = perguntas;
    }

    public boolean acabouJogo() {
        return indicePerguntaAtual >= perguntas.length;
    }

    public synchronized int registarRespostaIndividual(){
        incrementarOrdemRespostas();
        respostasRecebidas++;
        return ordemRespostas;
    }

    public synchronized void registarRespostaEquipa(int equipaID){
        int indiceEquipa = equipaID - 1;
        if(indiceEquipa < 0 || indiceEquipa >= numEquipas){
            throw new IllegalArgumentException("Equipa ID inválido: " + equipaID);
        }
        respostasEquipa[indiceEquipa]++;
    }

    public void avancarParaProximaPergunta(){
        indicePerguntaAtual++;
        reporRespostas();
    }

    public void reporRespostas() {
        respostasRecebidas = 0;
        ordemRespostas = 0;
        reporOpcoesEscolhidas();
        reporRespostasEquipa();
    }

}
