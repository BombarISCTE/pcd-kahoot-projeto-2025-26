package Game;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public class DealWithQuestions {

    private final GameState gameState;

    public DealWithQuestions(GameState gameState) {
        this.gameState = gameState;
    }

    public boolean perguntaRespondidaPorTodos(){
        int totalJogadores = gameState.getNumEquipas() * gameState.getNumJogadoresEquipa();
        if(gameState.getRespostasRecebidas() >= totalJogadores){
            return true;
        }
        return false;
    }

    public void carregarPerguntas(Pergunta[] lista){
        Pergunta[] perguntas  = new Pergunta[gameState.getNumPerguntas()];

        List<Pergunta> listaPerguntas = new ArrayList<>(Arrays.asList(lista));
        Collections.shuffle(listaPerguntas);

        for(int i = 0; i < gameState.getNumPerguntas(); i++){
            perguntas[i] = listaPerguntas.get(i);
        }

        gameState.setPerguntas(perguntas);
    }

    public boolean avancarProximaPergunta(){
        gameState.incrementarIndicePerguntaAtual();
        if(gameState.acabouJogo()){
            return false;
        }
        gameState.reporRespostasRecebidas();
        gameState.reporOrdemRespostas();
        gameState.reporRespostasEquipa();
        return true;
    }
}
