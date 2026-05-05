package model;

import java.util.List;
import java.util.Map;

import model.gestao.Alimento;
import model.pedidos.Pedido;
import model.pedidos.PedidoException;
import model.pedidos.Produto;
import model.preparacoes.TipoPosto;

public interface InterRestauranteL {

        public long registaPedido(List<String> codigoProdutos, String nota, boolean tipo);

        public void validaPagamento(long idPedido);

        public boolean registaTroca(long idPedido, String idProduto, String idAlimentoAtual, String idAlimentoDesejado)
                        throws PedidoException;

        public double apresentaTempoConfecao();

        public Map<String, Integer> apresentaStock();

        public void enviaMensagem(String mensagem);

        public List<String> getMensagens();

        public void entregarPedido(long idPedido);

        public String geraFatura(long idPedido);

        public Alimento getAlimento(String idAlimento);

        public boolean registaTrocaEmItemDoMenu(long idPedido, String idItemNoMenu, String idAlimentoAtual,
                        String idAlimentoDesejado) throws PedidoException;

        public void encerrarPedido(long idPedido, String postoId);

        public void removerPedidoFila(long idPedido, List<Long> filaPedidos);

        public void requisitarIngredientes(long idPedido, String idPosto);

        public void atrasarPedido(long idPedido, double tempoAtraso);

        public void atualizaFilaPedidos(long idPedido, List<Long> filaPedidos);

        public List<Pedido> getPedidosPorPagar();

        public List<Pedido> getPedidosEmPreparacao();

    public List<Pedido> getPedidosConcluidos();

    public List<Long> getPedidosConcluidosIds();

        public List<Long> getFilaPedidos();

        public List<String> getSubstitutosDisponiveis(long idPedido, String idProduto, String idAlimentoAtual)
                        throws PedidoException;

        public List<Produto> getProdutosPedido(long idPedido);

        public Map<String, Alimento> getAlimentosItem(long idPedido, String idProduto) throws PedidoException;

    public boolean autenticaFuncionario(long id, String password);

    public boolean funcionarioEAdmin(long id);

    public boolean postoExiste(String postoId);

    public List<String> getPostosLivres();

    public boolean ocuparPosto(String postoId, long funcionarioId);

    public void libertarPostoDeFuncionario(long funcionarioId);

    public void registaFuncionario(long id, String nome, String password, boolean admin);

    public TipoPosto getTipoPosto(String postoId);

    public String getPostoDeFuncionario(long funcionarioId);

    public boolean ingredientesSuficientes(long idPedido, String postoId);

    public void iniciarPedido(boolean tipoRestaurante);

    public void adicionarProdutoPedido(String produtoId);

    public void definirNotaPedido(String nota);

    public List<Produto> getProdutosPedidoEmConstrucao();

    public String getNotaPedidoEmConstrucao();

    public long confirmarPedidoEmConstrucao() throws PedidoException;

    public List<String> getIdsPedidoEmConstrucao();

    public void cancelarPedidoEmConstrucao();

    public List<Produto> getMenusDisponiveis();

    public List<Produto> getItensDisponiveis();

    public List<String> getMenusIds();

    public List<String> getItensIds();

    public String getNomeProduto(String idProduto);

    public boolean requisitarAlimento(String alimentoId, String postoId, int quantidade);

}
