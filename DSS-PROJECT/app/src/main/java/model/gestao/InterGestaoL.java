package model.gestao;

import java.util.Collection;
import java.util.List;
import java.util.Map;

public interface InterGestaoL {
    public Alimento getAlimento(String idAlimento);

    public Collection<Alimento> getAlimentos();

    public double apresentaTempoConfecao();

    public Map<String, Integer> apresentaStock();

    public void enviaMensagem(String mensagem);

    public List<String> getMensagens();

    public List<String> getPostosLivres();

    public void registaFuncionario(long id, String nome, String password, boolean admin);

    public boolean autenticaFuncionario(long id, String password);

    public boolean funcionarioEAdmin(long id);

}
