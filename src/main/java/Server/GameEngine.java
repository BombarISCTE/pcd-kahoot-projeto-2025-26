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
    private DealWithIndividualAnswers individualHandler;
    private DealWithTeamAnswers[] equipaHandler;

    private int equipasTerminadas = 0;

    private final Map<String, Player> players = new HashMap<>();

    public GameEngine(Server server, GameState gameState, Pergunta[] perguntas) {
        this.server = server;
        this.gameState = gameState;
        this.gameStatistics = new GameStatistics();

        this.dealWithQuestions = new DealWithQuestions(gameState);
        this.dealWithQuestions.carregarPerguntas(perguntas);

        int totalJogadores = gameState.getTotalJogadores();
        ModifiedCountdownLatch latchIndividual = new ModifiedCountdownLatch(2, 2, 30, totalJogadores);
        this.individualHandler = new DealWithIndividualAnswers(gameState, latchIndividual, gameStatistics);

        int numEquipas = gameState.getNumEquipas();
        this.equipaHandler = new DealWithTeamAnswers[numEquipas];
        for (int equipaId = 1; equipaId <= numEquipas; equipaId++) {
            this.equipaHandler[equipaId - 1] = new DealWithTeamAnswers(gameState, equipaId, gameStatistics);
        }
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
        individualHandler.iniciarPerguntaIndividual();

        individualHandler.esperarRespostasIndividuais();
        System.out.println("Pergunta individual finalizada.");

        System.out.println("A enviar estatisticas");
        enviarEstatisticas();
        System.out.println("A avançar para a próxima pergunta...");
        avancarProximaPergunta();
    }

    private void enviarEstatisticas() {
        RoundStats stats = new RoundStats(gameStatistics.snapshot());
        server.broadcastToGame(gameState.getGameCode(), stats);
    }

    private void comecarPerguntaEquipa(){
        System.out.println("A iniciar pergunta de equipa...");

        for(DealWithTeamAnswers handlerEquipa : equipaHandler){
            handlerEquipa.iniciarPerguntaEquipa();
        }

        for(DealWithTeamAnswers handlerEquipa : equipaHandler){
            handlerEquipa.esperarAteFimDaRonda();
        }

        enviarEstatisticas();
        avancarProximaPergunta();
    }

    public synchronized void registarResposta(String username, int teamId, int opcaoEscolhida) {
        Player jogador = players.get(username);
        if (jogador == null || jogador.getOpcaoEscolhida() != -1) {
            return;
        }
        if (isPerguntaIndividual()) {
            individualHandler.registarRespostaIndividual(jogador, opcaoEscolhida);
        } else {
            equipaHandler[teamId - 1].registarRespostaEquipa(jogador, opcaoEscolhida);
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

    public void equipaTerminou(int equipaId){
        boolean avancar = false;
        synchronized(this) {
            equipasTerminadas++;
            System.out.println("Equipa: " + equipaId + " terminou a pergunta. Total equipas terminadas: " + equipasTerminadas + "/" + gameState.getNumEquipas());

            if (equipasTerminadas == gameState.getNumEquipas()) {
                System.out.println("Todas as equipas terminaram a pergunta.");
                equipasTerminadas = 0;
                avancar = true;
            }
        }
        if(avancar){
            System.out.println("A avançar para a próxima pergunta...");
            avancarProximaPergunta();
        }
    }

    public synchronized void registarJogador(int equipaId, String nomeJogador) {
        if (players.containsKey(nomeJogador)) return;

        Player jogador = gameState.ocuparSlotJogador(equipaId, nomeJogador);
        if (jogador != null){
            players.put(nomeJogador, jogador);
            System.out.println("Jogador registado: " + nomeJogador + " na equipa " + equipaId);
        }
    }

//    public synchronized void adicionarCliente(DealWithClient cliente) {
//        clientes.add(cliente);
//    }

    private void enviarPerguntaAtualParaClientes() {
        Pergunta perguntaAtual = gameState.getPerguntaAtual();
        SendQuestion novaPergunta = new SendQuestion(perguntaAtual.getQuestao(), perguntaAtual.getOpcoes(), gameState.getIndicePerguntaAtual() + 1);
        server.broadcastToGame(gameState.getGameCode(), novaPergunta);
    }

}
