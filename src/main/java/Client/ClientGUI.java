package Client;

import Utils.Records.*;

import javax.swing.*;
import java.awt.*;
import java.util.ArrayList;
import java.util.HashMap;

public class ClientGUI extends JFrame {

    JLabel setMensagemEspaco;
    private JLabel perguntaEspaco;
    private JButton[] opcoesBotoes = new JButton[4];
    private JLabel[] playerScoreLabels;
    private final Client client;
    private ArrayList<Utils.Records.PlayerInfo> playerInfos = new ArrayList<>();

    private int currentQuestionNumber = -1;
    private Timer countdownTimer;
    private int tempoRestante;

    public ClientGUI(Client client) {
        this.client = client;
        System.out.println("ClientGUI - creating GUI for client: " + client.getUsername());
        initGUI();
    }

    private void initGUI() {
        this.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        this.setSize(600, 400);
        this.setLayout(new BorderLayout(10, 10));

        // Pergunta
        perguntaEspaco = new JLabel("Aguardando pergunta...", SwingConstants.CENTER);
        this.add(perguntaEspaco, BorderLayout.NORTH);

        // Painel central com opções
        JPanel painelCentral = new JPanel(new BorderLayout(10, 10));
        JPanel painelOpcoes = new JPanel(new GridLayout(2, 2, 5, 5));
        for (int i = 0; i < opcoesBotoes.length; i++) {
            JButton btn = new JButton("Opção " + (i + 1));
            int index = i;
            btn.addActionListener(e -> enviarResposta(index));
            opcoesBotoes[i] = btn;
            painelOpcoes.add(btn);
        }
        painelCentral.add(painelOpcoes, BorderLayout.CENTER);

        // Painel de placar inicial vazio
        JPanel placarPontos = new JPanel(new GridLayout(0, 1, 5, 5));
        placarPontos.setBorder(BorderFactory.createTitledBorder("Pontos"));
        this.add(painelCentral, BorderLayout.CENTER);
        this.add(placarPontos, BorderLayout.EAST);

        // Mensagem inferior
        setMensagemEspaco = new JLabel("Conectando...", SwingConstants.CENTER);
        this.add(setMensagemEspaco, BorderLayout.SOUTH);

        this.setVisible(true);
    }

    private void enviarResposta(int optionIndex) {
        if (currentQuestionNumber < 0) return;

        client.sendMessage(new SendAnswer(
                client.getUsername(),
                optionIndex,
                currentQuestionNumber
        ));

        setOptionsEnabled(false);
        setMensagemEspaco.setText("Resposta enviada!");
        if (countdownTimer != null) countdownTimer.stop();
    }

    void setOptionsEnabled(boolean enabled) {
        for (JButton btn : opcoesBotoes) {
            btn.setEnabled(enabled);
        }
    }

    // Atualiza lista de jogadores e cria labels a partir dos nomes (strings)
    public void setConnectedPlayers(ArrayList<Utils.Records.PlayerInfo> players) {
        this.playerInfos = new ArrayList<>(players);
        rebuildPlayerList();
    }

    public void addPlayer(Utils.Records.PlayerInfo p) {
        this.playerInfos.add(p);
        rebuildPlayerList();
    }

    private void rebuildPlayerList() {
        JPanel placarPontos = new JPanel(new GridLayout(playerInfos.size(), 1, 5, 5));
        placarPontos.setBorder(BorderFactory.createTitledBorder("Pontos"));

        playerScoreLabels = new JLabel[playerInfos.size()];
        for (int i = 0; i < playerInfos.size(); i++) {
            Utils.Records.PlayerInfo pi = playerInfos.get(i);
            playerScoreLabels[i] = new JLabel(pi.name() + " (T" + pi.teamId() + "): " + pi.score() + " pontos");
            placarPontos.add(playerScoreLabels[i]);
        }

        this.add(placarPontos, BorderLayout.EAST);
        this.revalidate();
        this.repaint();
    }

    public void mostrarNovaPergunta(SendTeamQuestion question) {
        System.out.println("ClientGUI - mostrarNovaPergunta (team) questionNumber=" + question.questionNumber());
        mostrarPerguntaGenerica(question.question(), question.options(), question.questionNumber(), question.timeLimit());
    }

    public void mostrarNovaPergunta(SendIndividualQuestion question) {
        System.out.println("ClientGUI - mostrarNovaPergunta (individual) questionNumber=" + question.questionNumber());
        mostrarPerguntaGenerica(question.question(), question.options(), question.questionNumber(), question.timeLimit());
    }

    private void mostrarPerguntaGenerica(String pergunta, String[] opcoes, int questionNumber, int timeLimit) {
        currentQuestionNumber = questionNumber;
        perguntaEspaco.setText(pergunta);

        for (int i = 0; i < opcoesBotoes.length; i++) {
            if (i < opcoes.length) {
                opcoesBotoes[i].setText(opcoes[i]);
                opcoesBotoes[i].setEnabled(true);
            } else {
                opcoesBotoes[i].setEnabled(false);
            }
        }

        tempoRestante = timeLimit;
        setMensagemEspaco.setText("Tempo restante: " + tempoRestante + "s");

        if (countdownTimer != null) countdownTimer.stop();
        countdownTimer = new Timer(1000, e -> {
            tempoRestante--;
            setMensagemEspaco.setText("Tempo restante: " + tempoRestante + "s");
            if (tempoRestante <= 0) {
                ((Timer) e.getSource()).stop();
                setOptionsEnabled(false);
                setMensagemEspaco.setText("Tempo esgotado!");
            }
        });
        countdownTimer.start();
    }

    public void atualizarPlacar(HashMap<String, Integer> scores) {
        if (playerScoreLabels == null) return;
        for (int i = 0; i < playerScoreLabels.length; i++) {
            String playerName = playerScoreLabels[i].getText().split(":")[0];
            if (scores.containsKey(playerName)) {
                playerScoreLabels[i].setText(playerName + ": " + scores.get(playerName) + " pontos");
            }
        }
    }

    public void gameEnded(HashMap<String, Integer> finalScores) {
        atualizarPlacar(finalScores);
        perguntaEspaco.setText("Jogo terminado!");
        setMensagemEspaco.setText("Pontuações finais enviadas pelo servidor.");
        setOptionsEnabled(false);
    }

    public void setSetMensagemEspaco(String msg) {
        setMensagemEspaco.setText(msg);}
}
