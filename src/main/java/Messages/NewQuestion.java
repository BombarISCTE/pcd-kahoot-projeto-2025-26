package Messages;

import java.io.Serializable;

public class NewQuestion implements Serializable {
    private String pergunta;
    private String[] opcoes;
    private int numeroPergunta;
    private int tempoLimite;
    private long tempoInicio;

    public NewQuestion(String pergunta, String[] opcoes, int numeroPergunta, int tempoLimite, long tempoInicio) {
        this.pergunta = pergunta;
        this.opcoes = opcoes;
        this.numeroPergunta = numeroPergunta;
        this.tempoLimite = tempoLimite;
        this.tempoInicio = tempoInicio;
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

    public long getTempoInicio() {
        return tempoInicio;
    }

}
