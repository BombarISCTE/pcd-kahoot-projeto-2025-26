package Messages;

import Game.Pergunta;
import Utils.Constants;

import java.io.Serializable;

public class SendQuestion implements Serializable {

    private final String question;
    private final String[] options;
    private final int questionNumber;
    private final int timeLimit = Constants.TEMPO_LIMITE_QUESTAO;

    public SendQuestion(String question, String[] options, int questionNumber) {
        this.question = question;
        this.options = options;
        this.questionNumber = questionNumber;
    }



}
