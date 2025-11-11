package Server;

import java.util.List;

public class Questao {

    private String questao;
    private int pontos;
    private int opcaoCorreta;

    //utilizar uma lista de strings para as opcoes pois é mais flexivel da ap ra adicionar mais opcoes se necessario ou remover, e é mais moderno e facil de usar:
    //um array de string é mais antigo e menos flexivel iriamos ter de ter sempre o mesmo numero de respostas possiveis
    //private List<String> opcoes;

    //o array de strings é mais simples de implementar e entender para este caso especifico, onde o numero de opcoes é fixo (4 opcoes)
    private String[] opcoes;

    public Questao(String questao, int pontos, int opcaoCorreta, /*List<String> opcoes*/ String[] opcoes) {
        this.questao = questao;
        this.pontos = pontos;
        this.opcaoCorreta = opcaoCorreta;
        this.opcoes = opcoes;
    }

    public String getQuestao() {
        return questao;
    }

    public int getPontos() {
        return pontos;
    }

    public int getOpcaoCorreta() {
        return opcaoCorreta;
    }

    public String[] getOpcoes() {
        return opcoes;
    }

//    public List<String> getOpcoes() {
//        return opcoes;
//    }

    public boolean verificarResposta(int opcaoEscolhida) {
        if(opcaoCorreta == opcaoEscolhida) {
            return true;
        }
        return false;
    }


}
