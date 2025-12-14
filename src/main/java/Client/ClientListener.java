package Client;

public interface ClientListener {

    void onNewQuestion(String pergunta, String[] opcoes, int numeroPergunta, int tempoLimite);

    void onEndGame(String mensagem);
}
