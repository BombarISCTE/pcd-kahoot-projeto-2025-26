package Server;

import Game.GameState;
import Game.Question;
import Utils.Records.SendFinalScores;

import java.io.IOException;
import java.util.Scanner;

/*
 TUI commands:
  - newGame
  - listGames
  - deleteGame
  - startGame
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
        System.out.println("----------- Server TUI -----------");
        System.out.println("Options: newGame | listGames | deleteGame | startGame | checkGameStats | exit");

        while (true) {
            System.out.print("\nTUI > ");
            String line = scanner.nextLine();
            if (line == null) break;

            String cmd = line.trim().toLowerCase();
            if (cmd.isEmpty()) continue;

            try {
                switch (cmd) {
                    case "newgame", "new" -> handleNewGame();
                    case "listgames", "list", "ls" -> server.listGames();
                    case "deletegame", "delete", "remove" -> handleDeleteGame();
                    case "start", "startgame" -> handleStartGame();
                    case "checkgamestats", "check" -> handleCheckGameStats();
                    case "exit", "quit" -> {
                        System.out.println("Exiting TUI (server keeps running).");
                        return;
                    }
                    default -> System.out.println("Unknown option: " + cmd);
                }
            } catch (Exception e) {
                System.out.println("TUI error: " + e.getMessage());
            }
        }
    }

    /* =======================
       COMMAND HANDLERS
       ======================= */

    private void handleNewGame() {
        try {
            System.out.print("Number of teams: ");
            int numTeams = Integer.parseInt(scanner.nextLine().trim());

            System.out.print("Players per team: ");
            int playersPerTeam = Integer.parseInt(scanner.nextLine().trim());

            if (numTeams < 1 || playersPerTeam < 1) {
                System.out.println("Values must be >= 1.");
                return;
            }

            int gameId = server.createGameId();
            GameState game = new GameState(numTeams, playersPerTeam, gameId);

            System.out.print("Questions file name: ");
            String fileName = scanner.nextLine().trim();

            if (!fileName.isEmpty()) {
                String path = "src/main/resources/Perguntas/" + fileName;
                Question[] perguntas = Utils.FormatQuestions.readQuestions(path);
                game.setQuestions(perguntas);
                System.out.println("Loaded " + perguntas.length + " questions.");
            }

            server.addGame(game);
            System.out.println("Game created with code: " + gameId);

        } catch (Exception e) {
            System.out.println("Failed to create game: " + e.getMessage());
        }
    }

    private void handleStartGame() {
        try {
            System.out.print("Enter gameId: ");
            int gameId = Integer.parseInt(scanner.nextLine().trim());

            GameState game = server.getGame(gameId);
            if (game == null) {
                System.out.println("No game with code " + gameId);
                return;
            }

            server.startGame(gameId);
            System.out.println("Game " + gameId + " started.");

        } catch (NumberFormatException e) {
            System.out.println("Invalid gameId.");
        }
    }

    private void handleDeleteGame() {
        try {
            System.out.print("Enter gameId to delete: ");
            int gameId = Integer.parseInt(scanner.nextLine().trim());
            server.removeGame(gameId);
        } catch (NumberFormatException e) {
            System.out.println("Invalid gameId.");
        }
    }

    private void handleCheckGameStats() {
        try {
            System.out.print("Enter gameId: ");
            int gameId = Integer.parseInt(scanner.nextLine().trim());

            GameState game = server.getGame(gameId);
            if (game == null) {
                System.out.println("No game with code " + gameId);
                return;
            }

            SendFinalScores scores = game.getFinalScores();
            System.out.println("Final scores for game " + gameId + ":");
            scores.finalScores().forEach(
                    (player, score) ->
                            System.out.println(player + ": " + score)
            );

        } catch (NumberFormatException e) {
            System.out.println("Invalid gameId.");
        }
    }
}
