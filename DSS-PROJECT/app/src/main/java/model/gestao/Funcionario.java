package model.gestao;

public class Funcionario {

    // ====================================================================================================
    // ATRIBUTOS
    // ====================================================================================================
    private long id;
    private String nome;
    private String password;
    private Perfil perfil;

    // ====================================================================================================
    // CONSTRUTORES
    // ====================================================================================================
    public Funcionario() {
        this.id = 0;
        this.nome = "";
        this.password = "";
        this.perfil = Perfil.NORMAL;
    }

    public Funcionario(long id, String nome, String password, Perfil perfil) {
        this.id = id;
        this.nome = nome;
        this.password = password;
        this.perfil = perfil;
    }

    public Funcionario(Funcionario outro) {
        this.id = outro.id;
        this.nome = outro.nome;
        this.password = outro.password;
        this.perfil = outro.perfil;
    }

    // ====================================================================================================
    // GETTERS E SETTERS
    // ====================================================================================================
    public long getId() {
        return this.id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public String getNome() {
        return this.nome;
    }

    public void setNome(String nome) {
        this.nome = nome;
    }

    public String getPassword() {
        return this.password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public Perfil getPerfil() {
        return this.perfil;
    }

    public void setPerfil(Perfil perfil) {
        this.perfil = perfil;
    }

    public boolean isAdmin() {
        return this.perfil == Perfil.ADMIN;
    }

    public Funcionario clone() {
        Funcionario copia = new Funcionario();
        copia.id = this.id;
        copia.nome = this.nome;
        copia.password = this.password;
        copia.perfil = this.perfil;
        return copia;
    }
}
