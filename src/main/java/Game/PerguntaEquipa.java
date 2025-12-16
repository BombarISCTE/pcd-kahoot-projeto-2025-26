package Game;

public class PerguntaEquipa extends Pergunta {
    public PerguntaEquipa(String questao, int pontos, int opcaoCorreta, String[] opcoes) {
        // Pergunta constructor expects (question, correct, points, options)
        super(questao, opcaoCorreta, pontos, opcoes);
    }


    // neste tipo de perguntas, a cotacao das perguntas de uma equipa apenas serao decididas apos a rececao das
    // respostas de todos os seus elementos ou quando o tempo expirar


}
