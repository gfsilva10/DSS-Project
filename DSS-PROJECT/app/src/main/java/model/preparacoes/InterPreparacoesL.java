package model.preparacoes;

import java.util.List;

import model.preparacoes.TipoPosto;

public interface InterPreparacoesL {

    void encerrarPedido(long idPedido, String postoId);

    void removerPedidoFila(long idPedido, List<Long> filaPedidos);

    void adicionaListaPedidos(long idPedido);

    void requisitarIngredientes(long idPedido, String idPosto);

    void atrasarPedido(long idPedido, double tempoAtraso);

    void atualizaFilaPedidos(long idPedido, List<Long> filaPedidos);

    List<Long> getFilaPedidos();

    boolean postoExiste(String postoId);

    boolean ocuparPosto(String postoId, long funcionarioId);

    void libertarPostoDeFuncionario(long funcionarioId);

    TipoPosto getTipoPosto(String postoId);

    String getPostoDeFuncionario(long funcionarioId);

    boolean ingredientesSuficientes(long idPedido, String postoId);

    boolean requisitarAlimento(String alimentoId, String postoId, int quantidade);
}
