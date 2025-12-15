package Game;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class GameState {

    //private final String gameCode;
    private final int gameCode;

    private final int numEquipas;
    private int numJogadoresEquipa;
    private int numPerguntas;

    private Team[] equipas;
    // private Player[][] jogadores;

    // New structures: keep players by username and by team (0-based team IDs)
    private final Map<String, Player> jogadoresMap = new ConcurrentHashMap<>(); // username -> Player
    private final Map<Integer, List<Player>> jogadoresPorEquipa = new ConcurrentHashMap<>(); // teamId(0-based) -> list of Players
    // quick map username -> teamId for O(1) lookup
    private final Map<String, Integer> jogadorParaEquipa = new ConcurrentHashMap<>();

    private int nextPlayerId = 0;

    private Pergunta[] perguntas;
    private int indicePerguntaAtual = 0;

    private int respostasRecebidas = 0;
    private int respostasEquipa[];

    private int ordemRespostas = 0;

    public GameState(int numEquipas, int numJogadoresEquipa, int numPerguntas, int gameCode) {
        this.numEquipas = numEquipas;
        this.numJogadoresEquipa = numJogadoresEquipa;
        this.numPerguntas = numPerguntas;

        //gameCode = IdCodeGenerator.gerarCodigo();
        this.gameCode = gameCode;

        this.equipas = new Team[numEquipas];
        //this.jogadores = new Player[numEquipas][numJogadoresEquipa];

        this.respostasEquipa = new int[numEquipas];

        for(int i = 0; i < numEquipas; i++) {
            // use 0-based teamCode
            equipas[i] = new Team("Equipa " + (i + 1), i);
            jogadoresPorEquipa.put(i, new ArrayList<>());
            // previously we precreated Player slots; now we create players when they join/ocuparSlotJogador
            // for(int j = 0; j < numJogadoresEquipa; j++) {
            //     jogadores[i][j] = new Player(i * numJogadoresEquipa + j, "Jogador " + (j + 1) + " da Equipa " + (i + 1));
            // }
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

    // Return an array of players for a given team (0-based teamId). If team has empty slots, the returned array may be smaller than numJogadoresEquipa
    public Player[] getJogadoresDaEquipa(int equipaID){
        if(equipaID < 0 || equipaID >= numEquipas){
            throw new IllegalArgumentException("Equipa ID inválido: " + equipaID);
        }
        List<Player> lista = jogadoresPorEquipa.get(equipaID);
        if(lista == null) return new Player[0];
        return lista.toArray(new Player[0]);
    }

    // keep the same semantics: jogadorId is index inside the team's list (0-based)
    public Player getJogador(int equipaId, int jogadorId){
        if(equipaId < 0 || equipaId >= numEquipas) {
            throw new IllegalArgumentException("Equipa ID inválido: " + equipaId);
        }
        List<Player> lista = jogadoresPorEquipa.get(equipaId);
        if(lista == null || jogadorId < 0 || jogadorId >= lista.size()){
            throw new IllegalArgumentException("Equipa ID ou Jogador ID inválido: " + equipaId + ", " + jogadorId);
        }
        return lista.get(jogadorId);
    }

    public Team[] getEquipas() {
        return equipas;
    }

    public Pergunta getPerguntaAtual() {
        if(perguntas == null) return null;
        if(indicePerguntaAtual < perguntas.length) {
            return perguntas[indicePerguntaAtual];
        }
        return null;
    }

    public int getEquipaDoJogador(Player jogador){
        if(jogador == null) return -1;
        // try fast path using name -> team map
        String name = jogador.getName();
        if(name != null) {
            Integer t = jogadorParaEquipa.get(name);
            if(t != null) return t;
        }
        // fallback: search lists
        for(int equipa = 0; equipa < numEquipas; equipa++){
            List<Player> lista = jogadoresPorEquipa.get(equipa);
            if(lista == null) continue;
            for(Player p : lista){
                if(p.getId() == jogador.getId()){
                    return equipa;
                }
            }
        }
        return -1;
    }

    public int getEquipaDoJogador(String username) {
        if(username == null) return -1;
        return jogadorParaEquipa.getOrDefault(username, -1);
    }

    public void reporRespostasEquipa(){
        for(int i = 0; i < respostasEquipa.length; i++) {
            respostasEquipa[i] = 0;
        }
    }

    public void reporOpcoesEscolhidas(){
        for(Player p : jogadoresMap.values()) {
            p.resetOpcaoEscolhida();
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
        if(perguntas == null) return true;
        return indicePerguntaAtual >= perguntas.length;
    }

    public synchronized int registarRespostaIndividual(){
        incrementarOrdemRespostas();
        respostasRecebidas++;
        return ordemRespostas;
    }

    public synchronized void registarRespostaEquipa(int equipaID){
        if(equipaID < 0 || equipaID >= numEquipas){
            throw new IllegalArgumentException("Equipa ID inválido: " + equipaID);
        }
        respostasEquipa[equipaID]++;
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

    // Try to occupy a slot in the given team (0-based). Returns the created Player (with username assigned) or null if no slot available.
    public Player ocuparSlotJogador(int equipaId, String nomeJogador) {
        if(equipaId < 0 || equipaId >= numEquipas) return null;
        List<Player> lista = jogadoresPorEquipa.get(equipaId);
        if(lista == null) return null;
        if(lista.size() >= numJogadoresEquipa) return null; // no free slot

        Player p = new Player(nextPlayerId++, nomeJogador);
        p.jogadorAtivo(nomeJogador);
        lista.add(p);
        jogadoresMap.put(nomeJogador, p);
        jogadorParaEquipa.put(nomeJogador, equipaId);
        return p;
    }

    @Override
    public String toString() { // todo method toString
        return "GameState{" +
                "gameCode=" + gameCode +
                ", numEquipas=" + numEquipas +
                ", numJogadoresEquipa=" + numJogadoresEquipa +
                ", numPerguntas=" + numPerguntas +
                ", indicePerguntaAtual=" + indicePerguntaAtual +
                '}';
    }


    public int getGameCode() {
        return gameCode;
    }
}
