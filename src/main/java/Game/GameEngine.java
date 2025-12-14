package Game;

import Messages.NewQuestion;
import Server.DealWithClient;
import Utils.ModifiedCountdownLatch;

import java.util.ArrayList;
import java.util.List;

public class GameEngine {

    private GameState gameState;
    private DealWithQuestions dealWithQuestions;
    private DealWithIndividualAnswers individualHandler;
    private DealWithTeamAnswers[] equipaHandler;

    private final List<DealWithClient> clientes;

    private int equipasTerminadas = 0;

    public GameEngine(GameState gameState, Pergunta[] perguntas) {
        this.gameState = gameState;

        this.dealWithQuestions = new DealWithQuestions(gameState);
        this.dealWithQuestions.carregarPerguntas(perguntas);

        int totalJogadores = gameState.getTotalJogadores();
        ModifiedCountdownLatch latchIndividual = new ModifiedCountdownLatch(2, 2, 30, totalJogadores);
        this.individualHandler = new DealWithIndividualAnswers(gameState, latchIndividual);

        int numEquipas = gameState.getNumEquipas();
        this.equipaHandler = new DealWithTeamAnswers[numEquipas];
        for (int equipaId = 1; equipaId <= numEquipas; equipaId++) {
            this.equipaHandler[equipaId - 1] = new DealWithTeamAnswers(gameState, equipaId, this);
        }

        this.clientes = new ArrayList<>();
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

        System.out.println("A avançar para a próxima pergunta...");
        avancarProximaPergunta();
    }

    private void comecarPerguntaEquipa(){
        System.out.println("A iniciar pergunta de equipa...");

        for(DealWithTeamAnswers handlerEquipa : equipaHandler){
            handlerEquipa.iniciarPerguntaEquipa();
        }
    }

    public synchronized void registarResposta(Player jogador, int opcaoEscolhida){
        if(isPerguntaIndividual()){
            individualHandler.registarRespostaIndividual(jogador, opcaoEscolhida);
        }else {
            int equipaId = gameState.getEquipaDoJogador(jogador);
            if(equipaId == -1){
                System.out.println("Erro: Jogador nao pertence a nenhuma equipa");
                return;
            }
            equipaHandler[equipaId - 1].registarRespostaEquipa(jogador, opcaoEscolhida);
        }
    }

    public void terminarJogo(){
        System.out.println("O jogo terminou.");
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

    public Player registarJogador(int equipaId, String nomeJogador) {
        Player jogador = gameState.ocuparSlotJogador(equipaId, nomeJogador);
        return jogador;
    }

    public synchronized void adicionarCliente(DealWithClient cliente) {
        clientes.add(cliente);
    }

    private void enviarPerguntaAtualParaClientes() {
        Pergunta perguntaAtual = gameState.getPerguntaAtual();
        NewQuestion novaPergunta = new NewQuestion(perguntaAtual.getQuestao(), perguntaAtual.getOpcoes(), gameState.getIndicePerguntaAtual() + 1);
        for(DealWithClient cliente : clientes) {
            cliente.enviarMensagem(novaPergunta);
        }
    }

}
