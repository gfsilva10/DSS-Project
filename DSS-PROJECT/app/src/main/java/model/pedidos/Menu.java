package model.pedidos;

import java.util.ArrayList;
import java.util.List;

import model.gestao.Alimento;

public class Menu extends Produto {
    private List<Item> itens;

    // ====================================================================================================
    // CONSTRUTORES
    // ====================================================================================================
    public Menu(String id, double preco, String nome, double tempoConfecaoEsperado, List<Item> itens) {
        super(id, preco, nome, tempoConfecaoEsperado);
        this.itens = new ArrayList<>();
        if (itens != null) {
        for (Item t : itens) {
            this.itens.add(t);
        }
    }
    }
    // ====================================================================================================
    // GETTERS E SETTERS
    // ====================================================================================================
     public List<Item> getItens() {
        return this.itens;
    }

    public void setItens(List<Item> itens) {
        this.itens = itens;
    }
    // ====================================================================================================
    // MÉTODOS
    // ====================================================================================================
    public void registaTroca(String idAlimentoAtual, Alimento alimentoDesejado) throws PedidoException {
        int count = 0;
        for (Item item : this.itens) {
            try {
                item.registaTroca(idAlimentoAtual, alimentoDesejado);
            } catch (PedidoException e) {
                count++;
            }
        }
        if (count == this.itens.size())
            throw new PedidoException("Erro no regista troca do pedidos/Menu.java");
    }

    // ====================================================================================================
    // TOSTRING CLONE EQUALS
    // ====================================================================================================
    @Override
    public Menu clone() {
        // Clonar cada item profundamente (ESSENCIAL para manter composição isolada)
        List<Item> itensClonados = new ArrayList<>();
        for (Item item : this.itens) {
            itensClonados.add(item.clone());
        }
        return new Menu(super.getId(), super.getPreco(), super.getNome(), super.getTempoConfecaoEsperado(), itensClonados);
    }

}
