package model.pedidos;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import model.gestao.Alimento;

public class Item extends Produto {
    private Map<String, Alimento> alimentos;
    private Map<String, List<String>> trocas; // <idAlimento que tenho, id alimento que posso trocar>

    // ====================================================================================================
    // CONSTRUTORES
    // ====================================================================================================
    public Item() {
        super();
        this.alimentos = new HashMap<>();
        this.trocas = new HashMap<>();
    }

    public Item(String id, double preco, String nome, double tempoConfecaoEsperado, Map<String, Alimento> alimentos,
            Map<String, List<String>> trocas) {

        super(id, preco, nome, tempoConfecaoEsperado);

        // Copiar alimentos um a um
        this.alimentos = new HashMap<>();
        for (Map.Entry<String, Alimento> entry : alimentos.entrySet()) {
            this.alimentos.put(entry.getKey(), entry.getValue().clone());
        }

        // Copiar trocas um a um
        this.trocas = new HashMap<>();
        for (Map.Entry<String, List<String>> entry : trocas.entrySet()) {
            this.trocas.put(entry.getKey(), new ArrayList<>(entry.getValue()));
        }

    }

    public Item(String id, double preco, String nome, double tempo) {
        super(id, preco, nome, tempo);
        this.alimentos = new HashMap<>();
        this.trocas = new HashMap<>();

    }

    // ====================================================================================================
    // GETTERS E SETTERS
    // ====================================================================================================
    public Map<String, Alimento> getAlimentos() {
        return this.alimentos;
    }

    public void setAlimentos(Map<String, Alimento> alimentos) {
        this.alimentos = alimentos;
    }

    public void setAlimento(String alimentoId, Alimento alimento) {
        this.alimentos.put(alimentoId, alimento);
    }

    public Map<String, List<String>> getTrocas() {
        return this.trocas;
    }

    public void setTrocas(Map<String, List<String>> trocas) {
        this.trocas = trocas;
    }

    public void setTroca(String alimentoOriginal, String alimentoTroca) {
        if (!this.trocas.containsKey(alimentoOriginal)) {
            this.trocas.put(alimentoOriginal, new ArrayList<>());
        }
        this.trocas.get(alimentoOriginal).add(alimentoTroca);
    }

    // ====================================================================================================
    // MÉTODOS
    // ====================================================================================================
    public void registaTroca(String idAlimentoAtual, Alimento alimentoDesejado) throws PedidoException {
        if (!this.trocas.containsKey(idAlimentoAtual))
            throw new PedidoException("O id: " + idAlimentoAtual
                    + " não existe como entrada no map de trocas (registaTroca - pedidos/Item.java)");
        List<String> trocasDisponiveis = this.trocas.get(idAlimentoAtual);

        if (!trocasDisponiveis.contains(alimentoDesejado.getId()))
            throw new PedidoException("O id: " + alimentoDesejado.getId()
                    + " não é um id válido na lista dos alimentos disponíveis para troca do id: " + idAlimentoAtual
                    + "registaTroca - pedidos/Item.java");

        this.alimentos.remove(idAlimentoAtual);
        this.alimentos.put(alimentoDesejado.getId(), alimentoDesejado);
    }

    // ====================================================================================================
    // TOSTRING CLONE EQUALS
    // ====================================================================================================
    @Override
    public Item clone() {
        return new Item(super.getId(), super.getPreco(), super.getNome(), super.getTempoConfecaoEsperado(),
                this.alimentos, this.trocas);
    }

}
