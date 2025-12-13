package Messages;

import java.io.Serializable;

public class NewQuestion implements Serializable {
    private String pergunta;
    private String[] opcoes;
    private int numeroPergunta;
    private int tempoMaximo;

    public NewQuestion(String pergunta, String[] opcoes, int numeroPergunta, int tempoMaximo) {
        this.pergunta = pergunta;
        this.opcoes = opcoes;
        this.numeroPergunta = numeroPergunta;
        this.tempoMaximo = tempoMaximo;
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
        return tempoMaximo;
    }

}
