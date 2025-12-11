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
        gameState.avancarParaProximaPergunta();

        if(gameState.acabouJogo()){
            return false;
        }
        return true;
    }
}
