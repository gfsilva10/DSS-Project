package model.gestao;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import data.AlimentoDAO;
import data.FuncionarioDAO;
import data.PedidoDAO;
import data.PostoDAO;
import model.pedidos.Pedido;
import model.preparacoes.Posto;

public class GestaoFacade implements InterGestaoL {
    private Map<String, Alimento> alimentos;
    private Map<Long, Pedido> pedidos; //Como temos DAO de Pedidos, podemos usar aqui para calcular tempos de confeção sem ter que passar o PedidosFacade como dependência
    private ArrayList<String> mensagens;
    private Map<String, Posto> postos;
    private Map<Long, Funcionario> funcionarios;


    // ====================================================================================================
    // CONSTRUTORES
    // ====================================================================================================
    public GestaoFacade() {
        this.alimentos = AlimentoDAO.getInstance();
        this.pedidos = PedidoDAO.getInstance();
        this.mensagens = new ArrayList<>();
        this.postos = PostoDAO.getInstance();
        this.funcionarios = FuncionarioDAO.getInstance();
    }

    // ====================================================================================================
    // GETTERS E SETTERS
    // ====================================================================================================

    @Override
    public Alimento getAlimento(String idAlimento) {
        Alimento a = this.alimentos.get(idAlimento);
        return a != null ? a.clone() : null;
    }

    @Override
    public Collection<Alimento> getAlimentos() {
        Collection<Alimento> copia = new ArrayList<>();
        for (Alimento alimento : this.alimentos.values()) {
            if (alimento != null) {
                copia.add(alimento.clone());
            }
        }
        return copia;
    }

    // ====================================================================================================
    // MÉTODOS
    // ====================================================================================================
    @Override
    public double apresentaTempoConfecao() {
        Collection<Pedido> todosPedidos = this.pedidos.values(); //this.pedidos.getpedidos() pedidos é facade
        double somaTempos = 0;
        int totalPedidos = 0;
        double media = 0;

        for (Pedido pedido : todosPedidos) {
            if (pedido == null) {
                continue;
            }
            // Consideramos apenas o restaurante atual (assume-se um único restaurante)
            somaTempos += pedido.getTempoConfecaoReal();
            totalPedidos++;
        }
        
        media = totalPedidos > 0 ? somaTempos / totalPedidos : 0;
        return media;
    }

    @Override
    public Map<String, Integer> apresentaStock(){
        Map<String, Integer> stockAtual = new HashMap<>();

        Collection<Alimento> listaAlimentos = this.getAlimentos();
        for (Alimento alimento : listaAlimentos) {
            if (alimento == null) {
                continue;
            }
            stockAtual.put(alimento.getId(), alimento.getQuantidade());
        }

        return stockAtual;
    }

    @Override
    public void enviaMensagem(String mensagem) {
        if (mensagem == null) {
            return;
        }
        String msg = mensagem.trim();
        if (msg.isEmpty()) {
            return;
        }
        this.mensagens.add(msg);
    }

    @Override
    public List<String> getMensagens() {
        return new ArrayList<>(this.mensagens);
    }

    @Override
    public List<String> getPostosLivres() {
        List<String> livres = new ArrayList<>();
        for (Posto posto : this.postos.values()) {
            if (posto != null && posto.estaLivre()) {
                livres.add(posto.getId());
            }
        }
        return livres;
    }

    @Override
    public void registaFuncionario(long id, String nome, String password, boolean admin) {
        Perfil perfil = admin ? Perfil.ADMIN : Perfil.NORMAL;
        Funcionario f = new Funcionario(id, nome, password, perfil);
        this.funcionarios.put(id, f);
    }

    @Override
    public boolean autenticaFuncionario(long id, String password) {
        Funcionario f = this.funcionarios.get(id);
        return f != null && f.getPassword().equals(password);
    }

    @Override
    public boolean funcionarioEAdmin(long id) {
        Funcionario f = this.funcionarios.get(id);
        return f != null && f.isAdmin();
    }

    // ====================================================================================================
    // TOSTRING CLONE EQUALS
    // ====================================================================================================
}
