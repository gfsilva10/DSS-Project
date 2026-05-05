package data;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import model.gestao.Alimento;
import model.pedidos.Estado;
import model.pedidos.Item;
import model.pedidos.Menu;
import model.pedidos.Pedido;
import model.pedidos.Produto;

/**
 * DAO para Pedidos Implementa o padrão Map<Long, Pedido> para persistência de
 * pedidos
 */
public class PedidoDAO implements Map<Long, Pedido> {

    private static PedidoDAO singleton = null;

    private PedidoDAO() {
        try (Connection conn = DriverManager.getConnection(DAOconfig.URL, DAOconfig.USERNAME, DAOconfig.PASSWORD);
                Statement stm = conn.createStatement()) {

        /*     // Desabilitar FK constraints temporariamente para dropar tudo limpo
            stm.executeUpdate("SET FOREIGN_KEY_CHECKS=0");

            // Drop de todas as tabelas para garantir recriação desde o início
            stm.executeUpdate("DROP TABLE IF EXISTS pedido_produto_alimentos");
            stm.executeUpdate("DROP TABLE IF EXISTS pedido_produtos");
            stm.executeUpdate("DROP TABLE IF EXISTS menu_itens");
            stm.executeUpdate("DROP TABLE IF EXISTS item_trocas");
            stm.executeUpdate("DROP TABLE IF EXISTS item_alimentos");
            stm.executeUpdate("DROP TABLE IF EXISTS pedidos");
            stm.executeUpdate("DROP TABLE IF EXISTS produtos");
            stm.executeUpdate("DROP TABLE IF EXISTS alimentos");
            stm.executeUpdate("DROP TABLE IF EXISTS postos");

            stm.executeUpdate("SET FOREIGN_KEY_CHECKS=1"); */

            // // Desabilitar FK constraints temporariamente para dropar tudo limpo
            // stm.executeUpdate("SET FOREIGN_KEY_CHECKS=0");

            // // Drop de todas as tabelas para garantir recriação desde o início
            // stm.executeUpdate("DROP TABLE IF EXISTS pedido_produto_alimentos");
            // stm.executeUpdate("DROP TABLE IF EXISTS pedido_produtos");
            // stm.executeUpdate("DROP TABLE IF EXISTS menu_itens");
            // stm.executeUpdate("DROP TABLE IF EXISTS item_trocas");
            // stm.executeUpdate("DROP TABLE IF EXISTS item_alimentos");
            // stm.executeUpdate("DROP TABLE IF EXISTS pedidos");
            // stm.executeUpdate("DROP TABLE IF EXISTS produtos");
            // stm.executeUpdate("DROP TABLE IF EXISTS alimentos");
            // stm.executeUpdate("DROP TABLE IF EXISTS postos");

            // stm.executeUpdate("SET FOREIGN_KEY_CHECKS=1");

            // Tabela Alimentos
            String sql = "CREATE TABLE IF NOT EXISTS alimentos ("
                    + "Id VARCHAR(50) NOT NULL PRIMARY KEY,"
                    + "Nome VARCHAR(100) NOT NULL,"
                    + "Quantidade INT DEFAULT 0)";
            stm.executeUpdate(sql);

            // Tabela Produtos (abstrata - armazena info comum)
            sql = "CREATE TABLE IF NOT EXISTS produtos ("
                    + "Id VARCHAR(50) NOT NULL PRIMARY KEY,"
                    + "Nome VARCHAR(100) NOT NULL,"
                    + "Preco DOUBLE NOT NULL,"
                    + "TempoConfecaoEsperado DOUBLE NOT NULL,"
                    + "TipoProduto VARCHAR(20) NOT NULL)"; // 'ITEM' ou 'MENU'
            stm.executeUpdate(sql);

            // Tabela Item_Alimentos (relaciona itens com alimentos)
            sql = "CREATE TABLE IF NOT EXISTS item_alimentos ("
                    + "ItemId VARCHAR(50) NOT NULL,"
                    + "AlimentoId VARCHAR(50) NOT NULL,"
                    + "PRIMARY KEY(ItemId, AlimentoId),"
                    + "FOREIGN KEY(ItemId) REFERENCES produtos(Id) ON DELETE CASCADE,"
                    + "FOREIGN KEY(AlimentoId) REFERENCES alimentos(Id) ON DELETE CASCADE)";
            stm.executeUpdate(sql);

            // Tabela Item_Trocas (possíveis trocas de alimentos)
            sql = "CREATE TABLE IF NOT EXISTS item_trocas ("
                    + "ItemId VARCHAR(50) NOT NULL,"
                    + "AlimentoOriginalId VARCHAR(50) NOT NULL,"
                    + "AlimentoTrocaId VARCHAR(50) NOT NULL,"
                    + "PRIMARY KEY(ItemId, AlimentoOriginalId, AlimentoTrocaId),"
                    + "FOREIGN KEY(ItemId) REFERENCES produtos(Id) ON DELETE CASCADE,"
                    + "FOREIGN KEY(AlimentoOriginalId) REFERENCES alimentos(Id) ON DELETE CASCADE,"
                    + "FOREIGN KEY(AlimentoTrocaId) REFERENCES alimentos(Id) ON DELETE CASCADE)";
            stm.executeUpdate(sql);

            // Tabela Menu_Itens (relaciona menus com itens)
            sql = "CREATE TABLE IF NOT EXISTS menu_itens ("
                    + "MenuId VARCHAR(50) NOT NULL,"
                    + "ItemId VARCHAR(50) NOT NULL,"
                    + "PRIMARY KEY(MenuId, ItemId),"
                    + "FOREIGN KEY(MenuId) REFERENCES produtos(Id) ON DELETE CASCADE,"
                    + "FOREIGN KEY(ItemId) REFERENCES produtos(Id) ON DELETE CASCADE)";
            stm.executeUpdate(sql);

            // Tabela Pedidos
            sql = "CREATE TABLE IF NOT EXISTS pedidos ("
                    + "Id BIGINT NOT NULL PRIMARY KEY,"
                    + "Estado VARCHAR(20) DEFAULT 'PorPagar',"
                    + "Nota VARCHAR(255) DEFAULT '',"
                    + "Preco DOUBLE DEFAULT 0,"
                    + "TempoConfecaoEsperado DOUBLE DEFAULT 0,"
                    + "TempoConfecaoReal DOUBLE DEFAULT 0,"
                    + "Tipo BOOLEAN DEFAULT TRUE)"; // true -> restaurante; false -> takeaway
            stm.executeUpdate(sql);

            // Tabelas de produtos em pedidos - migrar schema se vier de versões antigas
            // Drop tabela dependente para poder ajustar a PK de pedido_produtos
            stm.executeUpdate("DROP TABLE IF EXISTS pedido_produto_alimentos");

            // Tabela Pedido_Produtos (relaciona pedidos com produtos)
            // Sequencia permite múltiplos produtos iguais no mesmo pedido (ex: 4 batatas
            // fritas)
            sql = "CREATE TABLE IF NOT EXISTS pedido_produtos ("
                    + "PedidoId BIGINT NOT NULL,"
                    + "ProdutoId VARCHAR(50) NOT NULL,"
                    + "Sequencia INT NOT NULL,"
                    + "PRIMARY KEY(PedidoId, ProdutoId, Sequencia),"
                    + "FOREIGN KEY(PedidoId) REFERENCES pedidos(Id) ON DELETE CASCADE,"
                    + "FOREIGN KEY(ProdutoId) REFERENCES produtos(Id) ON DELETE CASCADE)";
            stm.executeUpdate(sql);

            // Tabela Pedido_Produto_Alimentos - ESSENCIAL para garantir a composição Pedido
            // -> Produto -> Alimentos
            // Esta tabela é necessária para manter alimentos específicos de cada produto em
            // cada pedido
            // Permite que o mesmo produto em pedidos diferentes tenha alimentos diferentes
            // Ex: Pedido 1 tem BigMac com carne_frango, Pedido 2 tem BigMac com carne_vaca
            // Sequencia permite distinguir produtos duplicados (ex: BigMac #1 vs BigMac #2)
            sql = "CREATE TABLE IF NOT EXISTS pedido_produto_alimentos ("
                    + "PedidoId BIGINT NOT NULL,"
                    + "ProdutoId VARCHAR(50) NOT NULL,"
                    + "Sequencia INT NOT NULL,"
                    + "ItemId VARCHAR(50) NOT NULL,"
                    + "AlimentoId VARCHAR(50) NOT NULL,"
                    + "PRIMARY KEY(PedidoId, ProdutoId, Sequencia, ItemId, AlimentoId),"
                    + "FOREIGN KEY(PedidoId, ProdutoId, Sequencia) REFERENCES pedido_produtos(PedidoId, ProdutoId, Sequencia) ON DELETE CASCADE,"
                    + "FOREIGN KEY(ItemId) REFERENCES produtos(Id) ON DELETE CASCADE,"
                    + "FOREIGN KEY(AlimentoId) REFERENCES alimentos(Id) ON DELETE CASCADE)";
            stm.executeUpdate(sql);

            // Inicializar o IdCounter do Pedido com o maior ID da base de dados
            try (Statement stmCounter = conn.createStatement(); ResultSet rsCounter = stmCounter.executeQuery("SELECT COALESCE(MAX(Id), 0) + 1 FROM pedidos")) {
                if (rsCounter.next()) {
                    long maxId = rsCounter.getLong(1);
                    Pedido.setIdCounter(maxId);
                }
            }

        } catch (SQLException e) {
            e.printStackTrace();
            throw new NullPointerException(e.getMessage());
        }
    }

    public static PedidoDAO getInstance() {
        if (PedidoDAO.singleton == null) {
            PedidoDAO.singleton = new PedidoDAO();
        }
        return PedidoDAO.singleton;
    }

    @Override
    public int size() {
        int i = 0;
        try (Connection conn = DriverManager.getConnection(DAOconfig.URL, DAOconfig.USERNAME, DAOconfig.PASSWORD);
                Statement stm = conn.createStatement();
                ResultSet rs = stm.executeQuery("SELECT count(*) FROM pedidos")) {
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
                PreparedStatement pstm = conn.prepareStatement("SELECT Id FROM pedidos WHERE Id=?")) {
            pstm.setLong(1, (Long) key);
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
        Pedido p = (Pedido) value;
        return this.containsKey(p.getIdCounter());
    }

    @Override
    public Pedido get(Object key) {
        Pedido p = null;
        if (!(key instanceof Long)) {
            return null;
        }
        try (Connection conn = DriverManager.getConnection(DAOconfig.URL, DAOconfig.USERNAME, DAOconfig.PASSWORD);
                PreparedStatement pstm = conn.prepareStatement("SELECT * FROM pedidos WHERE Id=?")) {
            pstm.setLong(1, (Long) key);
            try (ResultSet rs = pstm.executeQuery()) {
                if (rs.next()) {
                    p = new Pedido();
                    p.setId(rs.getLong("Id"));
                    p.setEstado(Estado.valueOf(rs.getString("Estado")));
                    p.setNota(rs.getString("Nota"));
                    p.setPreco(rs.getDouble("Preco"));
                    p.setTempoConfecaoEsperado(rs.getDouble("TempoConfecaoEsperado"));
                    p.setTempoConfecaoReal(rs.getDouble("TempoConfecaoReal"));
                    p.setTipo(rs.getBoolean("Tipo"));

                    // Carregar produtos associados ao pedido
                    p.setProdutos(getProdutosPedido((Long) key, conn));
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
            throw new NullPointerException(e.getMessage());
        }
        return p;
    }

    /**
     * Remove um alimento
     */
    public void removeAlimento(String id) {
        try (Connection conn = DriverManager.getConnection(DAOconfig.URL, DAOconfig.USERNAME, DAOconfig.PASSWORD);
                PreparedStatement pstm = conn.prepareStatement("DELETE FROM alimentos WHERE Id=?")) {
            pstm.setString(1, id);
            pstm.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
            throw new NullPointerException(e.getMessage());
        }
    }

    private List<Produto> getProdutosPedido(Long pedidoId, Connection conn) throws SQLException {
        List<Produto> produtos = new ArrayList<>();
        try (PreparedStatement pstm = conn.prepareStatement(
                "SELECT ProdutoId, Sequencia FROM pedido_produtos WHERE PedidoId=? ORDER BY Sequencia")) {
            pstm.setLong(1, pedidoId);
            try (ResultSet rs = pstm.executeQuery()) {
                while (rs.next()) {
                    String produtoId = rs.getString("ProdutoId");
                    int sequencia = rs.getInt("Sequencia");
                    Produto produto = getProduto(produtoId, conn);

                    // Se for Item, carregar alimentos específicos deste produto neste pedido
                    // Garante a composição: cada pedido tem seus próprios alimentos por produto
                    if (produto != null && produto instanceof Item) {
                        Item item = (Item) produto;
                        carregaAlimentosPedido(item, pedidoId, sequencia, item.getId(), conn);
                        produtos.add(item);
                    } else if (produto != null && produto instanceof Menu) {
                        Menu menu = (Menu) produto;
                        for (Item item : menu.getItens()) {
                            carregaAlimentosPedido(item, pedidoId, sequencia, menu.getId(), conn);
                        }
                        produtos.add(menu);
                    } else if (produto != null) {
                        produtos.add(produto);
                    }
                }
            }
        }
        return produtos;
    }

    /**
     * Carrega alimentos específicos de um item dentro de um pedido específico
     * ESSENCIAL para garantir composição: cada pedido tem seus próprios
     * alimentos por produto Exemplo: Pedido 1 BigMac com carne_frango, Pedido 2
     * BigMac com carne_vaca
     */
    private void carregaAlimentosPedido(Item item, Long pedidoId, int sequencia, String produtoId, Connection conn)
            throws SQLException {
        try (PreparedStatement pstm = conn.prepareStatement(
                "SELECT AlimentoId FROM pedido_produto_alimentos WHERE PedidoId=? AND ProdutoId=? AND Sequencia=? AND ItemId=?")) {
            pstm.setLong(1, pedidoId);
            pstm.setString(2, produtoId);
            pstm.setInt(3, sequencia);
            pstm.setString(4, item.getId());
            try (ResultSet rs = pstm.executeQuery()) {
                boolean encontrou = false;
                Map<String, Alimento> alimentosNovos = new HashMap<>();
                while (rs.next()) {
                    String alimentoId = rs.getString("AlimentoId");
                    Alimento alimento = AlimentoDAO.getInstance().get(alimentoId);
                    if (alimento != null) {
                        alimentosNovos.put(alimentoId, alimento);
                        encontrou = true;
                    }
                }
                if (encontrou) {
                    item.getAlimentos().clear(); // Substituir composição só quando há dados do pedido
                    item.getAlimentos().putAll(alimentosNovos);
                }
            }
        }
    }

    public Produto getProduto(String produtoId, Connection conn) throws SQLException {
        try (PreparedStatement pstm = conn.prepareStatement(
                "SELECT * FROM produtos WHERE Id=?")) {
            pstm.setString(1, produtoId);
            try (ResultSet rs = pstm.executeQuery()) {
                if (rs.next()) {
                    String tipo = rs.getString("TipoProduto");
                    String id = rs.getString("Id");
                    String nome = rs.getString("Nome");
                    double preco = rs.getDouble("Preco");
                    double tempo = rs.getDouble("TempoConfecaoEsperado");

                    if ("ITEM".equals(tipo)) {
                        return getItem(id, nome, preco, tempo, conn);
                    } else if ("MENU".equals(tipo)) {
                        return getMenu(id, nome, preco, tempo, conn);
                    }
                }
            }
        }
        return null;
    }

    private Item getItem(String id, String nome, double preco, double tempo, Connection conn) throws SQLException {

        // Carregar alimentos do item
        Map<String, Alimento> alimentos = new HashMap<>();
        try (PreparedStatement pstm = conn.prepareStatement(
                "SELECT a.* FROM alimentos a "
                        + "INNER JOIN item_alimentos ia ON a.Id = ia.AlimentoId "
                        + "WHERE ia.ItemId = ?")) {
            pstm.setString(1, id);
            try (ResultSet rs = pstm.executeQuery()) {
                while (rs.next()) {
                    // Usa quantidade unitária por item, não o stock global de alimentos
                    Alimento alimento = new Alimento(
                            1,
                            rs.getString("Id"),
                            rs.getString("Nome"));
                    alimentos.put(alimento.getId(), alimento);
                }
            }
        }

        // Carregar trocas possíveis
        Map<String, List<String>> trocas = new HashMap<>();
        try (PreparedStatement pstm = conn.prepareStatement(
                "SELECT AlimentoOriginalId, AlimentoTrocaId FROM item_trocas WHERE ItemId = ?")) {
            pstm.setString(1, id);
            try (ResultSet rs = pstm.executeQuery()) {
                while (rs.next()) {
                    String originalId = rs.getString("AlimentoOriginalId");
                    String trocaId = rs.getString("AlimentoTrocaId");

                    trocas.putIfAbsent(originalId, new ArrayList<>());
                    trocas.get(originalId).add(trocaId);
                }
            }
        }
        return new Item(id, preco, nome, tempo, alimentos, trocas);
    }

    private Menu getMenu(String id, String nome, double preco, double tempo, Connection conn) throws SQLException {
        List<Item> itens = new ArrayList<>();

        try (PreparedStatement pstm = conn.prepareStatement(
                "SELECT p.* FROM produtos p "
                        + "INNER JOIN menu_itens mi ON p.Id = mi.ItemId "
                        + "WHERE mi.MenuId = ?")) {
            pstm.setString(1, id);
            try (ResultSet rs = pstm.executeQuery()) {
                while (rs.next()) {
                    Item item = getItem(
                            rs.getString("Id"),
                            rs.getString("Nome"),
                            rs.getDouble("Preco"),
                            rs.getDouble("TempoConfecaoEsperado"),
                            conn);
                    itens.add(item);
                }
            }
        }

        return new Menu(id, preco, nome, tempo, itens);
    }

    private void saveProduto(Produto produto, Connection conn) throws SQLException {
        String tipoProduto = produto instanceof Menu ? "MENU" : "ITEM";

        try (PreparedStatement pstm = conn.prepareStatement(
                "INSERT INTO produtos VALUES (?, ?, ?, ?, ?) "
                        + "ON DUPLICATE KEY UPDATE Nome=VALUES(Nome), Preco=VALUES(Preco), "
                        + "TempoConfecaoEsperado=VALUES(TempoConfecaoEsperado), TipoProduto=VALUES(TipoProduto)")) {
            pstm.setString(1, produto.getId());
            pstm.setString(2, produto.getNome());
            pstm.setDouble(3, produto.getPreco());
            pstm.setDouble(4, produto.getTempoConfecaoEsperado());
            pstm.setString(5, tipoProduto);
            pstm.executeUpdate();
        }

        if (produto instanceof Menu) {
            saveMenu((Menu) produto, conn);
        } else if (produto instanceof Item) {
            saveItem((Item) produto, conn);
        }
    }

    private void saveItem(Item item, Connection conn) throws SQLException {
        // Remover alimentos e trocas antigos
        try (PreparedStatement pstm = conn.prepareStatement(
                "DELETE FROM item_alimentos WHERE ItemId=?")) {
            pstm.setString(1, item.getId());
            pstm.executeUpdate();
        }

        try (PreparedStatement pstm = conn.prepareStatement(
                "DELETE FROM item_trocas WHERE ItemId=?")) {
            pstm.setString(1, item.getId());
            pstm.executeUpdate();
        }

        // Inserir alimentos do item
        if (item.getAlimentos() != null) {
            try (PreparedStatement pstm = conn.prepareStatement(
                    "INSERT INTO item_alimentos (ItemId, AlimentoId) VALUES (?, ?)")) {
                for (Alimento alimento : item.getAlimentos().values()) {
                    saveAlimento(alimento, conn);
                    pstm.setString(1, item.getId());
                    pstm.setString(2, alimento.getId());
                    pstm.addBatch();
                }
                pstm.executeBatch();
            }
        }

        // Inserir trocas do item
        if (item.getTrocas() != null) {
            try (PreparedStatement pstm = conn.prepareStatement(
                    "INSERT INTO item_trocas (ItemId, AlimentoOriginalId, AlimentoTrocaId) VALUES (?, ?, ?)")) {
                for (Map.Entry<String, List<String>> entrada : item.getTrocas().entrySet()) {
                    String alimentoOriginalId = entrada.getKey();
                    for (String alimentoTrocaId : entrada.getValue()) {
                        pstm.setString(1, item.getId());
                        pstm.setString(2, alimentoOriginalId);
                        pstm.setString(3, alimentoTrocaId);
                        pstm.addBatch();
                    }
                }
                pstm.executeBatch();
            }
        }
    }

    private void saveMenu(Menu menu, Connection conn) throws SQLException {
        // Remover itens antigos do menu
        try (PreparedStatement pstm = conn.prepareStatement(
                "DELETE FROM menu_itens WHERE MenuId=?")) {
            pstm.setString(1, menu.getId());
            pstm.executeUpdate();
        }

        // Inserir novos itens do menu
        if (menu.getItens() != null) {
            try (PreparedStatement pstm = conn.prepareStatement(
                    "INSERT INTO menu_itens (MenuId, ItemId) VALUES (?, ?)")) {
                for (Item item : menu.getItens()) {
                    saveItem(item, conn);
                    pstm.setString(1, menu.getId());
                    pstm.setString(2, item.getId());
                    pstm.addBatch();
                }
                pstm.executeBatch();
            }
        }
    }

    private void saveAlimento(Alimento alimento, Connection conn) throws SQLException {
        try (PreparedStatement pstm = conn.prepareStatement(
                "INSERT INTO alimentos VALUES (?, ?, ?) "
                        + "ON DUPLICATE KEY UPDATE Nome=VALUES(Nome), Quantidade=VALUES(Quantidade)")) {
            pstm.setString(1, alimento.getId());
            pstm.setString(2, alimento.getNome());
            pstm.setInt(3, alimento.getQuantidade());
            pstm.executeUpdate();
        }
    }

    @Override
    public Pedido put(Long key, Pedido p) {
        Pedido res = null;
        try (Connection conn = DriverManager.getConnection(DAOconfig.URL, DAOconfig.USERNAME, DAOconfig.PASSWORD)) {
            conn.setAutoCommit(false);

            try (PreparedStatement pstm = conn.prepareStatement(
                    "INSERT INTO pedidos VALUES (?, ?, ?, ?, ?, ?, ?) "
                            + "ON DUPLICATE KEY UPDATE Estado=VALUES(Estado), Nota=VALUES(Nota), "
                            + "Preco=VALUES(Preco), TempoConfecaoEsperado=VALUES(TempoConfecaoEsperado), "
                            + "TempoConfecaoReal=VALUES(TempoConfecaoReal), Tipo=VALUES(Tipo)")) {
                pstm.setLong(1, p.getIdCounter());
                pstm.setString(2, p.getEstado().toString());
                pstm.setString(3, p.getNota());
                pstm.setDouble(4, p.getPreco());
                pstm.setDouble(5, p.getTempoConfecaoEsperado());
                pstm.setDouble(6, p.getTempoConfecaoReal());
                pstm.setBoolean(7, p.getTipo());
                pstm.executeUpdate();
            }

            // Remover alimentos e produtos antigos (garante composição limpa para este
            // pedido)
            try (PreparedStatement pstm = conn.prepareStatement(
                    "DELETE FROM pedido_produto_alimentos WHERE PedidoId=?")) {
                pstm.setLong(1, p.getIdCounter());
                pstm.executeUpdate();
            }

            try (PreparedStatement pstm = conn.prepareStatement(
                    "DELETE FROM pedido_produtos WHERE PedidoId=?")) {
                pstm.setLong(1, p.getIdCounter());
                pstm.executeUpdate();
            }

            // Inserir produtos do pedido e seus alimentos específicos (garante composição)
            int sequencia = 0;
            for (Produto produto : p.getProdutos()) {
                // SÓ salvar o produto se ele NÃO existir ainda na BD
                if (!produtoExiste(produto.getId(), conn)) {
                    saveProduto(produto, conn);
                }

                try (PreparedStatement pstm = conn.prepareStatement(
                        "INSERT INTO pedido_produtos (PedidoId, ProdutoId, Sequencia) VALUES (?, ?, ?)")) {
                    pstm.setLong(1, p.getIdCounter());
                    pstm.setString(2, produto.getId());
                    pstm.setInt(3, sequencia);
                    pstm.executeUpdate();
                }

                // Guardar alimentos específicos do produto neste pedido (ESSENCIAL para
                // composição)
                // Permite que produtos iguais em pedidos diferentes tenham alimentos diferentes
                if (produto instanceof Item) {
                    Item item = (Item) produto;
                    try (PreparedStatement pstmAlimentos = conn.prepareStatement(
                            "INSERT INTO pedido_produto_alimentos (PedidoId, ProdutoId, Sequencia, ItemId, AlimentoId) VALUES (?, ?, ?, ?, ?)")) {
                        for (String alimentoId : item.getAlimentos().keySet()) {
                            pstmAlimentos.setLong(1, p.getIdCounter());
                            pstmAlimentos.setString(2, produto.getId());
                            pstmAlimentos.setInt(3, sequencia);
                            pstmAlimentos.setString(4, item.getId());
                            pstmAlimentos.setString(5, alimentoId);
                            pstmAlimentos.executeUpdate();
                        }
                    }
                } else if (produto instanceof Menu) {
                    Menu menu = (Menu) produto;
                    // Para cada item do menu, salvar seus alimentos usando o ID do Menu
                    for (Item item : menu.getItens()) {
                        try (PreparedStatement pstmAlimentos = conn.prepareStatement(
                                "INSERT INTO pedido_produto_alimentos (PedidoId, ProdutoId, Sequencia, ItemId, AlimentoId) VALUES (?, ?, ?, ?, ?)")) {
                            for (String alimentoId : item.getAlimentos().keySet()) {
                                pstmAlimentos.setLong(1, p.getIdCounter());
                                pstmAlimentos.setString(2, produto.getId()); // ProdutoId é o MENU do pedido
                                pstmAlimentos.setInt(3, sequencia);
                                pstmAlimentos.setString(4, item.getId());
                                pstmAlimentos.setString(5, alimentoId);
                                pstmAlimentos.executeUpdate();
                            }
                        }
                    }
                }
                sequencia++;
            }

            conn.commit();
        } catch (SQLException e) {
            e.printStackTrace();
            throw new NullPointerException(e.getMessage());
        }
        return res;
    }

    private boolean produtoExiste(String produtoId, Connection conn) throws SQLException {
        try (PreparedStatement pstm = conn.prepareStatement("SELECT Id FROM produtos WHERE Id=?")) {
            pstm.setString(1, produtoId);
            try (ResultSet rs = pstm.executeQuery()) {
                return rs.next();
            }
        }
    }

    @Override
    public Pedido remove(Object key) {
        Pedido p = this.get(key);
        try (Connection conn = DriverManager.getConnection(DAOconfig.URL, DAOconfig.USERNAME, DAOconfig.PASSWORD);
                PreparedStatement pstm = conn.prepareStatement("DELETE FROM pedidos WHERE Id=?")) {
            pstm.setLong(1, (Long) key);
            pstm.executeUpdate();
        } catch (Exception e) {
            e.printStackTrace();
            throw new NullPointerException(e.getMessage());
        }
        return p;
    }

    @Override
    public void putAll(Map<? extends Long, ? extends Pedido> pedidos) {
        for (Pedido p : pedidos.values()) {
            this.put(p.getIdCounter(), p);
        }
    }

    @Override
    public void clear() {
        try (Connection conn = DriverManager.getConnection(DAOconfig.URL, DAOconfig.USERNAME, DAOconfig.PASSWORD); Statement stm = conn.createStatement()) {
            // Desabilitar FK constraints para permitir TRUNCATE
            stm.executeUpdate("SET FOREIGN_KEY_CHECKS=0");

            stm.executeUpdate("TRUNCATE pedido_produto_alimentos");
            stm.executeUpdate("TRUNCATE pedido_produtos");
            stm.executeUpdate("TRUNCATE menu_itens");
            stm.executeUpdate("TRUNCATE item_trocas");
            stm.executeUpdate("TRUNCATE item_alimentos");
            stm.executeUpdate("TRUNCATE pedidos");
            stm.executeUpdate("TRUNCATE produtos");
            stm.executeUpdate("TRUNCATE alimentos");

            // Reabilitar FK constraints
            stm.executeUpdate("SET FOREIGN_KEY_CHECKS=1");
        } catch (SQLException e) {
            e.printStackTrace();
            throw new NullPointerException(e.getMessage());
        }
    }

    @Override
    public Set<Long> keySet() {
        throw new NullPointerException("Not implemented!");
    }

    @Override
    public Collection<Pedido> values() {
        Collection<Pedido> res = new HashSet<>();
        try (Connection conn = DriverManager.getConnection(DAOconfig.URL, DAOconfig.USERNAME, DAOconfig.PASSWORD);
                Statement stm = conn.createStatement();
                ResultSet rs = stm.executeQuery("SELECT Id FROM pedidos")) {
            while (rs.next()) {
                Long id = rs.getLong("Id");
                Pedido p = this.get(id);
                res.add(p);
            }
        } catch (Exception e) {
            e.printStackTrace();
            throw new NullPointerException(e.getMessage());
        }
        return res;
    }

    @Override
    public Set<Entry<Long, Pedido>> entrySet() {
        throw new NullPointerException("Not implemented!");
    }
}
