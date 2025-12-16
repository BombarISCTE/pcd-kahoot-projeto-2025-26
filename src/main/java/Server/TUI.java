package Server;

import Game.GameState;
import Game.Pergunta;
import Utils.Records;

import java.io.IOException;
import java.util.Scanner;

/*
 TUI commands:
  - newGame
  - listGames
  - deleteGame
  - checkGameStats
  - exit
*/
public class TUI {
    private final Scanner scanner;
    private final Server server;

    public TUI(Server server) {
        this.scanner = new Scanner(System.in);
        this.server = server;
    }

    public void menu() throws IOException {
        System.out.println("-----------Server TUI-----------");
        System.out.println("Options: newGame | listGames | deleteGame | startGame | checkGameStats | exit");

//        // optional: preload questions for admin info (non-fatal)
//        try {
//            String caminhoFicheiro = "src/main/java/Game/FicheiroQuestoes.json";
//            Pergunta[] perguntas = Pergunta.lerPerguntas(caminhoFicheiro);
//            System.out.println("Loaded " + perguntas.length + " questions (if file exists).");
//        } catch (Exception ignored) {}

        while (true) {
            System.out.print("\nChoose option > ");
            String line = scanner.nextLine();
            if (line == null) break;
            String cmd = line.trim().toLowerCase(); // para tornar case-insensitive
            if (cmd.isEmpty()) continue;

            try {
                switch (cmd) {
                    case "newgame":
                    case "new":
                        handleNewGame();
                        break;

                    case "listgames":
                    case "list":
                    case "ls":
                        server.listGames();
                        break;

                    case "deletegame":
                    case "delete":
                    case "remove":
                        handleDeleteGame();
                        break;

                    case "checkgamestats":
                    case "check":
                        handleCheckGameStats();
                        break;

                    case "start":
                    case "startgame":
                        handleStartGame();
                        break;

                    case "exit":
                    case "quit":
                        System.out.println("Exiting TUI.");
                        System.out.println("this does not stop the server, just the TUI.");
                        return;

                    default:
                        System.out.println("Unknown option: " + cmd);
                        break;
                }
            } catch (NumberFormatException nfe) {
                System.out.println("Invalid number format.");
            } catch (Exception e) {
                System.out.println("Error: " + e.getMessage());
            }
        }
    }

    private void handleNewGame() {
        try {
            System.out.print("Number of teams: ");
            int numEquipas = Integer.parseInt(scanner.nextLine().trim());
            System.out.print("Players per team: ");
            int jogadoresPorEquipa = Integer.parseInt(scanner.nextLine().trim());

            if (numEquipas < 1 || jogadoresPorEquipa < 1) {
                System.out.println("All numbers must be >= 1.");
                return;
            }

            int code = server.createGameId();
            GameState game = new GameState(numEquipas, jogadoresPorEquipa, code);
            System.out.print("Enter questions file name: ");
            String fileName = scanner.nextLine().trim();

            if (!fileName.isEmpty()) {
                String path = "src/main/resources/Perguntas/" + fileName;
                try {
                    Pergunta[] perguntas = Pergunta.lerPerguntas(path);
                    game.setQuestions(perguntas);
                    System.out.println("Loaded " + perguntas.length + " questions from " + path);
                } catch (Exception e) {
                    System.out.println("Could not load questions from Game/: " + e.getMessage());
                    return;
                }
            }

            server.addGame(game);
            System.out.println("Game created with code: " + game.getGameCode());

        } catch (NumberFormatException nfe) {
            System.out.println("Invalid number format, aborted creation.");
        }
    }

    private void handleStartGame() {
        try {
            System.out.print("Enter gameId to start: ");
            int code = Integer.parseInt(scanner.nextLine().trim());
            GameState game = server.getGame(code);
            if (game == null) {
                System.out.println("No game with code " + code);
                return;
            }

            System.out.println("Game " + code + " started.");

            // envia primeira pergunta
            Records.SendQuestion sendQuestion = game.createSendQuestion(30);
            if (sendQuestion != null) {
                for (ClientHandler ch : ClientHandler.clientHandlers) {
                    if (ch.getGameId() == code) {
                        ch.sendMessage(sendQuestion);
                    }
                }
            }

        } catch (NumberFormatException nfe) {
            System.out.println("Invalid game code.");
        }
    }



    private void handleDeleteGame() {
        try {
            System.out.print("Enter game code to delete: ");
            String s = scanner.nextLine().trim();
            int code = Integer.parseInt(s);
            server.removeGame(code);
        } catch (NumberFormatException nfe) {
            System.out.println("Invalid game code.");
        }
    }

    private void handleCheckGameStats() {
        try {
            System.out.print("Enter game code to inspect: ");
            String s = scanner.nextLine().trim();
            int code = Integer.parseInt(s);
            GameState game = server.getGame(code);
            if (game == null) {
                System.out.println("No game with code " + code);
                return;
            }
            Records.SendFinalScores finalScores = game.getFinalScores();
            System.out.println("Final Scores for Game " + code + ":");
            finalScores.finalScores().forEach((player, score) ->
                    System.out.println(player + ": " + score + " points"));
        } catch (NumberFormatException nfe) {
            System.out.println("Invalid game code.");
        }
    }

}
