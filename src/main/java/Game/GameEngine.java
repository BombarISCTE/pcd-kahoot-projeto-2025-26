package Game;

import Utils.ModifiedCountdownLatch;

public class GameEngine {
    private GameState gameState;

    private DealWithQuestions dealWithQuestions;
    private DealWithIndividualAnswers individualHandler;
    private DealWithTeamAnswers[] equipaHandler;

    private int jogadoresProntos = 0;

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
            this.equipaHandler[equipaId - 1] = new DealWithTeamAnswers(gameState, equipaId);
        }
    }

    public void iniciarJogo(){
        System.out.println("A iniciar jogo...");
        Pergunta perguntaAtual = gameState.getPerguntaAtual();
        System.out.println("Pergunta nº" + gameState.getIndicePerguntaAtual() + 1 + ": " + perguntaAtual.getQuestao());
        iniciarPerguntaAtual();

//        if(perguntaAtual == null){
//            System.out.println("Erro: Nao ha perguntas disponiveis");
//            terminarJogo();
//            return;
//        }
//        if(isPerguntaIndividual()){
//            comecarPerguntaIndividual();
//        }else {
//            comecarPerguntaEquipa();
//        }
//
////        if(!dealWithQuestions.avancarProximaPergunta()){
////            break;
////        }
    }

    private void iniciarPerguntaAtual() {
        Pergunta perguntaAtual = gameState.getPerguntaAtual();
        if(perguntaAtual == null){
            System.out.println("Erro: Nao ha perguntas disponiveis");
            terminarJogo();
            return;
        }
        System.out.println("Pergunta nº " + (gameState.getIndicePerguntaAtual() + 1) + ": " + perguntaAtual.getQuestao());
        jogadoresProntos = 0;
        if(isPerguntaIndividual()){
            individualHandler.iniciarPerguntaIndividual();
        }else {
            for (DealWithTeamAnswers handlerEquipa : equipaHandler) {
                handlerEquipa.iniciarPerguntaEquipa();
            }
        }
    }

    private boolean isPerguntaIndividual(){
        if(gameState.getIndicePerguntaAtual() % 2 == 0){
            return true;
        }
        return false;
    }

//    private void comecarPerguntaIndividual(){
//        System.out.println("A iniciar pergunta individual...");
//        individualHandler.iniciarPerguntaIndividual();
//
//
//        individualHandler.esperarRespostasIndividuais();
//
//        System.out.println("Pergunta individual finalizada.");
//    }
//
//    private void comecarPerguntaEquipa(){
//        System.out.println("A iniciar pergunta de equipa...");
//        for(DealWithTeamAnswers handlerEquipa : equipaHandler){
//            handlerEquipa.iniciarPerguntaEquipa();
//        }
//        for(DealWithTeamAnswers handlerEquipa : equipaHandler){
//            handlerEquipa.esperarRespostasEquipa();
//        }
//
//        System.out.println("Pergunta de equipa finalizada.");
//    }

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


    public void jogadorProntoParaProximaPergunta(Player jogadorConectado) {
        jogadoresProntos++;
        if(jogadoresProntos == gameState.getNumEquipas()){
            avançarProximaPergunta();
        }

    }

    public void terminarJogo(){
        System.out.println("O jogo terminou.");

    }

    private void avançarProximaPergunta(){
        gameState.avancarParaProximaPergunta();
        if (gameState.acabouJogo()){
            terminarJogo();
        } else {
            iniciarPerguntaAtual();
        }

    }
}
