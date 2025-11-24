package Client;

import Game.Pergunta;

import javax.swing.*;
import java.awt.*;

public class ClientGUI extends JFrame {

    private JLabel mensagemEspaco;
    private JLabel perguntaEspaco;
    private JButton[] opcoesBotoes = new JButton[4];
    private Pergunta[] perguntas;

    public ClientGUI(Pergunta[] perguntas) {
        this.perguntas = perguntas;

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
            painelOpcoes.add(opcoesBotoes[i]);
        }

        //Adiciona o pinel das opcoes ao painel principal
        painelCentral.add(painelOpcoes, BorderLayout.CENTER);

        //Placar central do lado direito da Client, com as pontuacoes
        JPanel placarPontos = new JPanel(new GridLayout(3, 2, 10, 10));

        placarPontos.setBorder(BorderFactory.createTitledBorder("Pontos"));

        JLabel equipaALabel = new JLabel("Equipa A", SwingConstants.CENTER);
        JLabel equipaBLabel = new JLabel("Equipa B", SwingConstants.CENTER);

        JLabel jogadorA1 = new JLabel("A1 : 0 pontos", SwingConstants.CENTER);
        JLabel jogadorB1 = new JLabel("B1 : 0 pontos", SwingConstants.CENTER);
        JLabel jogadorA2 = new JLabel("A2 : 0 pontos", SwingConstants.CENTER);
        JLabel jogadorB2 = new JLabel("B2 : 0 pontos", SwingConstants.CENTER);

        //1ºlinha
        placarPontos.add(equipaALabel);
        placarPontos.add(equipaBLabel);
        //2ºlinha
        placarPontos.add(jogadorA1);
        placarPontos.add(jogadorB1);
        //3ºlinha
        placarPontos.add(jogadorA2);
        placarPontos.add(jogadorB2);

        //Adiciona o painel das pontuacoes ao painel principal
        painelCentral.add(placarPontos, BorderLayout.EAST);

        //Adiciona o painel central à Client
        this.add(painelCentral, BorderLayout.CENTER);

        //Adiciona no fim da Client uma zona para colocar um cronometro com o tempo estabelecido pelo servidor
        JPanel painelInferior = new JPanel(new BorderLayout(10, 10));
        JLabel tempo = new JLabel("Tempo: --", SwingConstants.LEFT);
        mensagemEspaco = new JLabel("Utils.Mensagem: --", SwingConstants.RIGHT);

        painelInferior.add(tempo, BorderLayout.WEST);
        painelInferior.add(mensagemEspaco, BorderLayout.EAST);

        //Adiciona o painelInferior à Client
        this.add(painelInferior, BorderLayout.SOUTH);

        mostrarPergunta();

        this.setVisible(true);
    }

    private void mostrarPergunta(){
        if(perguntas == null || perguntas.length == 0){
            perguntaEspaco.setText("Nenhuma pergunta disponivel.");
            return;
        }
        Pergunta pergunta = perguntas[0];
        perguntaEspaco.setText(pergunta.getQuestao());
        String[] opcoes = pergunta.getOpcoes();
        for(int i = 0; i < opcoesBotoes.length; i++){
            opcoesBotoes[i].setText(opcoes[i]);
        }
        mensagemEspaco.setText("Fim das perguntas.");
        return;
    }
}