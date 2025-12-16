package Client;

import Game.Pergunta;
import Utils.Constants;
import Utils.Records;

import javax.swing.*;
import java.awt.*;
import java.util.ArrayList;
import java.util.HashMap;

public class ClientGUI extends JFrame {

    JLabel mensagemEspaco;
    private JLabel perguntaEspaco;
    private JButton[] opcoesBotoes = new JButton[4];
    private JLabel[] playerScoreLabels; // para atualizar o placar dinamicamente
    private Pergunta currentQuestion;
    private final Client client;

    public ClientGUI(Client client) {
        this.client = client;
        initGUI();
    }

    private void initGUI() {
        this.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        this.setSize(500, 400);
        this.setLayout(new BorderLayout(10, 10));
        getRootPane().setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        // Pergunta
        perguntaEspaco = new JLabel("Aguardando pergunta...", SwingConstants.CENTER);
        this.add(perguntaEspaco, BorderLayout.NORTH);

        // Painel central com opções
        JPanel painelCentral = new JPanel(new BorderLayout(10, 10));
        JPanel painelOpcoes = new JPanel(new GridLayout(2, 2, 10, 10));
        for (int i = 0; i < opcoesBotoes.length; i++) {
            JButton btn = new JButton("Opção " + (i + 1));
            int index = i;
            btn.addActionListener(e -> enviarResposta(index));
            opcoesBotoes[i] = btn;
            painelOpcoes.add(btn);
        }
        painelCentral.add(painelOpcoes, BorderLayout.CENTER);

        // Placar
        JPanel placarPontos = new JPanel(new GridLayout(0, 1, 5, 5));
        placarPontos.setBorder(BorderFactory.createTitledBorder("Pontos"));
        playerScoreLabels = new JLabel[Constants.MAX_CLIENTS];
        for (int i = 0; i < playerScoreLabels.length; i++) {
            playerScoreLabels[i] = new JLabel("Jogador " + (i + 1) + ": 0 pontos");
            placarPontos.add(playerScoreLabels[i]);
        }
        painelCentral.add(placarPontos, BorderLayout.EAST);

        this.add(painelCentral, BorderLayout.CENTER);

        // Mensagem inferior
        JPanel painelInferior = new JPanel(new BorderLayout(10, 10));
        mensagemEspaco = new JLabel("Conectando...", SwingConstants.CENTER);
        painelInferior.add(mensagemEspaco, BorderLayout.CENTER);
        this.add(painelInferior, BorderLayout.SOUTH);

        this.setVisible(true);
    }

    private void enviarResposta(int optionIndex) {
        if (currentQuestion == null) return;

        client.sendMessage(new Records.SendAnswer(
                client.getUsername(),
                optionIndex,
                0 // pode passar o questionNumber se necessário
        ));

        // Desativa os botões até receber a próxima pergunta
        setOptionsEnabled(false);
        mensagemEspaco.setText("Resposta enviada!");
    }

    private void setOptionsEnabled(boolean enabled) {
        for (JButton btn : opcoesBotoes) {
            btn.setEnabled(enabled);
        }
    }
    public void setConnectedPlayers(ArrayList<String> players) {
        playerScoreLabels = new JLabel[players.size()];
        JPanel placarPontos = new JPanel(new GridLayout(players.size(), 1, 5, 5));
        placarPontos.setBorder(BorderFactory.createTitledBorder("Pontos"));

        for (int i = 0; i < players.size(); i++) {
            playerScoreLabels[i] = new JLabel(players.get(i) + ": 0 pontos");
            placarPontos.add(playerScoreLabels[i]);
        }


        this.removeAll(); // ou remover apenas o placar antigo
        this.add(placarPontos, BorderLayout.EAST);
        this.revalidate();
        this.repaint();
    }


    public void mostrarNovaPergunta(Pergunta pergunta, int questionIndex) {
        if (pergunta == null) {
            perguntaEspaco.setText("Fim do jogo ou sem perguntas.");
            setOptionsEnabled(false);
            return;
        }

        currentQuestion = pergunta;
        perguntaEspaco.setText(pergunta.getQuestion());

        String[] opcoes = pergunta.getOptions();
        for (int i = 0; i < opcoesBotoes.length; i++) {
            if (i < opcoes.length) {
                opcoesBotoes[i].setText(opcoes[i]);
                opcoesBotoes[i].setEnabled(true);
            } else {
                opcoesBotoes[i].setEnabled(false);
            }
        }

        mensagemEspaco.setText("Tempo restante: " + Constants.QUESTION_TIME_LIMIT + "s");
    }

    public void atualizarPlacar(HashMap<String, Integer> scores) {
        int i = 0;
        for (String player : scores.keySet()) {
            if (i >= playerScoreLabels.length) break;
            playerScoreLabels[i].setText(player + ": " + scores.get(player) + " pontos");
            i++;
        }
    }

    public void gameEnded(HashMap<String, Integer> finalScores) {
        atualizarPlacar(finalScores);
        perguntaEspaco.setText("Jogo terminado!");
        mensagemEspaco.setText("Pontuações finais enviadas pelo servidor.");
        setOptionsEnabled(false);
    }
}
