package Utils;

import Game.IndividualQuestion;
import Game.Question;
import Game.TeamQuestion;
import com.google.gson.*;
import java.io.*;

public class FormatQuestions {

    /**
     * Converte um caminho TXT para um caminho JSON equivalente
     */
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
        return newPath.toString();
    }

    /**
     * Lê um ficheiro TXT e limpa espaços em keys e valores, gerando JSON limpo
     */
    public static void txtTrimmer(String inputFilePath, String outputFilePath) throws IOException {
        StringBuilder sb = new StringBuilder();
        try (BufferedReader br = new BufferedReader(new FileReader(inputFilePath))) {
            String line;
            while ((line = br.readLine()) != null) {
                sb.append(line).append("\n");
            }
        }

        String json = sb.toString();

        // Limpar keys com espaços
        json = json.replaceAll("\"\\s*(.*?)\\s*\"\\s*:", "\"$1\":");

        // Limpar valores string
        json = json.replaceAll(":\\s*\"\\s*(.*?)\\s*\"", ": \"$1\"");

        // Limpar strings dentro de arrays
        json = json.replaceAll("\"\\s+(.*?)\\s+\"", "\"$1\"");

        try (BufferedWriter bw = new BufferedWriter(new FileWriter(outputFilePath))) {
            bw.write(json);
        }
    }

    /**
     * Lê perguntas de um ficheiro JSON já limpo e retorna array de Question
     * Alterna tipo: par = IndividualQuestion, ímpar = TeamQuestion
     */
    public static Question[] readQuestions(String filePath) throws IOException {
        String newPath = convertTxtToJsonPath(filePath);
        try (FileReader reader = new FileReader(newPath)) {
            Gson gson = new Gson();
            JsonElement root = JsonParser.parseReader(reader);
            JsonArray arr = root.getAsJsonArray();
            Question[] questions = new Question[arr.size()];

            for (int i = 0; i < arr.size(); i++) {
                JsonObject obj = arr.get(i).getAsJsonObject();
                String question = obj.get("question").getAsString();
                int points = obj.get("points").getAsInt();
                int correct = obj.get("correct").getAsInt();
                String[] options = gson.fromJson(obj.get("options"), String[].class);

                // Alterna tipo
                if (i % 2 == 0) {
                    questions[i] = new IndividualQuestion(question, correct, points, options, 0); // totalPlayers será configurado depois
                } else {
                    questions[i] = new TeamQuestion(question, correct, points, options, null); // lista de equipas será passada no GameState
                }
            }

            return questions;
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
            Question[] perguntas = Utils.FormatQuestions.readQuestions(jsonPath);

            System.out.println("Total de perguntas: " + perguntas.length + "\n");

            for (Question p : perguntas) {
                System.out.println(p); // usa o toString() da classe Pergunta
                System.out.println("-----------------------------------");
            }

        } catch (IOException e) {
            System.err.println("Erro ao ler o ficheiro de perguntas: " + e.getMessage());
        }
    }
}
