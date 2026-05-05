package model.pedidos;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import data.PedidoDAO;
import data.ProdutoDAO;
import model.gestao.Alimento;

public class PedidosFacade implements InterPedidoL {

    private Map<String, Produto> produtos;
    private Map<Long, Pedido> pedidos;
    private List<String> pedidoEmConstrucao;
    private String notaEmConstrucao;
    private Boolean tipoEmConstrucao;

    // ====================================================================================================
    // CONSTRUTORES
    // ====================================================================================================
    public PedidosFacade() {
        this.pedidos = PedidoDAO.getInstance();
        this.produtos = ProdutoDAO.getInstance();
        this.pedidoEmConstrucao = new ArrayList<>();
        this.notaEmConstrucao = "";
        this.tipoEmConstrucao = null;
    }

    // ====================================================================================================
    // GETTERS E SETTERS
    // ====================================================================================================
    @Override
    public List<Produto> getProdutosPedido(long idPedido) {
        Pedido pedido = this.pedidos.get(idPedido);
        if (pedido != null) {
            return pedido.getProdutos();
        }
        return new ArrayList<>();
    }

    @Override
    public Map<String, Alimento> getAlimentosItem(long idPedido, String idProduto) throws PedidoException {
        Pedido pedido = this.pedidos.get(idPedido);
        if (pedido == null) {
            throw new PedidoException("Pedido " + idPedido + " não encontrado");
        }

        // Procurar o produto no pedido
        for (Produto produto : pedido.getProdutos()) {
            if (produto.getId().equals(idProduto) && produto instanceof Item) {
                Item item = (Item) produto;
                return item.getAlimentos();
            }
        }
        throw new PedidoException("Produto " + idProduto + " não encontrado ou não é um Item");
    }

    @Override
    public List<String> getSubstitutosDisponiveis(long idPedido, String idProduto, String idAlimentoAtual)
            throws PedidoException {
        Pedido pedido = this.pedidos.get(idPedido);
        if (pedido == null) {
            throw new PedidoException("Pedido " + idPedido + " não encontrado");
        }

        // Procurar o produto no pedido
        for (Produto produto : pedido.getProdutos()) {
            if (produto.getId().equals(idProduto) && produto instanceof Item) {
                Item item = (Item) produto;
                Map<String, List<String>> trocas = item.getTrocas();
                if (trocas.containsKey(idAlimentoAtual)) {
                    return trocas.get(idAlimentoAtual);
                }
                throw new PedidoException("Não existem trocas disponíveis para o alimento " + idAlimentoAtual);
            }
        }
        throw new PedidoException("Produto " + idProduto + " não encontrado ou não é um Item");
    }

    @Override
    public List<Pedido> getPedidosPorPagar() {
        return this.getPedidosPorEstado(Estado.PorPagar);
    }

    @Override
    public List<Pedido> getPedidosEmPreparacao() {
        return this.getPedidosPorEstado(Estado.EmPreparacao);
    }

    @Override
    public List<Pedido> getPedidosConcluidos() {
        return this.getPedidosPorEstado(Estado.Concluido);
    }

    @Override
    public List<Long> getPedidosConcluidosIds() {
        List<Long> ids = new ArrayList<>();
        for (Pedido p : this.pedidos.values()) {
            if (p.getEstado() == Estado.Concluido) {
                ids.add(p.getIdCounter());
            }
        }
        return ids;
    }

    private List<Pedido> getPedidosPorEstado(Estado estado) {
        List<Pedido> clones = new ArrayList<>();
        for (Pedido p : this.pedidos.values()) {
            if (p.getEstado() == estado) {
                clones.add(p.clone());
            }
        }
        return clones;
    }

    // ====================================================================================================
    // MÉTODOS
    // ====================================================================================================
    @Override
    public long registaPedido(List<String> codigoProdutos, String nota, boolean tipo) {
        List<Produto> produtosSelecionados = new ArrayList<>();

        double precoTotal = 0;
        double tempoTotal = 0;

        for (String codigo : codigoProdutos) {
            try {
                Produto produto = this.produtos.get(codigo);
                if (produto != null) {
                    produtosSelecionados.add(produto);
                    precoTotal += produto.getPreco();
                    tempoTotal += produto.getTempoConfecaoEsperado();
                }
            } catch (Exception e) {
                // Ignorar produtos não encontrados
            }
        }

        Pedido pedido = new Pedido(produtosSelecionados, nota, tipo, precoTotal, tempoTotal);

        this.pedidos.put(pedido.getIdCounter(), pedido);

        return pedido.getIdCounter();
    }

    @Override
    public void iniciarPedido(boolean tipoRestaurante) {
        this.tipoEmConstrucao = tipoRestaurante;
        this.pedidoEmConstrucao.clear();
        this.notaEmConstrucao = "";
    }

    @Override
    public void adicionarProdutoPedido(String produtoId) {
        if (this.produtos.containsKey(produtoId)) {
            this.pedidoEmConstrucao.add(produtoId);
        }
    }

    @Override
    public void definirNotaPedido(String nota) {
        this.notaEmConstrucao = nota != null ? nota : "";
    }

    @Override
    public List<Produto> getProdutosPedidoEmConstrucao() {
        List<Produto> selecionados = new ArrayList<>();
        for (String id : this.pedidoEmConstrucao) {
            Produto p = this.produtos.get(id);
            if (p != null) {
                selecionados.add(p.clone());
            }
        }
        return selecionados;
    }

    @Override
    public String getNotaPedidoEmConstrucao() {
        return this.notaEmConstrucao;
    }

    @Override
    public List<String> getIdsPedidoEmConstrucao() {
        return new ArrayList<>(this.pedidoEmConstrucao);
    }

    @Override
    public long confirmarPedidoEmConstrucao() throws PedidoException {
        if (this.tipoEmConstrucao == null) {
            throw new PedidoException("Pedido não iniciado.");
        }
        if (this.pedidoEmConstrucao.isEmpty()) {
            throw new PedidoException("Pedido sem produtos.");
        }
        long id = registaPedido(new ArrayList<>(this.pedidoEmConstrucao), this.notaEmConstrucao,
                this.tipoEmConstrucao);
        limparPedidoEmConstrucao();
        return id;
    }

    @Override
    public void cancelarPedidoEmConstrucao() {
        limparPedidoEmConstrucao();
    }

    private void limparPedidoEmConstrucao() {
        this.pedidoEmConstrucao.clear();
        this.notaEmConstrucao = "";
        this.tipoEmConstrucao = null;
    }

    @Override
    public List<Produto> getMenusDisponiveis() {
        List<Produto> menusDisponiveis = new ArrayList<>();
        for (Produto p : this.produtos.values()) {
            if (p instanceof Menu) {
                menusDisponiveis.add(p.clone());
            }
        }
        return menusDisponiveis;
    }

    @Override
    public List<Produto> getItensDisponiveis() {
        List<Produto> itensDisponiveis = new ArrayList<>();
        for (Produto p : this.produtos.values()) {
            if (p instanceof Item) {
                itensDisponiveis.add(p.clone());
            }
        }
        return itensDisponiveis;
    }

    @Override
    public List<String> getMenusIds() {
        List<String> ids = new ArrayList<>();
        for (Produto p : this.produtos.values()) {
            if (p instanceof Menu) {
                ids.add(p.getId());
            }
        }
        return ids;
    }

    @Override
    public List<String> getItensIds() {
        List<String> ids = new ArrayList<>();
        for (Produto p : this.produtos.values()) {
            if (p instanceof Item) {
                ids.add(p.getId());
            }
        }
        return ids;
    }

    @Override
    public String getNomeProduto(String idProduto) {
        Produto p = this.produtos.get(idProduto);
        return p != null ? p.getNome() : idProduto;
    }

    @Override
    public boolean registaTroca(long idPedido, String idProduto, String idAlimentoAtual, Alimento alimentoDesejado)
            throws PedidoException {
        Pedido pedido = this.pedidos.get(idPedido);
        if (pedido != null) {
            // Delegar a troca para o pedido (composição)
            pedido.registaTroca(idProduto, idAlimentoAtual, alimentoDesejado);
            // Persistir as alterações no pedido
            this.pedidos.put(idPedido, pedido);
            return true;
        }
        throw new PedidoException("Pedido " + idPedido + " não encontrado");
    }

    @Override
    public void validaPagamento(long idPedido) {
        Pedido pedido = this.pedidos.get(idPedido);

        pedido.pagamentoConcluido();
        this.pedidos.put(idPedido, pedido);
    }

    @Override
    public void entregarPedido(long idPedido) {
        Pedido pedido = this.pedidos.get(idPedido);

        if (pedido == null) {
            return;
        }

        pedido.setEstado(Estado.Entregue);
        this.pedidos.put(idPedido, pedido);
    }

    @Override
    public String geraFatura(long idPedido) {
        Pedido pedido = this.pedidos.get(idPedido);
        if (pedido == null) {
            return "";
        }

        StringBuilder fatura = new StringBuilder();
        fatura.append("\n========== FATURA ==========\n");
        fatura.append("Pedido #").append(pedido.getIdCounter()).append("\n");
        fatura.append("Tipo: ").append(pedido.getTipo() ? "Restaurante" : "Take Away").append("\n");
        fatura.append("---\n");

        for (Produto produto : pedido.getProdutos()) {
            fatura.append("• ").append(produto.getNome())
                    .append(" - €").append(String.format("%.2f", produto.getPreco())).append("\n");
        }

        fatura.append("---\n");
        fatura.append("Total: €").append(String.format("%.2f", pedido.getPreco())).append("\n");
        fatura.append("Tempo estimado: ").append((int) pedido.getTempoConfecaoEsperado()).append(" min\n");

        if (pedido.getNota() != null && !pedido.getNota().isEmpty()) {
            fatura.append("Nota: ").append(pedido.getNota()).append("\n");
        }

        fatura.append("===========================\n");
        return fatura.toString();
    }

    // ====================================================================================================
    // TOSTRING CLONE EQUALS
    // ====================================================================================================
}
