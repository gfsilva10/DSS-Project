package model.preparacoes;

import java.util.HashMap;
import java.util.Map;

public class Posto {

    private final String id;
    private final Map<String, Integer> quantidadeAlimento; // idAlimento -> quantidade
    private Long funcionarioId; // null -> livre
    private TipoPosto tipo;

    public Posto(String id) {
        this.id = id;
        this.quantidadeAlimento = new HashMap<>();
        this.funcionarioId = null;
        this.tipo = TipoPosto.CAIXA;
    }

    public String getId() {
        return id;
    }

    public Map<String, Integer> getQuantidadeAlimento() {
        return quantidadeAlimento;
    }

    public Long getFuncionarioId() {
        return funcionarioId;
    }

    public void setFuncionarioId(Long funcionarioId) {
        this.funcionarioId = funcionarioId;
    }

    public boolean estaLivre() {
        return this.funcionarioId == null;
    }

    public TipoPosto getTipo() {
        return tipo;
    }

    public void setTipo(TipoPosto tipo) {
        this.tipo = tipo;
    }

    public Posto clone() {
        Posto copia = new Posto(this.id);
        copia.funcionarioId = this.funcionarioId;
        copia.quantidadeAlimento.putAll(this.quantidadeAlimento);
        copia.tipo = this.tipo;
        return copia;
    }

}
