package Game;

import com.google.gson.*;

import java.io.*;

//public abstract class Pergunta {
//
//    protected String question;
//    protected int points;
//    protected int correct;
//    protected String[] options;
//
//    public Pergunta(String question,  int correct,int points, String[] options) {
//        this.question = question;
//        this.correct = correct;
//        this.points = points; // pontos atribuídos por responder corretamente
//        this.options = options;
//    }
//
//    public String getQuestion() {
//        return question;
//    }
//
//    public int getPoints() {
//        return points;
//    }
//
//    public int getCorrect() {
//        return correct;
//    }
//
//    public String[] getOptions() {
//        return options;
//    }
//
//    public String getOption(int index) {
//        if (index < 0 || index >= options.length) return null;
//        return options[index];
//    }
//
//    public boolean isCorrect(int choice) {return correct == choice;}
//
//    public int getPointsForAnswer(int choice) {return isCorrect(choice) ? points : 0;}
//
//    public String toJson() {return new Gson().toJson(this);}
//
//    public boolean verificarResposta(int opcaoEscolhida) {return correct == opcaoEscolhida;} //é opcaoEscolhida é um int
//
//
//    @Override
//    public String toString() {
//        StringBuilder str = new StringBuilder();
//        str.append("Pergunta: ").append(question).append("\n");
//        str.append("Pontos: ").append(points).append("\n");
//        str.append("Opções:\n");
//        for (int i = 0; i < options.length; i++) {
//            str.append((i + 1)).append(". ").append(options[i]).append("\n");
//        }
//        str.append("Opção Correta: ").append(correct).append("\n");
//        return str.toString();
//    }
//
//
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
//    //como Perguntas é abstract, o Json nao converte diretamente para Pergunta
//    public static Pergunta[] lerPerguntas(String filePath) throws IOException {
//        String newPath = convertTxtToJsonPath(filePath);
//        try (FileReader reader = new FileReader(newPath)) {
//            Gson gson = new Gson();
//            JsonElement root = JsonParser.parseReader(reader); //ler o conteúdo do ficheiro JSON
//            JsonArray arr = root.getAsJsonArray(); //obter o array de perguntas
//            Pergunta[] perguntas = new Pergunta[arr.size()]; //criar um array de perguntas com o tamanho do array JSON
//
//            for (int i = 0; i < arr.size(); i++) {
//                JsonObject obj = arr.get(i).getAsJsonObject();
//                String question = obj.get("question").getAsString();
//                int points = obj.get("points").getAsInt();
//                int correct = obj.get("correct").getAsInt();
//                String[] options = gson.fromJson(obj.get("options"), String[].class);
//
//                if (i % 2 == 0) {
//                    perguntas[i] = new Pergunta.PerguntaIndividual(question, correct, points, options);
//                } else {
//                    perguntas[i] = new Pergunta.PerguntaEquipa(question, correct, points, options);
//                }
//            }
//            return perguntas;
//        }
//    }
//
//
//
//
//    public static void txtTrimmer(String inputFilePath, String outputFilePath) throws IOException {
//        StringBuilder sb = new StringBuilder();
//
//        try (BufferedReader br = new BufferedReader(new FileReader(inputFilePath))) {
//            String line;
//            while ((line = br.readLine()) != null) {
//                sb.append(line).append("\n");
//            }
//        }
//        String json = sb.toString();
//
//        // Limpar keys com espaços: "  question  " -> "question"
//        json = json.replaceAll("\"\\s*(.*?)\\s*\"\\s*:", "\"$1\":");
//
//        // Limpar valores string: "  Resposta  " -> "Resposta"
//        json = json.replaceAll(":\\s*\"\\s*(.*?)\\s*\"", ": \"$1\"");
//
//        // Limpar strings dentro de arrays: "   Valor   " -> "Valor"
//        json = json.replaceAll("\"\\s+(.*?)\\s+\"", "\"$1\"");
//
//        // Guardar o JSON limpo
//        try (BufferedWriter bw = new BufferedWriter(new FileWriter(outputFilePath))) {
//            bw.write(json);
//        }
//    }
//
//
//
//    public static void main(String[] args) {
//
//        String path = "src/main/resources/Perguntas/FicheiroComPerguntas.txt";
//        String jsonPath = convertTxtToJsonPath(path);
//
//        try{
//            txtTrimmer(path, jsonPath);
//        } catch (IOException e){
//            System.err.println("Erro ao trimar o ficheiro de perguntas: " + e.getMessage());
//        }
//
//        try {
//            Pergunta[] perguntas = Pergunta.lerPerguntas(jsonPath);
//
//            System.out.println("Total de perguntas: " + perguntas.length + "\n");
//
//            for (Pergunta p : perguntas) {
//                System.out.println(p); // usa o toString() da classe Pergunta
//                System.out.println("-----------------------------------");
//            }
//
//        } catch (IOException e) {
//            System.err.println("Erro ao ler o ficheiro de perguntas: " + e.getMessage());
//        }
//    }
//
//    public abstract int calcularPontuacao(int choice);
//
//    //  subclasses ------------------------------------------
//
//    public static class PerguntaIndividual extends Pergunta {
//
//        public PerguntaIndividual(String question, int correct, int points, String[] options) {
//            super(question, correct, points, options);
//        }
//
//        @Override
//        public int calcularPontuacao(int choice) {
//            return isCorrect(choice) ? points : 0;
//        }
//    }
//
//    public static class PerguntaEquipa extends Pergunta {
//
//        public PerguntaEquipa(String question, int correct, int points, String[] options) {
//            super(question, correct, points, options);
//        }
//
//        @Override
//        public int calcularPontuacao(int choice) {
//            return isCorrect(choice) ? points : 0;
//        }
//    }
//
//}