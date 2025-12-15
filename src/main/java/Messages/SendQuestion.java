package Messages;

import Utils.Constants;
import java.io.Serializable;

public class SendQuestion implements Serializable {

    private final String question;
    private final String[] options;
    private final int questionNumber;
    private final int timeLimit;

    public SendQuestion(String question, String[] options, int questionNumber) {
        this.question = question;
        this.options = options;
        this.questionNumber = questionNumber;
        this.timeLimit = Constants.TEMPO_LIMITE_QUESTAO;
    }

    public String getQuestion() {
        return question;
    }

    public String[] getOptions() {
        return options;
    }

    public int getQuestionNumber() {
        return questionNumber;
    }

    public int getTimeLimit() {
        return timeLimit;
    }
}
