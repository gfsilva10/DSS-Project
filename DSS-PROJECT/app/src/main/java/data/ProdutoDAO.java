package data;

import model.pedidos.Produto;
import model.pedidos.Item;
import model.pedidos.Menu;
import model.gestao.Alimento;

import java.sql.*;
import java.util.*;

/**
 * DAO para Produtos (Items e Menus)
 * Implementa o padrão Map<String, Produto> para persistência de produtos
 */
public class ProdutoDAO implements Map<String, Produto> {
    private static ProdutoDAO singleton = null;

    private ProdutoDAO() {
        // Tabelas já criadas em PedidoDAO
    }

    public static ProdutoDAO getInstance() {
        if (ProdutoDAO.singleton == null) {
            ProdutoDAO.singleton = new ProdutoDAO();
        }
        return ProdutoDAO.singleton;
    }

    @Override
    public int size() {
        int i = 0;
        try (Connection conn = DriverManager.getConnection(DAOconfig.URL, DAOconfig.USERNAME, DAOconfig.PASSWORD);
                Statement stm = conn.createStatement();
                ResultSet rs = stm.executeQuery("SELECT count(*) FROM produtos")) {
            if (rs.next()) {
                i = rs.getInt(1);
            }
        } catch (Exception e) {
            e.printStackTrace();
            throw new NullPointerException(e.getMessage());
        }
        return i;
    }

    @Override
    public boolean isEmpty() {
        return this.size() == 0;
    }

    @Override
    public boolean containsKey(Object key) {
        boolean r = false;
        try (Connection conn = DriverManager.getConnection(DAOconfig.URL, DAOconfig.USERNAME, DAOconfig.PASSWORD);
                PreparedStatement pstm = conn.prepareStatement("SELECT Id FROM produtos WHERE Id=?")) {
            pstm.setString(1, (String) key);
            try (ResultSet rs = pstm.executeQuery()) {
                r = rs.next();
            }
        } catch (SQLException e) {
            e.printStackTrace();
            throw new NullPointerException(e.getMessage());
        }
        return r;
    }

    @Override
    public boolean containsValue(Object value) {
        Produto p = (Produto) value;
        return this.containsKey(p.getId());
    }

    @Override
    public Produto get(Object key) {
        Produto p = null;
        if (!(key instanceof String))
            return null;
        try (Connection conn = DriverManager.getConnection(DAOconfig.URL, DAOconfig.USERNAME, DAOconfig.PASSWORD)) {
            p = getProduto((String) key, conn);
        } catch (SQLException e) {
            e.printStackTrace();
            throw new NullPointerException(e.getMessage());
        }
        return p;
    }

    public Produto getProduto(String produtoId, Connection conn) throws SQLException {
        Produto p = null;
        boolean closeConn = false;

        if (conn == null) {
            conn = DriverManager.getConnection(DAOconfig.URL, DAOconfig.USERNAME, DAOconfig.PASSWORD);
            closeConn = true;
        }

        try (PreparedStatement pstm = conn.prepareStatement("SELECT * FROM produtos WHERE Id=?")) {
            pstm.setString(1, produtoId);
            try (ResultSet rs = pstm.executeQuery()) {
                if (rs.next()) {
                    String id = rs.getString("Id");
                    String nome = rs.getString("Nome");
                    double preco = rs.getDouble("Preco");
                    double tempo = rs.getDouble("TempoConfecaoEsperado");
                    String tipo = rs.getString("TipoProduto");

                    if ("ITEM".equals(tipo)) {
                        p = getItem(id, nome, preco, tempo, conn);
                    } else if ("MENU".equals(tipo)) {
                        p = getMenu(id, nome, preco, tempo, conn);
                    }
                }
            }
        } finally {
            if (closeConn && conn != null) {
                conn.close();
            }
        }
        return p;
    }

    private Item getItem(String id, String nome, double preco, double tempo, Connection conn) throws SQLException {
        Item item = new Item(id, preco, nome, tempo);

        // Carregar alimentos do item
        try (PreparedStatement pstm = conn.prepareStatement(
                "SELECT AlimentoId FROM item_alimentos WHERE ItemId=?")) {
            pstm.setString(1, id);
            try (ResultSet rs = pstm.executeQuery()) {
                AlimentoDAO alimentoDAO = AlimentoDAO.getInstance();
                while (rs.next()) {
                    String alimentoId = rs.getString("AlimentoId");
                    Alimento alimento = alimentoDAO.get(alimentoId);
                    if (alimento != null) {
                        item.setAlimento(alimentoId, alimento);
                    }
                }
            }
        }

        // Carregar trocas do item
        try (PreparedStatement pstm = conn.prepareStatement(
                "SELECT AlimentoOriginalId, AlimentoTrocaId FROM item_trocas WHERE ItemId=?")) {
            pstm.setString(1, id);
            try (ResultSet rs = pstm.executeQuery()) {
                while (rs.next()) {
                    String alimentoOriginal = rs.getString("AlimentoOriginalId");
                    String alimentoTroca = rs.getString("AlimentoTrocaId");

                    if (!item.getTrocas().containsKey(alimentoOriginal)) {
                        item.getTrocas().put(alimentoOriginal, new ArrayList<>());
                    }
                    item.setTroca(alimentoOriginal, alimentoTroca);
                }
            }
        }

        return item;
    }

    private Menu getMenu(String id, String nome, double preco, double tempo, Connection conn) throws SQLException {
        Menu menu = new Menu(id, preco, nome, tempo, new ArrayList<>());

        // Carregar items do menu
        try (PreparedStatement pstm = conn.prepareStatement(
                "SELECT ItemId FROM menu_itens WHERE MenuId=?")) {
            pstm.setString(1, id);
            try (ResultSet rs = pstm.executeQuery()) {
                while (rs.next()) {
                    String itemId = rs.getString("ItemId");
                    Produto item = getProduto(itemId, conn);
                    if (item instanceof Item) {
                        menu.getItens().add((Item) item);
                    }
                }
            }
        }

        return menu;
    }

    @Override
    public Produto put(String key, Produto produto) {
        Produto oldProduto = this.get(key);
        try (Connection conn = DriverManager.getConnection(DAOconfig.URL, DAOconfig.USERNAME, DAOconfig.PASSWORD)) {
            saveProduto(produto, conn);
        } catch (SQLException e) {
            e.printStackTrace();
            throw new NullPointerException(e.getMessage());
        }
        return oldProduto;
    }

    private void saveProduto(Produto produto, Connection conn) throws SQLException {
        // Inserir ou atualizar produto base
        try (PreparedStatement pstm = conn.prepareStatement(
                "INSERT INTO produtos (Id, Nome, Preco, TempoConfecaoEsperado, TipoProduto) VALUES (?, ?, ?, ?, ?) " +
                        "ON DUPLICATE KEY UPDATE Nome=VALUES(Nome), Preco=VALUES(Preco), TempoConfecaoEsperado=VALUES(TempoConfecaoEsperado)")) {
            pstm.setString(1, produto.getId());
            pstm.setString(2, produto.getNome());
            pstm.setDouble(3, produto.getPreco());
            pstm.setDouble(4, produto.getTempoConfecaoEsperado());

            if (produto instanceof Item) {
                pstm.setString(5, "ITEM");
                pstm.executeUpdate();
                saveItem((Item) produto, conn);
            } else if (produto instanceof Menu) {
                pstm.setString(5, "MENU");
                pstm.executeUpdate();
                saveMenu((Menu) produto, conn);
            }
        }
    }

    private void saveItem(Item item, Connection conn) throws SQLException {
        // Sincronizar alimentos com a database
        // Primeiro, remover alimentos existentes
        try (PreparedStatement pstm = conn.prepareStatement("DELETE FROM item_alimentos WHERE ItemId = ?")) {
            pstm.setString(1, item.getId());
            pstm.executeUpdate();
        }

        // Depois, inserir alimentos atualizados
        try (PreparedStatement pstm = conn.prepareStatement(
                "INSERT INTO item_alimentos (ItemId, AlimentoId) VALUES (?, ?)")) {
            for (String alimentoId : item.getAlimentos().keySet()) {
                pstm.setString(1, item.getId());
                pstm.setString(2, alimentoId);
                pstm.executeUpdate();
            }
        }

        // Salvar trocas (só se ainda não existirem)
        if (!produtoExiste(item.getId(), conn)) {
            try (PreparedStatement pstm = conn.prepareStatement(
                    "INSERT INTO item_trocas (ItemId, AlimentoOriginalId, AlimentoTrocaId) VALUES (?, ?, ?)")) {
                for (Map.Entry<String, List<String>> entrada : item.getTrocas().entrySet()) {
                    String alimentoOriginal = entrada.getKey();
                    for (String alimentoTroca : entrada.getValue()) {
                        pstm.setString(1, item.getId());
                        pstm.setString(2, alimentoOriginal);
                        pstm.setString(3, alimentoTroca);
                        pstm.executeUpdate();
                    }
                }
            }
        }
    }

    private void saveMenu(Menu menu, Connection conn) throws SQLException {
        // Remover itens antigos
        try (PreparedStatement pstm = conn.prepareStatement("DELETE FROM menu_itens WHERE MenuId=?")) {
            pstm.setString(1, menu.getId());
            pstm.executeUpdate();
        }

        // Inserir itens novos
        try (PreparedStatement pstm = conn.prepareStatement(
                "INSERT INTO menu_itens (MenuId, ItemId) VALUES (?, ?)")) {
            for (Item item : menu.getItens()) {
                pstm.setString(1, menu.getId());
                pstm.setString(2, item.getId());
                pstm.executeUpdate();
            }
        }
    }

    @Override
    public Produto remove(Object key) {
        Produto p = this.get(key);
        try (Connection conn = DriverManager.getConnection(DAOconfig.URL, DAOconfig.USERNAME, DAOconfig.PASSWORD);
                PreparedStatement pstm = conn.prepareStatement("DELETE FROM produtos WHERE Id=?")) {
            pstm.setString(1, (String) key);
            pstm.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
            throw new NullPointerException(e.getMessage());
        }
        return p;
    }

    @Override
    public void putAll(Map<? extends String, ? extends Produto> produtos) {
        for (Produto p : produtos.values()) {
            this.put(p.getId(), p);
        }
    }

    @Override
    public void clear() {
        try (Connection conn = DriverManager.getConnection(DAOconfig.URL, DAOconfig.USERNAME, DAOconfig.PASSWORD);
                Statement stm = conn.createStatement()) {
            stm.executeUpdate("DELETE FROM menu_itens");
            stm.executeUpdate("DELETE FROM item_trocas");
            stm.executeUpdate("DELETE FROM item_alimentos");
            stm.executeUpdate("DELETE FROM produtos");
        } catch (SQLException e) {
            e.printStackTrace();
            throw new NullPointerException(e.getMessage());
        }
    }

    @Override
    public Set<String> keySet() {
        throw new UnsupportedOperationException("Not implemented");
    }

    @Override
    public Collection<Produto> values() {
        Collection<Produto> produtos = new HashSet<>();
        try (Connection conn = DriverManager.getConnection(DAOconfig.URL, DAOconfig.USERNAME, DAOconfig.PASSWORD);
                Statement stm = conn.createStatement();
                ResultSet rs = stm.executeQuery("SELECT Id FROM produtos")) {
            while (rs.next()) {
                String id = rs.getString("Id");
                Produto p = getProduto(id, conn);
                if (p != null) {
                    produtos.add(p);
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
            throw new NullPointerException(e.getMessage());
        }
        return produtos;
    }

    @Override
    public Set<Entry<String, Produto>> entrySet() {
        throw new UnsupportedOperationException("Not implemented");
    }

    private boolean produtoExiste(String produtoId, Connection conn) throws SQLException {
        try (PreparedStatement pstm = conn.prepareStatement("SELECT Id FROM produtos WHERE Id=?")) {
            pstm.setString(1, produtoId);
            try (ResultSet rs = pstm.executeQuery()) {
                return rs.next();
            }
        }
    }
}
