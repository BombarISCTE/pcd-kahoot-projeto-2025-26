package GUI;

import javax.swing.*;
import java.awt.*;

public class KahootGUI extends JFrame {

    public KahootGUI() {

        //janela pricipal da GUI
        this.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        this.setSize(400, 400);
        this.setLayout(new BorderLayout(10, 10));
        //para ficar com espaçamento de 10 em toda a volta da GUI
        getRootPane().setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        //Espaço para colocar a pergunta em questao, na parte superior da GUI
        JLabel perguntaEspaco = new JLabel("Pergunta", SwingConstants.CENTER);
        this.add(perguntaEspaco, BorderLayout.NORTH);

        //Painel principal do centro
        JPanel painelCentral = new JPanel(new BorderLayout(10, 10));

        //Painel central do lado esquerdo da GUI, com as opcoes da pergunta
        JPanel painelOpcoes = new JPanel(new GridLayout(2, 2, 10, 10));

        JButton opcao1 = new JButton("Opcao 1");
        JButton opcao2 = new JButton("Opcao 2");
        JButton opcao3 = new JButton("Opcao 3");
        JButton opcao4 = new JButton("Opcao 4");

        painelOpcoes.add(opcao1);
        painelOpcoes.add(opcao2);
        painelOpcoes.add(opcao3);
        painelOpcoes.add(opcao4);

        //Adiciona o pinel das opcoes ao painel principal
        painelCentral.add(painelOpcoes, BorderLayout.CENTER);

        //Placar central do lado direito da GUI, com as pontuacoes
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

        //Adiciona o painel central à GUI
        this.add(painelCentral, BorderLayout.CENTER);

        //Adiciona no fim da GUI uma zona para colocar um cronometro com o tempo estabelecido pelo servidor
        JPanel painelInferior = new JPanel(new BorderLayout(10, 10));
        JLabel tempo = new JLabel("Tempo: --", SwingConstants.LEFT);
        JLabel mensagem = new JLabel("Mensagem: --", SwingConstants.RIGHT);

        painelInferior.add(tempo, BorderLayout.WEST);
        painelInferior.add(mensagem, BorderLayout.EAST);

        //Adiciona o painelInferior à GUI
        this.add(painelInferior, BorderLayout.SOUTH);

        this.setVisible(true);
    }

}
