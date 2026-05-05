package model.pedidos;

import java.util.List;
import java.util.Map;

import model.gestao.Alimento;

public interface InterPedidoL {

        long registaPedido(List<String> codigoProdutos, String nota, boolean tipo);

        void validaPagamento(long idPedido);

        boolean registaTroca(long idPedido, String idProduto, String idAlimentoAtual, Alimento alimentoDesejado)
                        throws PedidoException;

        List<Produto> getProdutosPedido(long idPedido);

        Map<String, Alimento> getAlimentosItem(long idPedido, String idProduto) throws PedidoException;

        List<String> getSubstitutosDisponiveis(long idPedido, String idProduto, String idAlimentoAtual)
                        throws PedidoException;

        void entregarPedido(long idPedido);

        String geraFatura(long idPedido);

        List<Pedido> getPedidosPorPagar();

        List<Pedido> getPedidosEmPreparacao();

    List<Pedido> getPedidosConcluidos();

    List<Long> getPedidosConcluidosIds();

    void iniciarPedido(boolean tipoRestaurante);

    void adicionarProdutoPedido(String produtoId);

    void definirNotaPedido(String nota);

    List<Produto> getProdutosPedidoEmConstrucao();

    String getNotaPedidoEmConstrucao();

    long confirmarPedidoEmConstrucao() throws PedidoException;

    List<String> getIdsPedidoEmConstrucao();

    void cancelarPedidoEmConstrucao();

    List<Produto> getMenusDisponiveis();

    List<Produto> getItensDisponiveis();

    List<String> getMenusIds();

    List<String> getItensIds();

    String getNomeProduto(String idProduto);
}
