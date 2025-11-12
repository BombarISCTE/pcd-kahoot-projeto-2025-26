package Server;

import java.util.HashSet;
import java.util.Random;
import java.util.Set;
import java.util.concurrent.ThreadLocalRandom;

//2.2 Códigos do jogo
//Servidor deve ser capaz de gerar um código único que não esteja em utilização.
//O servidor deve conseguir armazenar os códigos e descartá-los quando
//não estão mais em utilização.
public class GeradorCodigo {

    private static final Set<String> codigosUsados = new HashSet<String>();

    public static synchronized String gerarCodigo(){
        String codigo;
        while(true){
            codigo = String.format("%06d", ThreadLocalRandom.current().nextInt(1000000));
            if(!codigosUsados.contains(codigo)){
                codigosUsados.add(codigo);
                return codigo;
            }
        }
    }

    public static synchronized void apagarCodigo(String codigo) {
        codigosUsados.remove(codigo);
    }

}
