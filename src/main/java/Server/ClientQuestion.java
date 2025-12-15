package Server;

import Game.Pergunta;

public class ClientQuestion {
    private final String question;
    private final String[] options;

    public ClientQuestion(Pergunta pergunta) {
        this.question = pergunta.getQuestao();
        this.options = pergunta.getOpcoes();
    }
}