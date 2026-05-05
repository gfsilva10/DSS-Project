package model.pedidos;

public class PedidoException extends Exception{
    public PedidoException(){
        super();
    }

    public PedidoException(String mensagem) {
        super(mensagem);
    }
}
