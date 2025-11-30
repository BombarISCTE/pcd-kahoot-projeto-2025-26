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

    public int getRespostasRecebidas() {
        return respostasRecebidas;
    }

    public int getNumPerguntas() {
        return numPerguntas;
    }

    public void reporRespostasRecebidas(){
        respostasRecebidas = 0;
    }

    public void reporOrdemRespostas(){
        ordemRespostas = 0;
    }

    public void reporRespostasEquipa(){
        for(int i = 0; i < respostasEquipa.length; i++) {
            respostasEquipa[i] = 0;
        }
    }

    public void incrementarIndicePerguntaAtual(){
        indicePerguntaAtual++;
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

    public synchronized int registarRespostaIndividual(){
        ordemRespostas++;
        respostasRecebidas++;
        return ordemRespostas;
    }

    public synchronized void registarRespostaEquipa(int equipaID){
        respostasEquipa[equipaID]++;
    }

    public boolean equipaRespondeu(int equipaID){
        if(respostasEquipa[equipaID] == numJogadoresEquipa){
            return true;
        }
        return false;
    }



//    public boolean registrarResposta(Player jogador, int opcaoEscolhida) {
//        if(respostasJogadores.containsKey(jogador)) {
//            return false;
//        }
//        respostasJogadores.put(jogador, opcaoEscolhida);
//        return true;
//    }
//
//    public boolean respostaCorreta(Player jogador) {
//        if(!respostasJogadores.containsKey(jogador)) {
//            return false;
//        }
//        Pergunta perguntaAtual = getPerguntaAtual();
//        int opcaoEscolhida = respostasJogadores.get(jogador);
//        return perguntaAtual.verificarResposta(opcaoEscolhida);
//    }
//
//    public boolean todasRespostasRecebidas(){
//        int totalJogadores = numEquipas * numJogadoresEquipa;
//        if(respostasJogadores.size() >= totalJogadores){
//            return true;
//        }
//        return false;
//    }
//
//    public boolean equipaRespondeu(int equipaID){
//        int count = 0;
//        for(Player jogador : jogadores[equipaID]){
//            if(respostasJogadores.containsKey(jogador)){
//                count++;
//            }
//        }
//        if(count == numJogadoresEquipa){
//            return true;
//        }
//        return false;
//    }



}
