package Messages;

import Utils.Constants;

import java.io.Serializable;

public class NewQuestion implements Serializable {
    private String pergunta;
    private String[] opcoes;
    private int numeroPergunta;
    private int tempoLimite;

    public NewQuestion(String pergunta, String[] opcoes, int numeroPergunta) {
        this.pergunta = pergunta;
        this.opcoes = opcoes;
        this.numeroPergunta = numeroPergunta;
        this.tempoLimite = Constants.TEMPO_LIMITE_QUESTAO;
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

    public int getTempoLimite() {
        return tempoLimite;
    }

}
