package Client;

import Messages.Answer;
import Messages.SendQuestion;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Map;

public class ClientGUI extends JFrame  {

    private JLabel mensagemEspaco;
    private JLabel perguntaEspaco;
    private JLabel tempoEspaco;
    private JPanel placarPontos;

    private JButton[] opcoesBotoes = new JButton[4];

    private final Client client;

    private javax.swing.Timer cronometro;

    public ClientGUI(Client client) {
        this.client = client;
        initGUI();
    }

    private void initGUI() {
//        this.perguntas = perguntas;

        //janela pricipal da Client
        this.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        this.setSize(400, 400);
        this.setLayout(new BorderLayout(10, 10));
        //para ficar com espaçamento de 10 em toda a volta da Client
        getRootPane().setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        //Espaço para colocar a pergunta em questao, na parte superior da Client
        perguntaEspaco = new JLabel("", SwingConstants.CENTER);
        this.add(perguntaEspaco, BorderLayout.NORTH);

        //Painel principal do centro
        JPanel painelCentral = new JPanel(new BorderLayout(10, 10));

        //Painel central do lado esquerdo da Client, com as opcoes da pergunta
        JPanel painelOpcoes = new JPanel(new GridLayout(2, 2, 10, 10));

        for(int i = 0; i < opcoesBotoes.length; i++) {
            opcoesBotoes[i] = new JButton("Opcao " + (i + 1));
            opcoesBotoes[i].setEnabled(false);
            painelOpcoes.add(opcoesBotoes[i]);
        }

        //Adiciona o pinel das opcoes ao painel principal
        painelCentral.add(painelOpcoes, BorderLayout.CENTER);

        //Placar central do lado direito da Client, com as pontuacoes
        placarPontos = new JPanel();
        placarPontos.setLayout(new BoxLayout(placarPontos, BoxLayout.Y_AXIS));
        placarPontos.setBorder(BorderFactory.createTitledBorder("Pontuações"));

        placarPontos.add(new JLabel("A aguardar estatísticas..."));

        //Adiciona o painel das pontuacoes ao painel principal
        painelCentral.add(placarPontos, BorderLayout.EAST);

        //Adiciona o painel central à Client
        this.add(painelCentral, BorderLayout.CENTER);

        //Adiciona no fim da Client uma zona para colocar um cronometro com o tempo estabelecido pelo servidor
        JPanel painelInferior = new JPanel(new BorderLayout(10, 10));
        tempoEspaco = new JLabel("Tempo: --", SwingConstants.LEFT);
        mensagemEspaco = new JLabel("Utils.Mensagem: --", SwingConstants.RIGHT);

        painelInferior.add(tempoEspaco, BorderLayout.WEST);
        painelInferior.add(mensagemEspaco, BorderLayout.EAST);

        //Adiciona o painelInferior à Client
        this.add(painelInferior, BorderLayout.SOUTH);

        this.setVisible(true);
    }

    public void mostrarPergunta(SendQuestion envioPergunta) {
        SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {
                perguntaEspaco.setText("Pergunta #" + envioPergunta.getQuestionNumber() + ": " + envioPergunta.getQuestion());

                for (int i = 0; i < opcoesBotoes.length; i++) {
                    ActionListener[] listeners = opcoesBotoes[i].getActionListeners();
                    for (int j = 0; j < listeners.length; j++) {
                        opcoesBotoes[i].removeActionListener(listeners[j]);
                    }
                }

                String[] opcoes = envioPergunta.getOptions();

                for (int i = 0; i < opcoesBotoes.length; i++) {
                    opcoesBotoes[i].setText(opcoes[i]);
                    opcoesBotoes[i].setEnabled(true);
                    final int opcaoEscolhida = i + 1;
                    opcoesBotoes[i].addActionListener(new ActionListener() {
                        @Override
                        public void actionPerformed(ActionEvent e) {
                            bloquearBotoes();
                            client.sendMessage(new Answer(client.getUsername(), client.getTeamId(), opcaoEscolhida));
                        }
                    });
                }
                mensagemEspaco.setText("Escolhe uma opção!");

                iniciarCronometro(envioPergunta.getTimeLimit());
            }
        });
    }

    private void iniciarCronometro(final int tempoLimite) {
        if (cronometro != null) {
            cronometro.stop();
        }

        cronometro = new javax.swing.Timer(1000, new ActionListener() {
            int tempoRestante = tempoLimite;

            @Override
            public void actionPerformed(ActionEvent e) {

                if (tempoRestante <= 0) {
                    tempoEspaco.setText("Tempo esgotado!");
                    bloquearBotoes();
                    cronometro.stop();
                } else {
                    tempoEspaco.setText("Tempo: " + tempoRestante + " segundos");
                    tempoRestante--;
                }
            }
        });

        cronometro.start();
    }

    public void atualizarPlacar(final Map<Integer, Integer> pontosJogadores) {
        SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {
                placarPontos.removeAll();
                for (Map.Entry<Integer, Integer> entry : pontosJogadores.entrySet()) {
                    int jogadorId = entry.getKey();
                    int pontos = entry.getValue();
                    JLabel linha = new JLabel("Jogador " + jogadorId + " : " + pontos + " pontos");
                    placarPontos.add(linha);
                }
                placarPontos.revalidate();
                placarPontos.repaint();
            }
        });
    }


    private void bloquearBotoes() {
        for (int i = 0; i < opcoesBotoes.length; i++) {
            opcoesBotoes[i].setEnabled(false);
        }
    }

}