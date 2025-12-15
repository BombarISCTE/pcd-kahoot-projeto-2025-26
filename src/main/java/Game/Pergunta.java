package Game;

import com.google.gson.Gson;

import java.io.*;

public class Pergunta {

    private String question;
    private int points;
    private int correct;

    private String[] options;

    public Pergunta(String question,  int correct,int points, String[] options) {
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


    public boolean verificarResposta(int opcaoEscolhida) {return correct == opcaoEscolhida;} //é opcaoEscolhida é um int


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


    public static String convertTxtToJsonPath(String filePath) {
        String[] pathParts = filePath.split("\\.");
        StringBuilder newPath = new StringBuilder();
        for (int i = 0; i < pathParts.length - 1; i++) {
            newPath.append(pathParts[i]);
            if (i < pathParts.length - 2) {
                newPath.append(".json");
            }
        }
        newPath.append(".json");
        return String.valueOf(newPath);
    }

    public static Pergunta[] lerPerguntas(String filePath) throws IOException {
        String newPath = convertTxtToJsonPath(filePath);
        try (FileReader reader = new FileReader(newPath)) {
            return new Gson().fromJson(reader, Pergunta[].class);
        }
    }



    public static void txtTrimmer(String inputFilePath, String outputFilePath) throws IOException {
        StringBuilder sb = new StringBuilder();

        // 1. Ler tudo para uma única String
        try (BufferedReader br = new BufferedReader(new FileReader(inputFilePath))) {
            String line;
            while ((line = br.readLine()) != null) {
                sb.append(line).append("\n");
            }
        }
        String json = sb.toString();

        // 2. Limpar keys com espaços: "  question  " -> "question"
        json = json.replaceAll("\"\\s*(.*?)\\s*\"\\s*:", "\"$1\":");

        // 3. Limpar valores string: "  Resposta  " -> "Resposta"
        json = json.replaceAll(":\\s*\"\\s*(.*?)\\s*\"", ": \"$1\"");

        // 4. Limpar strings dentro de arrays: "   Valor   " -> "Valor"
        json = json.replaceAll("\"\\s+(.*?)\\s+\"", "\"$1\"");

        // 5. Guardar o JSON limpo
        try (BufferedWriter bw = new BufferedWriter(new FileWriter(outputFilePath))) {
            bw.write(json);
        }
    }

    public static void main(String[] args) {

        String path = "src/main/resources/Perguntas/FicheiroComPerguntas.txt";
        String jsonPath = convertTxtToJsonPath(path);

        try{
            txtTrimmer(path, jsonPath);
        } catch (IOException e){
            System.err.println("Erro ao trimar o ficheiro de perguntas: " + e.getMessage());
        }

        try {
            Pergunta[] perguntas = Pergunta.lerPerguntas(jsonPath);

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