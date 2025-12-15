package Server;

import Game.*;

import java.io.IOException;
import java.util.Scanner;

public class TUI {
    private final Server server;
    private final Scanner scanner = new Scanner(System.in);

    public TUI (Server server) {
        this.server = server;
    }

    public void menuConsola() throws IOException {
        System.out.println("-----------Menu do Servidor-----------");

        System.out.println("Número de equipas: ");
        int numEquipas = scanner.nextInt();
        if (numEquipas < 1) {
            System.out.println("Número de equipas inválido. Deve ser pelo menos 1.");
            return;
        }

        System.out.println("Número de jogadores por equipa: ");
        int jogadoresPorEquipa = scanner.nextInt();
        if (jogadoresPorEquipa < 1) {
            System.out.println("Número de jogadores por equipa inválido. Deve ser pelo menos 1.");
            return;
        }

        System.out.println("Número de perguntas: ");
        int numPerguntas = scanner.nextInt();
        if (numPerguntas < 1) {
            System.out.println("Número de perguntas inválido. Deve ser pelo menos 1.");
            return;
        }

        String caminhoFicheiro = "src/main/java/Game/FicheiroQuestoes.json";
        Pergunta[] perguntas = Pergunta.lerPerguntas(caminhoFicheiro);

        GameState gameState = new GameState(numEquipas, jogadoresPorEquipa, numPerguntas);
        GameEngine gameEngine = new GameEngine(gameState, perguntas);
        String codigoJogo = server.createGame(gameEngine);
        System.out.println("Jogo criado com o código: " + codigoJogo);
    }

}
