package Game;

import Utils.ModifiedCountdownLatch;

public class GameEngine {
    private GameState gameState;

    private DealWithQuestions dealWithQuestions;
    private DealWithIndividualAnswers individualHandler;
    private DealWithTeamAnswers[] teamHandler;

    public GameEngine(GameState gameState, Pergunta[] perguntas) {
        this.gameState = gameState;

        this.dealWithQuestions = new DealWithQuestions(gameState);
        this.dealWithQuestions.carregarPerguntas(perguntas);

        int totalJogadore = gameState.getTotalJogadores();
        ModifiedCountdownLatch latchIndividual = new ModifiedCountdownLatch(2, 2, 30, totalJogadore);
        this.individualHandler = new DealWithIndividualAnswers(gameState, latchIndividual);

        int numEquipas = gameState.getNumEquipas();
        this.teamHandler = new DealWithTeamAnswers[numEquipas];
        for (int equipaId = 1; equipaId <= numEquipas; equipaId++) {
            this.teamHandler[equipaId - 1] = new DealWithTeamAnswers(gameState, equipaId);
        }
    }

    public void iniciarJogo(){
        System.out.println("A iniciar jogo...");
        while(!gameState.acabouJogo()){
            Pergunta perguntaAtual = gameState.getPerguntaAtual();
            System.out.println("Pergunta nº" + gameState.getIndicePerguntaAtual() + 1 + ": " + perguntaAtual.getQuestao());

            if(isPerguntaIndividual()){
                comecarPerguntaIndividual();
            }else {
                comecarPerguntaEquipa();
            }

            if(!dealWithQuestions.avancarProximaPergunta()){
                break;
            }

        }

        System.out.println("A jogo finalizado com sucesso!");
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
    }

    private void comecarPerguntaEquipa(){
        System.out.println("A iniciar pergunta de equipa...");
        for(DealWithTeamAnswers handlerEquipa : teamHandler){
            handlerEquipa.iniciarPerguntaEquipa();
        }
        for(DealWithTeamAnswers handlerEquipa : teamHandler){
            handlerEquipa.esperarRespostasEquipa();
        }

        System.out.println("Pergunta de equipa finalizada.");
    }
}
