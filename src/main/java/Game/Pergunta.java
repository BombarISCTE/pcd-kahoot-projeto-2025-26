package Game;

import com.google.gson.Gson;

import java.io.FileReader;
import java.io.IOException;

public class Pergunta {

    private String question;
    private int points;
    private int correct;

    private static final int MAX_TIMER = 30; //segundos

    //utilizar uma lista de strings para as opcoes pois é mais flexivel da ap ra adicionar mais opcoes se necessario ou remover, e é mais moderno e facil de usar:
    //um array de string é mais antigo e menos flexivel iriamos ter de ter sempre o mesmo numero de respostas possiveis
    //private List<String> opcoes;

    //o array de strings é mais simples de implementar e entender para este caso especifico, onde o numero de opcoes é fixo (4 opcoes)
    private String[] options;

    public Pergunta(String question,  int correct,int points, /*List<String> options*/ String[] options) {
        this.question = question;
        this.points = points;
        this.correct = correct;
        this.options = options;
    }

    public String getQuestao() {
        return question;
    }

    public int getPontos() {
        return points;
    }

    public int getCorrect() {
        return correct;
    }

    public String[] getOpcoes() {
        return options;
    }

//    public List<String> getOptions() {
//        return options;
//    }

    public boolean verificarResposta(int opcaoEscolhida) {return correct == opcaoEscolhida;}

    public int getMaxTimer() {return MAX_TIMER;}

    @Override
    public String toString() {
        StringBuilder str = new StringBuilder();
        str.append("Pergunta: ").append(question).append("\n");
        str.append("Pontos: ").append(points).append("\n");
        str.append("Opções:\n");
        for (int i = 0; i < options.length; i++) {
            str.append((i + 1)).append(". ").append(options[i]).append("\n");
        }
        str.append("Opção Correta: ").append(correct).append("\n");
        return str.toString();
    }



    public static Pergunta[] lerPerguntas(String caminhoFicheiro) throws IOException {
        try (FileReader reader = new FileReader(caminhoFicheiro)) {
            return new Gson().fromJson(reader, Pergunta[].class);
        }
    }
// todo trim Perguntas.txt e adequar e passar a .json
    public static void main(String[] args) {
        // Caminho relativo: procura o ficheiro dentro do projeto
        String caminhoFicheiro = "src/main/java/Game/FicheiroQuestoes.json";

        try {
            Pergunta[] perguntas = Pergunta.lerPerguntas(caminhoFicheiro);

            System.out.println("Total de perguntas: " + perguntas.length + "\n");

            for (Pergunta p : perguntas) {
                System.out.println(p); // usa o toString() da classe Pergunta
                System.out.println("-----------------------------------");
            }

        } catch (IOException e) {
            System.err.println("Erro ao ler o ficheiro de perguntas: " + e.getMessage());
        }
    }



}
