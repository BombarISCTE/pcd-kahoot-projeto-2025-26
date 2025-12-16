package Game;

import com.google.gson.*;
import java.io.*;

public abstract class Question {
    protected String questionText;
    protected int correct;
    protected int points;
    protected String[] options;

    public Question(String questionText, int correct, int points, String[] options) {
        this.questionText = questionText;
        this.correct = correct;
        this.points = points;
        this.options = options;
    }

    public String getQuestionText() {
        return questionText;
    }

    public int getPoints() {
        return points;
    }

    public int getCorrect() {
        return correct;
    }

    public String[] getOptions() {
        return options;
    }

    public abstract void processResponses(Team team);

//    // --- Converter TXT para JSON ---
//    public static String convertTxtToJsonPath(String filePath) {
//        String[] pathParts = filePath.split("\\.");
//        StringBuilder newPath = new StringBuilder();
//        for (int i = 0; i < pathParts.length - 1; i++) {
//            newPath.append(pathParts[i]);
//            if (i < pathParts.length - 2) {
//                newPath.append(".json");
//            }
//        }
//        newPath.append(".json");
//        return String.valueOf(newPath);
//    }
//
//    // --- Limpar TXT e gerar JSON ---
//    public static void txtTrimmer(String inputFilePath, String outputFilePath) throws IOException {
//        StringBuilder sb = new StringBuilder();
//        try (BufferedReader br = new BufferedReader(new FileReader(inputFilePath))) {
//            String line;
//            while ((line = br.readLine()) != null) {
//                sb.append(line).append("\n");
//            }
//        }
//
//        String json = sb.toString();
//
//        // Limpar keys com espaços
//        json = json.replaceAll("\"\\s*(.*?)\\s*\"\\s*:", "\"$1\":");
//
//        // Limpar valores string
//        json = json.replaceAll(":\\s*\"\\s*(.*?)\\s*\"", ": \"$1\"");
//
//        // Limpar strings dentro de arrays
//        json = json.replaceAll("\"\\s+(.*?)\\s+\"", "\"$1\"");
//
//        try (BufferedWriter bw = new BufferedWriter(new FileWriter(outputFilePath))) {
//            bw.write(json);
//        }
//    }
//
//    // --- Ler perguntas a partir do JSON ---
//    public static Question[] readQuestions(String filePath) throws IOException {
//        String newPath = convertTxtToJsonPath(filePath);
//        try (FileReader reader = new FileReader(newPath)) {
//            Gson gson = new Gson();
//            JsonElement root = JsonParser.parseReader(reader);
//            JsonArray arr = root.getAsJsonArray();
//            Question[] questions = new Question[arr.size()];
//
//            for (int i = 0; i < arr.size(); i++) {
//                JsonObject obj = arr.get(i).getAsJsonObject();
//                String question = obj.get("question").getAsString();
//                int points = obj.get("points").getAsInt();
//                int correct = obj.get("correct").getAsInt();
//                String[] options = gson.fromJson(obj.get("options"), String[].class);
//
//                // alternar tipo de pergunta: par = IndividualQuestion, ímpar = TeamQuestion
//                if (i % 2 == 0) {
//                    questions[i] = new IndividualQuestion(question, correct, points, options);
//                } else {
//                    questions[i] = new TeamQuestion(question, correct, points, options);
//                }
//            }
//            return questions;
//        }
//    }
}
