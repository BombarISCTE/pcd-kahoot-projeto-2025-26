package Game;

public class PerguntaEquipa extends Pergunta {
    public PerguntaEquipa(String questao, int pontos, int opcaoCorreta, String[] opcoes) {
        super(questao, pontos, opcaoCorreta, opcoes);
    }


    // neste tipo de perguntas, a cotacao das +erguntas de u,a equipa apenas serao decididas apos a rececao das
    // respostas de todos os membros da equipa ou quando o tempo expirar



}
