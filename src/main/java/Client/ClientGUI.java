package Client;

import Game.Pergunta;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.HashMap;
import java.util.Map;

public class ClientGUI extends JFrame implements ClientListener {

    private JLabel mensagemEspaco;
    private JLabel perguntaEspaco;
    private JLabel tempoEspaco;
    private JPanel placarPontos;
    private JButton[] opcoesBotoes = new JButton[4];
    private final Client client;
    private int tempoAtual;
    private javax.swing.Timer cronometro;
    private final Map<Integer, JLabel> labelsJogadores = new HashMap<>();

    public ClientGUI(Client client) {
        this.client = client;
        client.setListener(this);
        initGUI();
    }

    private void initGUI() {
        this.setTitle("Kahoot Cliente");
        //janela pricipal da Client
        this.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        this.setSize(400, 400);
        this.setLayout(new BorderLayout(10, 10));
        //para ficar com espaçamento de 10 em toda a volta da Client
        getRootPane().setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        //Espaço para colocar a pergunta em questao, na parte superior da Client
        perguntaEspaco = new JLabel("À espera de pergunta...", SwingConstants.CENTER);
        this.add(perguntaEspaco, BorderLayout.NORTH);

        //Painel principal do centro
        JPanel painelCentral = new JPanel(new BorderLayout(10, 10));

        //Painel central do lado esquerdo da Client, com as opcoes da pergunta
        JPanel painelOpcoes = new JPanel(new GridLayout(2, 2, 10, 10));

        for(int i = 0; i < opcoesBotoes.length; i++) {
            final int opcao = i + 1;
            JButton botao = new JButton("Opcao " + opcao);

            botao.addActionListener(new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent e){
                    client.enviarResposta(opcao);
                    bloquearBotoes();
                }
            });
            opcoesBotoes[i] = botao;
            painelOpcoes.add(opcoesBotoes[i]);
        }

        //Adiciona o pinel das opcoes ao painel principal
        painelCentral.add(painelOpcoes, BorderLayout.CENTER);

        //Placar central do lado direito da Client, com as pontuacoes
        placarPontos = new JPanel();
        placarPontos.setLayout(new BoxLayout(placarPontos, BoxLayout.Y_AXIS));
        placarPontos.setBorder(BorderFactory.createTitledBorder("Pontuação"));

        //Adiciona o painel das pontuacoes ao painel principal
        painelCentral.add(placarPontos, BorderLayout.EAST);

        //Adiciona o painel central à Client
        this.add(painelCentral, BorderLayout.CENTER);

        //Adiciona no fim da Client uma zona para colocar um cronometro com o tempo estabelecido pelo servidor
        JPanel painelInferior = new JPanel(new BorderLayout(10, 10));
        tempoEspaco = new JLabel("Tempo: --", SwingConstants.LEFT);
        mensagemEspaco = new JLabel("Mensagem: --", SwingConstants.RIGHT);

        painelInferior.add(tempoEspaco, BorderLayout.WEST);
        painelInferior.add(mensagemEspaco, BorderLayout.EAST);

        //Adiciona o painelInferior à Client
        this.add(painelInferior, BorderLayout.SOUTH);

        this.setVisible(true);
    }

    private void bloquearBotoes() {
        for (JButton botao : opcoesBotoes) {
            botao.setEnabled(false);
        }
    }

//    private void mostrarPergunta(){
//        if(perguntas == null || perguntas.length == 0){
//            perguntaEspaco.setText("Nenhuma pergunta disponivel.");
//            return;
//        }
//        Pergunta pergunta = perguntas[0];
//        perguntaEspaco.setText(pergunta.getQuestao());
//        String[] opcoes = pergunta.getOpcoes();
//        for(int i = 0; i < opcoesBotoes.length; i++){
//            opcoesBotoes[i].setText(opcoes[i]);
//        }
//        mensagemEspaco.setText("Fim das perguntas.");
//        return;
//    }

    @Override
    public void onNewQuestion(String pergunta, String[] opcoes, int numeroPergunta, int tempoLimite, long tempoInicioServidor) {
        SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {
                perguntaEspaco.setText(pergunta);
                for(int i = 0; i < opcoesBotoes.length; i++) {
                    opcoesBotoes[i].setText(opcoes[i]);
                    opcoesBotoes[i].setEnabled(true);
                }
                mensagemEspaco.setText("Pergunta " + numeroPergunta);

                long tempoAtualCliente = System.currentTimeMillis();
                long tempoPassado = (tempoAtualCliente - tempoInicioServidor) / 1000;
                int tempoRestante = tempoLimite - (int) tempoPassado;
                if(tempoRestante < 0){
                    tempoRestante = 0;
                }
                iniciarCronometro(tempoRestante);
            }
        });
    }

    @Override
    public void onEndGame(String mensagem) {
        SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {
                if(cronometro != null){
                    cronometro.stop();
                }

                perguntaEspaco.setText("Jogo terminado.");
                mensagemEspaco.setText(mensagem);
                tempoEspaco.setText("Tempo: --");
                bloquearBotoes();

                JOptionPane.showMessageDialog(ClientGUI.this, mensagemEspaco.getText());
            }
        });
    }

    @Override
    public void onStatistic(Map<Integer, Integer> pontosJogadores) {
        SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {
                for (Map.Entry<Integer, Integer> ponto : pontosJogadores.entrySet()) {
                    int jogadorId = ponto.getKey();
                    int pontos = ponto.getValue();
                    JLabel label = labelsJogadores.get(jogadorId);
                    if (label == null) {
                        label = new JLabel("Jogador " + jogadorId + " : " + pontos + " pontos");
                        labelsJogadores.put(jogadorId, label);
                        placarPontos.add(label);
                    } else {
                        label.setText("Jogador " + jogadorId + " : " + pontos + " pontos");
                    }
                }
                placarPontos.revalidate();
                placarPontos.repaint();
            }
        });
    }

    private void iniciarCronometro(int tempoLimite){
        if(cronometro != null){
            cronometro.stop();
        }

        tempoAtual = tempoLimite;
        tempoEspaco.setText("Tempo: " + tempoAtual + " segundos");

        cronometro = new javax.swing.Timer(1000, new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                tempoAtual--;
                tempoEspaco.setText("Tempo: " + tempoAtual + " segundos");
                if(tempoAtual <= 0){
                    cronometro.stop();
                    tempoEspaco.setText("Tempo esgotado!");
                    bloquearBotoes();
                }
            }
        });
        cronometro.start();
    }


}