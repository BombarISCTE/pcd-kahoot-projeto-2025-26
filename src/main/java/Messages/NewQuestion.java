package Messages;

import java.io.Serializable;

public class NewQuestion implements Serializable {
    private String pergunta;
    private String[] opcoes;
    private int numeroPergunta;
    private int tempoResposta;

    public NewQuestion(String pergunta, String[] opcoes, int numeroPergunta, int tempoResposta) {
        this.pergunta = pergunta;
        this.opcoes = opcoes;
        this.numeroPergunta = numeroPergunta;
        this.tempoResposta = tempoResposta;
    }

    public String getPergunta() {
        return pergunta;
    }

    public String[] getOpcoes() {
        return opcoes;
    }

    public int getNumeroPergunta() {
        return numeroPergunta;
    }

    public int getTempoResposta() {
        return tempoResposta;
    }

}
