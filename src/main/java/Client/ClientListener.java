package Client;

import java.util.Map;

public interface ClientListener {

    void onNewQuestion(String pergunta, String[] opcoes, int numeroPergunta, int tempoLimite, long tempoInicioServidor);

    void onEndGame(String mensagem);

    void onStatistic(Map<Integer, Integer> pontosJogadores);
}
