package Server;

import Game.GameState;
import Game.Pergunta;
import Utils.Records.*;

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
        System.out.println("-----------Server TUI-----------");
        System.out.println("Options: newGame | listGames | deleteGame | startGame | checkGameStats | exit");

        while (true) {
            System.out.println("\nTUI menu - Choose option > ");
            String line = scanner.nextLine();
            if (line == null) break;
            String cmd = line.trim().toLowerCase();
            if (cmd.isEmpty()) continue;

            try {
                switch (cmd) {
                    case "newgame", "new" -> handleNewGame();
                    case "listgames", "list", "ls" -> server.listGames();
                    case "deletegame", "delete", "remove" -> handleDeleteGame();
                    case "checkgamestats", "check" -> handleCheckGameStats();
                    case "start", "startgame" -> handleStartGame();
                    case "exit", "quit" -> {
                        System.out.println("TUI menu - Exiting TUI.");
                        System.out.println("TUI menu - this does not stop the server, just the TUI.");
                        return;
                    }
                    default -> System.out.println("TUI menu - Unknown option: " + cmd);
                }
            } catch (NumberFormatException nfe) {
                System.out.println("TUI menu - Invalid number format.");
            } catch (Exception e) {
                System.out.println("TUI menu - Error: " + e.getMessage());
            }
        }
    }

    private void handleNewGame() {
        try {
            System.out.print("TUI handleNewGame - Number of teams: ");
            int numEquipas = Integer.parseInt(scanner.nextLine().trim());
            System.out.print("TUI handleNewGame - Players per team: ");
            int jogadoresPorEquipa = Integer.parseInt(scanner.nextLine().trim());

            if (numEquipas < 1 || jogadoresPorEquipa < 1) {
                System.out.println("TUI handleNewGame - All numbers must be >= 1.");
                return;
            }

            int code = server.createGameId();
            GameState game = new GameState(numEquipas, jogadoresPorEquipa, code);
            System.out.print("TUI handleNewGame - Enter questions file name: ");
            String fileName = scanner.nextLine().trim();

            if (!fileName.isEmpty()) {
                String path = "src/main/resources/Perguntas/" + fileName;
                try {
                    Pergunta[] perguntas = Pergunta.lerPerguntas(path);
                    game.setQuestions(perguntas);
                    System.out.println("TUI handleNewGame - Loaded " + perguntas.length + " questions from " + path);
                } catch (Exception e) {
                    System.out.println("TUI handleNewGame - Could not load questions: " + e.getMessage());
                    return;
                }
            }

            server.addGame(game);
            System.out.println("TUI handleNewGame - Game created with code: " + game.getGameCode());

        } catch (NumberFormatException nfe) {
            System.out.println("TUI handleNewGame - Invalid number format, aborted creation.");
        }
    }

    private void handleStartGame() {
        try {
            System.out.print("TUI handleStartGame - Enter gameId to start: ");
            int code = Integer.parseInt(scanner.nextLine().trim());
            GameState game = server.getGame(code);
            if (game == null) {
                System.out.println("TUI handleStartGame - No game with code " + code);
                return;
            }

            System.out.println("TUI handleStartGame - Game " + code + " started.");

            // envia a primeira pergunta consoante o tipo
            Pergunta current = game.getCurrentQuestion();
            if (current == null) return;

            if (current instanceof Pergunta.PerguntaIndividual) {
                SendIndividualQuestion sq = game.createSendIndividualQuestion();
                for (ClientHandler ch : ClientHandler.clientHandlers) {
                    if (ch.getGameId() == code) ch.sendMessage(sq);
                }
            } else if (current instanceof Pergunta.PerguntaEquipa) {
                SendTeamQuestion sq = game.createSendTeamQuestion();
                for (ClientHandler ch : ClientHandler.clientHandlers) {
                    if (ch.getGameId() == code) ch.sendMessage(sq);
                }
            }

        } catch (NumberFormatException nfe) {
            System.out.println("TUI handleStartGame - Invalid game code.");
        }
    }

    private void handleDeleteGame() {
        try {
            System.out.print("TUI handleDeleteGame -Enter game code to delete: ");
            int code = Integer.parseInt(scanner.nextLine().trim());
            server.removeGame(code);
        } catch (NumberFormatException nfe) {
            System.out.println("TUI handleDeleteGame - Invalid game code.");
        }
    }

    private void handleCheckGameStats() {
        try {
            System.out.print("TUI handleCheckGameStats - Enter game code to inspect: ");
            int code = Integer.parseInt(scanner.nextLine().trim());
            GameState game = server.getGame(code);
            if (game == null) {
                System.out.println("TUI handleCheckGameStats - No game with code " + code);
                return;
            }
            SendFinalScores finalScores = game.getFinalScores();
            System.out.println("TUI handleCheckGameStats - Final Scores for Game " + code + ":");
            finalScores.finalScores().forEach((player, score) ->
                    System.out.println(player + ": " + score + " points"));
        } catch (NumberFormatException nfe) {
            System.out.println("TUI handleCheckGameStats - Invalid game code.");
        }
    }

}
