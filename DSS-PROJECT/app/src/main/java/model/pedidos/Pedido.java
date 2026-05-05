package model.pedidos;

import java.util.ArrayList;
import java.util.List;

import model.gestao.Alimento;

public class Pedido {
    private static long IdCounter;
    private long idInstance;
    private double tempoConfecaoEsperado;
    private double tempoConfecaoReal; 
    private String nota;
    private List<Produto> produtos;
    private Estado estado;
    private double preco;
    private boolean tipo; // true -> restaurante; false -> takeaway

    // ====================================================================================================
    // CONSTRUTORES
    // ====================================================================================================
    public Pedido() {
        this.idInstance = Pedido.IdCounter++;
        this.tempoConfecaoEsperado = 0;
        this.tempoConfecaoReal = 0;
        this.nota = "";
        this.produtos = new ArrayList<>();
        this.estado = Estado.PorPagar;
        this.preco = 0;
        this.tipo = true;
    }

    public Pedido(List<Produto> produtosSelecionados, String nota, boolean tipo, double preco,
            double tempoConfecaoEsperado) {

        this();
        this.tempoConfecaoEsperado = tempoConfecaoEsperado;
        this.tempoConfecaoReal = this.tempoConfecaoEsperado;
        this.nota = nota;

        for (Produto p : produtosSelecionados) {
            this.produtos.add(p.clone());
            this.preco += p.getPreco();
        }

        this.estado = Estado.PorPagar;
        this.preco = preco;
        this.tipo = tipo;

    }

    // ====================================================================================================
    // GETTERS E SETTERS
    // ====================================================================================================
    public Long getIdCounter() {
        return this.idInstance;
    }

    public static void setIdCounter(long id) {
        Pedido.IdCounter = id;
    }

    public void setId(long id) {
        this.idInstance = id;
    }

    public void setEstado(Estado estado) {
        this.estado = estado;
    }

    public void setNota(String nota) {
        this.nota = nota;
    }

    public void setPreco(double preco) {
        this.preco = preco;
    }

    public void setTempoConfecaoEsperado(double tempoConfecaoEsperado) {
        this.tempoConfecaoEsperado = tempoConfecaoEsperado;
    }

    public void setTempoConfecaoReal(double tempoConfecaoReal) {
        this.tempoConfecaoReal = tempoConfecaoReal;
    }

    public void setTipo(boolean tipo) {
        this.tipo = tipo;
    }

    public void setProdutos(List<Produto> produtos) {
        this.produtos = produtos;
    }

    public Estado getEstado() {
        return this.estado;
    }

    public String getNota() {
        return this.nota;
    }

    public double getPreco() {
        return this.preco;
    }

    public double getTempoConfecaoEsperado() {
        return this.tempoConfecaoEsperado;
    }

    public double getTempoConfecaoReal() {
        return this.tempoConfecaoReal;
    }

    public boolean getTipo() {
        return this.tipo;
    }

    public List<Produto> getProdutos() {
        return this.produtos;
    }

    public void setPedidoEntregue() {
        this.estado = Estado.Entregue;
    }

    // ====================================================================================================
    // MÉTODOS
    // ====================================================================================================
    public void pagamentoConcluido() {
        this.estado = Estado.EmPreparacao;
    }

    /**
     * Realiza uma troca de alimento num produto específico deste pedido
     * Procura o produto dentro deste pedido e faz a troca isoladamente
     */
    public void registaTroca(String idProduto, String idAlimentoAtual, Alimento alimentoDesejado)
            throws PedidoException {
        // Procurar o produto neste pedido específico
        for (Produto produto : this.produtos) {
            if (produto.getId().equals(idProduto)) {
                // Fazer a troca apenas neste produto deste pedido
                produto.registaTroca(idAlimentoAtual, alimentoDesejado);
                return;
            }
        }
        throw new PedidoException("Produto " + idProduto + " não encontrado neste pedido");
    }

    // ====================================================================================================
    // TOSTRING CLONE EQUALS
    // ====================================================================================================

    public Pedido clone() {
        Pedido cloned = new Pedido();
        cloned.idInstance = this.idInstance;
        cloned.estado = this.estado;
        cloned.nota = this.nota;
        cloned.preco = this.preco;
        cloned.tempoConfecaoEsperado = this.tempoConfecaoEsperado;
        cloned.tempoConfecaoReal = this.tempoConfecaoReal;
        cloned.tipo = this.tipo;
        // Deep clone de produtos para garantir composição isolada
        cloned.produtos = new ArrayList<>();
        for (Produto produto : this.produtos) {
            cloned.produtos.add(produto.clone());
        }
        return cloned;
    }

    @Override
    public String toString() {
        return "\nPedido\n{id: " + idInstance +
                ",\n estado: " + estado +
                ",\n nota: " + nota +
                ",\n preço: " + preco +
                ",\n tempoConfecaoEsperado: " + tempoConfecaoEsperado +
                ",\n tempoConfecaoReal: " + tempoConfecaoReal +
                ", \n tipo: " + (this.tipo ? "Restaurante" : "TakeAway") +
                ",\n produtos: " + produtos.toString() + "\n";
    }

}
