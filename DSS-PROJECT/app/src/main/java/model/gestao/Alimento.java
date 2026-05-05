package model.gestao;

public class Alimento {
    private int quantidade;
    private String id;
    private String nome;

    // ====================================================================================================
    // CONSTRUTORES
    // ====================================================================================================
    public Alimento() {
        this.quantidade = 0;
        this.id = "";
        this.nome = "";
    }

    public Alimento(int quantidade, String id, String nome) {
        this.quantidade = quantidade;
        this.id = id;
        this.nome = nome;
    }

    public Alimento(Alimento alimento) {
        this.quantidade = alimento.getQuantidade();
        this.id = alimento.getId();
        this.nome = alimento.getNome();
    }

    // ====================================================================================================
    // GETTERS E SETTERS
    // ====================================================================================================
    public int getQuantidade() {
        return this.quantidade;
    }

    public void setQuantidade(int quantidade) {
        this.quantidade = quantidade;
    }

    public String getId() {
        return this.id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getNome() {
        return this.nome;
    }

    public void setNome(String nome) {
        this.nome = nome;
    }
    // ====================================================================================================
    // MÉTODOS
    // ====================================================================================================

    // ====================================================================================================
    // TOSTRING CLONE EQUALS
    // ====================================================================================================
    public Alimento clone() {
        return new Alimento(this.quantidade, this.id, this.nome);
    }
}
