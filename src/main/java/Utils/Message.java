package Utils;

import java.io.Serializable;

public class Message implements Serializable {
    private final MessageType tipo;
    private final Object dados;

    public Message(MessageType tipo, Object dados) {
        this.tipo = tipo;
        this.dados = dados;
    }

    public MessageType getTipo() {
        return tipo;
    }

    public Object getDados() {
        return dados;
    }

    @Override
    public String toString(){
        return "Mensagem{" + "tipo= " + tipo + ", dados= " + dados + "}";
    }


}
