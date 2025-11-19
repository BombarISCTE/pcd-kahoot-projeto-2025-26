package Utils;

import java.util.HashSet;
import java.util.Random;
import java.util.Set;

//2.2 Códigos do jogo
//Servidor deve ser capaz de gerar um código único que não esteja em utilização.
//O servidor deve conseguir armazenar os códigos e descartá-los quando
//não estão mais em utilização.
public class IdCodeGenerator {

    private static final Random random = new Random();
    private static final Set<String> codigosUsados = new HashSet<String>();

    public static synchronized String gerarCodigo(){
        String codigo;
        while(true){
            codigo = String.format("%06d", random.nextInt(1000000));
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
