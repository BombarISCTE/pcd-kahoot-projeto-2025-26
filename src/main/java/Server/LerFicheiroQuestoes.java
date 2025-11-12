package Server;

import java.io.FileReader;
import java.io.IOException;
import com.google.gson.Gson;

public class LerFicheiroQuestoes {

    public static Pergunta[] lerQueestoes(String caminhoFicheiro) throws IOException {

        Gson gson = new Gson();

        try(FileReader leitor = new FileReader(caminhoFicheiro)) {
            Pergunta[] questoes = gson.fromJson(leitor, Pergunta[].class);
            return questoes;
        }

    }

}
