package Game;

/*
Class: Question

Public constructors:
 - Question(String questionText, int correct, int points, String[] options)

Public methods (signatures):
 - String getQuestionText()
 - int getPoints()
 - int getCorrect()
 - String[] getOptions()
 - abstract void processResponses(Team team)
 - abstract StringBuilder formatedClassName()
 - String toString()

Notes: abstract base class for questions. The concrete subclasses implement processResponses and formatedClassName.
*/

public abstract class Question {
    protected String questionText;
    protected int correct;
    protected int points;
    protected String[] options;

    public Question(String questionText, int correct, int points, String[] options) {
        this.questionText = questionText;
        this.correct = correct;
        this.points = points;
        this.options = options;
    }

    public String getQuestionText() {
        return questionText;
    }

    public int getPoints() {
        return points;
    }

    public int getCorrect() {
        return correct;
    }

    public String[] getOptions() {
        return options;
    }

    public abstract void processResponses(Team team);

    public abstract StringBuilder formatedClassName();

    @Override
    public String toString() {
        StringBuilder str = new StringBuilder();
        str.append(this.formatedClassName().append(questionText).append("\n"));
        str.append("Pontos: ").append(points).append("\n");
        str.append("Opções:\n");
        for (int i = 0; i < options.length; i++) {
            str.append((i + 1)).append(". ").append(options[i]).append("\n");
        }
        str.append("Opção Correta: ").append(correct).append("\n");
        return str.toString();
    }

}
