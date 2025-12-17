package Server;

import Game.*;
import Messages.RoundStats;
import Messages.SendQuestion;
import Utils.ModifiedCountdownLatch;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class GameEngine implements Runnable{

    private final GameState gameState;
    private final Server server;
    private final GameStatistics gameStatistics;

    private DealWithQuestions dealWithQuestions;
    private DealWithIndividualAnswers individualHandlerAtivo;
    private DealWithTeamAnswers[] equipaHandlerAtivo;

//    private int equipasTerminadas = 0; //del

    private final Map<String, Player> players = new HashMap<>();
    private final Map<String, Integer> indiceJogador = new HashMap<>();
    private int proximoIndice = 0;

    private int jogadoresLigados = 0; //add
    private boolean jogoIniciado = false; //add

    public GameEngine(Server server, GameState gameState, Pergunta[] perguntas) {
        this.server = server;
        this.gameState = gameState;
        this.gameStatistics = new GameStatistics();

        this.dealWithQuestions = new DealWithQuestions(gameState);
        this.dealWithQuestions.carregarPerguntas(perguntas);

//        int totalJogadores = gameState.getTotalJogadores(); //del
//        ModifiedCountdownLatch latchIndividual = new ModifiedCountdownLatch(2, 2, 30, totalJogadores); //del
//        this.individualHandler = new DealWithIndividualAnswers(gameState, latchIndividual, gameStatistics); //del

//        int numEquipas = gameState.getNumEquipas(); //del
//        this.equipaHandler = new DealWithTeamAnswers[numEquipas]; //del
//        for (int equipaId = 1; equipaId <= numEquipas; equipaId++) { //del
//            this.equipaHandler[equipaId - 1] = new DealWithTeamAnswers(gameState, equipaId, gameStatistics); //del
//        }
    }

    @Override
    public void run(){
        iniciarJogo();
    }
    public void iniciarJogo(){
        System.out.println("A iniciar jogo...");
        Pergunta perguntaAtual = gameState.getPerguntaAtual();
        System.out.println("Pergunta nº" + gameState.getIndicePerguntaAtual() + 1 + ": " + perguntaAtual.getQuestao());
        iniciarPerguntaAtual();
    }

    private void iniciarPerguntaAtual() {
        Pergunta perguntaAtual = gameState.getPerguntaAtual();
        if(perguntaAtual == null){
            System.out.println("Erro: Nao ha perguntas disponiveis");
            terminarJogo();
            return;
        }
        System.out.println("Pergunta a ser enviada: " + perguntaAtual.getQuestao());
        enviarPerguntaAtualParaClientes();

        System.out.println("Pergunta nº " + (gameState.getIndicePerguntaAtual() + 1) + ": " + perguntaAtual.getQuestao());
        if(isPerguntaIndividual()){
            comecarPerguntaIndividual();
        }else {
            comecarPerguntaEquipa();
        }
    }

    private boolean isPerguntaIndividual(){
        if(gameState.getIndicePerguntaAtual() % 2 == 0){
            return true;
        }
        return false;
    }

    private void comecarPerguntaIndividual(){
        System.out.println("A iniciar pergunta individual...");

        int tempo = gameState.getPerguntaAtual().getMaxTimer();//add
        int totalJogadores = gameState.getTotalJogadores();//add

        ModifiedCountdownLatch latchIndividual = new ModifiedCountdownLatch(2, 2, tempo, totalJogadores);//add
        this.individualHandlerAtivo = new DealWithIndividualAnswers(gameState, latchIndividual, gameStatistics);//add

        individualHandlerAtivo.iniciarPerguntaIndividual();

        individualHandlerAtivo.esperarRespostasIndividuais();
        System.out.println("Pergunta individual finalizada.");

        System.out.println("A enviar estatisticas");
        enviarEstatisticas();

        individualHandlerAtivo = null; // add

        System.out.println("A avançar para a próxima pergunta...");
        avancarProximaPergunta();
    }

    private void enviarEstatisticas() {
        RoundStats stats = new RoundStats(gameStatistics.snapshot());
        server.broadcastToGame(gameState.getGameCode(), stats);
    }

    private void comecarPerguntaEquipa(){
        System.out.println("A iniciar pergunta de equipa...");

        this.equipaHandlerAtivo = new DealWithTeamAnswers[gameState.getNumEquipas()];//add

        for(int equipaId = 1; equipaId <= gameState.getNumEquipas(); equipaId++){//add
            equipaHandlerAtivo[equipaId - 1] = new DealWithTeamAnswers(gameState, equipaId /*gameStatistics*/);//add
            equipaHandlerAtivo[equipaId - 1].iniciarPerguntaEquipa();//add
        }

//        for(DealWithTeamAnswers handlerEquipa : equipaHandler){//del
//            handlerEquipa.iniciarPerguntaEquipa();//del
//        }

        for(DealWithTeamAnswers handlerEquipa : equipaHandlerAtivo){
            handlerEquipa.esperarAteFimDaRonda();
            int pontosGanhos = handlerEquipa.getPontosEquipa();
            Player[] jogadores = handlerEquipa.getJogadoresEquipa();

            for(Player jogador : jogadores){
                int indice = getIndiceJogador(jogador.getName());
                if(indice != -1){
                    gameStatistics.atualizaPontosJogadores(indice, pontosGanhos);
                }
            }
        }
        enviarEstatisticas();

        equipaHandlerAtivo = null;// add

        avancarProximaPergunta();
    }

    public synchronized void registarResposta(String username, int teamId, int opcaoEscolhida) {
        Player jogador = players.get(username);
        if (jogador == null || jogador.getOpcaoEscolhida() != -1) {
            return;
        }

        int indice = getIndiceJogador(username);//add
        if(indice == -1){//add
            return;//add
        }

        if (isPerguntaIndividual()) {
            if(individualHandlerAtivo != null){ //add
                int pontosGanhos = individualHandlerAtivo.registarRespostaIndividual(jogador, opcaoEscolhida);
                gameStatistics.atualizaPontosJogadores(indice, pontosGanhos);//add
            }
        } else {
            if(equipaHandlerAtivo != null) { //add
                equipaHandlerAtivo[teamId - 1].registarRespostaEquipa(jogador, opcaoEscolhida);
            }
        }
    }

    public void terminarJogo(){
        System.out.println("O jogo terminou.");
        enviarEstatisticas();
    }

    private void avancarProximaPergunta(){
        gameState.avancarParaProximaPergunta();
        if (gameState.acabouJogo()){
            terminarJogo();
        } else {
            iniciarPerguntaAtual();
        }
    }

//del
//    public void equipaTerminou(int equipaId){
//        boolean avancar = false;
//        synchronized(this) {
//            equipasTerminadas++;
//            System.out.println("Equipa: " + equipaId + " terminou a pergunta. Total equipas terminadas: " + equipasTerminadas + "/" + gameState.getNumEquipas());
//
//            if (equipasTerminadas == gameState.getNumEquipas()) {
//                System.out.println("Todas as equipas terminaram a pergunta.");
//                equipasTerminadas = 0;
//                avancar = true;
//            }
//        }
//        if(avancar){
//            System.out.println("A avançar para a próxima pergunta...");
//            avancarProximaPergunta();
//        }
//    }

    public synchronized boolean registarJogador(int equipaId, String nomeJogador) {
        if (players.containsKey(nomeJogador)){
            return false;
        }

        if(jogoIniciado){
            return false;
        }

        Player jogador = gameState.ocuparSlotJogador(equipaId, nomeJogador);
        if(jogador == null){
            return false;
        }

        players.put(nomeJogador, jogador);//add
        jogadoresLigados++;//add
        indiceJogador.put(nomeJogador, proximoIndice++);//add
        System.out.println("Jogador registado: " + nomeJogador + " na equipa " + equipaId);
        System.out.println("[DEBUG] jogadoresLigados = " + jogadoresLigados + " / " + gameState.getTotalJogadores());
        if(!jogoIniciado && jogadoresLigados == gameState.getTotalJogadores()){//add
            jogoIniciado = true;//add
            new Thread(this).start();//add
        }
        return true;
    }

//    public synchronized void adicionarCliente(DealWithClient cliente) {
//        clientes.add(cliente);
//    }

    private void enviarPerguntaAtualParaClientes() {
        Pergunta perguntaAtual = gameState.getPerguntaAtual();
        SendQuestion novaPergunta = new SendQuestion(perguntaAtual.getQuestao(), perguntaAtual.getOpcoes(), gameState.getIndicePerguntaAtual() + 1);
        server.broadcastToGame(gameState.getGameCode(), novaPergunta);
    }

    //add
    public synchronized void removerJogador(String nomeJogador){
        Player jogador = players.remove(nomeJogador);
        if(jogador != null){
            jogador.desconectarJogador();
            jogadoresLigados--;
            indiceJogador.remove(nomeJogador);//add
            System.out.println("Jogador removido: " + nomeJogador + " (" + jogadoresLigados + "/" + gameState.getTotalJogadores() + ")");
        }else {
            System.out.println("[DEBUG] removerJogador ignorado (não registado): " + nomeJogador);

        }
    }

    //add
    public synchronized int getIndiceJogador(String nome){
        Integer indice = indiceJogador.get(nome);
        if(indice == null){
            return -1;
        }
        return indice;
    }

}
