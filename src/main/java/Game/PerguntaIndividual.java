package Game;

public class PerguntaIndividual extends Pergunta {

    public PerguntaIndividual(String questao, int pontos, int opcaoCorreta, String[] opcoes) {
        super(questao, pontos, opcaoCorreta, opcoes);
    }

    //neste tipo de perguntas, a classificacao é calculada para cada jogador, somando-se os valores de todos os membros da mesma equipa
}
