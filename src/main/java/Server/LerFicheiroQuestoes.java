package Server;

import java.io.FileReader;
import java.io.IOException;
import com.google.gson.Gson;

public class LerFicheiroQuestoes {

    public static Questao[] lerQueestoes(String caminhoFicheiro) throws IOException {

        Gson gson = new Gson();

        try(FileReader leitor = new FileReader(caminhoFicheiro)) {
            Questao[] questoes = gson.fromJson(leitor, Questao[].class);
            return questoes;
        }

    }

}
