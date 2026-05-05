package data;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Collection;
import java.util.Map;
import java.util.Set;
import java.util.ArrayList;

import model.preparacoes.Posto;
import model.preparacoes.TipoPosto;

/**
 * DAO para Postos. Implementa Map<String, Posto> persistido em BD.
 */
public class PostoDAO implements Map<String, Posto> {

    private static PostoDAO singleton = null;

    private PostoDAO() {
        try (Connection conn = DriverManager.getConnection(DAOconfig.URL, DAOconfig.USERNAME, DAOconfig.PASSWORD); Statement stm = conn.createStatement()) {

            // Tabela Postos
            String sql = "CREATE TABLE IF NOT EXISTS postos ("
                    + "Id VARCHAR(50) NOT NULL PRIMARY KEY,"
                    + "FuncionarioId BIGINT DEFAULT NULL,"
                    + "Tipo VARCHAR(30) DEFAULT 'CAIXA'"
                    + ")";
            stm.executeUpdate(sql);

            // Garantir colunas existentes em esquemas antigos
            try {
                stm.executeUpdate("ALTER TABLE postos ADD COLUMN IF NOT EXISTS FuncionarioId BIGINT DEFAULT NULL");
                stm.executeUpdate("ALTER TABLE postos ADD COLUMN IF NOT EXISTS Tipo VARCHAR(30) DEFAULT 'CAIXA'");
            } catch (SQLException ignore) {
                // Coluna já existe ou ALTER não suportado
            }

            // Tabela Posto_Alimentos
            sql = "CREATE TABLE IF NOT EXISTS posto_alimentos ("
                    + "PostoId VARCHAR(50) NOT NULL,"
                    + "AlimentoId VARCHAR(50) NOT NULL,"
                    + "Quantidade INT DEFAULT 0,"
                    + "PRIMARY KEY(PostoId, AlimentoId),"
                    + "FOREIGN KEY(PostoId) REFERENCES postos(Id) ON DELETE CASCADE,"
                    + "FOREIGN KEY(AlimentoId) REFERENCES alimentos(Id) ON DELETE CASCADE"
                    + ")";
            stm.executeUpdate(sql);

        } catch (SQLException e) {
            e.printStackTrace();
            throw new NullPointerException(e.getMessage());
        }
    }

    public static PostoDAO getInstance() {
        if (singleton == null) {
            singleton = new PostoDAO();
        }
        return singleton;
    }

    @Override
    public int size() {
        int i = 0;
        try (Connection conn = DriverManager.getConnection(DAOconfig.URL, DAOconfig.USERNAME, DAOconfig.PASSWORD); Statement stm = conn.createStatement(); ResultSet rs = stm.executeQuery("SELECT count(*) FROM postos")) {
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
        try (Connection conn = DriverManager.getConnection(DAOconfig.URL, DAOconfig.USERNAME, DAOconfig.PASSWORD); PreparedStatement pstm = conn.prepareStatement("SELECT Id FROM postos WHERE Id=?")) {
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
        Posto p = (Posto) value;
        return this.containsKey(p.getId());
    }

    @Override
    public Posto get(Object key) {
        if (!(key instanceof String)) {
            return null;
        }
        String postoId = (String) key;
        Posto posto = null;
        try (Connection conn = DriverManager.getConnection(DAOconfig.URL, DAOconfig.USERNAME, DAOconfig.PASSWORD)) {
            // Verifica se o posto existe
            try (PreparedStatement pstm = conn.prepareStatement("SELECT Id FROM postos WHERE Id=?")) {
                pstm.setString(1, postoId);
                try (ResultSet rs = pstm.executeQuery()) {
                    if (!rs.next()) {
                        return null;
                    }
                }
            }

            posto = new Posto(postoId);
            posto.setFuncionarioId(rsFuncionario(conn, postoId));
            posto.setTipo(rsTipo(conn, postoId));

            // Carrega quantidades por alimento
            try (PreparedStatement pstm = conn.prepareStatement(
                    "SELECT AlimentoId, Quantidade FROM posto_alimentos WHERE PostoId=?")) {
                pstm.setString(1, postoId);
                try (ResultSet rs = pstm.executeQuery()) {
                    Map<String, Integer> stock = posto.getQuantidadeAlimento();
                    while (rs.next()) {
                        stock.put(rs.getString("AlimentoId"), rs.getInt("Quantidade"));
                    }
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
            throw new NullPointerException(e.getMessage());
        }
        return posto;
    }

    @Override
    public Posto put(String key, Posto posto) {
        Posto old = this.get(key);
        try (Connection conn = DriverManager.getConnection(DAOconfig.URL, DAOconfig.USERNAME, DAOconfig.PASSWORD)) {
            conn.setAutoCommit(false);

            try (PreparedStatement pstm = conn.prepareStatement(
                    "INSERT INTO postos (Id, FuncionarioId, Tipo) VALUES (?, ?, ?) "
                            + "ON DUPLICATE KEY UPDATE FuncionarioId=VALUES(FuncionarioId), Tipo=VALUES(Tipo)")) {
                pstm.setString(1, posto.getId());
                if (posto.getFuncionarioId() == null) {
                    pstm.setNull(2, java.sql.Types.BIGINT);
                } else {
                    pstm.setLong(2, posto.getFuncionarioId());
                }
                pstm.setString(3, posto.getTipo() != null ? posto.getTipo().name() : TipoPosto.CAIXA.name());
                pstm.executeUpdate();
            }

            try (PreparedStatement del = conn.prepareStatement("DELETE FROM posto_alimentos WHERE PostoId=?")) {
                del.setString(1, posto.getId());
                del.executeUpdate();
            }

            if (posto.getQuantidadeAlimento() != null && !posto.getQuantidadeAlimento().isEmpty()) {
                try (PreparedStatement ins = conn.prepareStatement(
                        "INSERT INTO posto_alimentos (PostoId, AlimentoId, Quantidade) VALUES (?, ?, ?)")) {
                    for (Map.Entry<String, Integer> e : posto.getQuantidadeAlimento().entrySet()) {
                        ins.setString(1, posto.getId());
                        ins.setString(2, e.getKey());
                        ins.setInt(3, e.getValue());
                        ins.addBatch();
                    }
                    ins.executeBatch();
                }
            }

            conn.commit();
        } catch (SQLException e) {
            e.printStackTrace();
            throw new NullPointerException(e.getMessage());
        }
        return old;
    }

    private Long rsFuncionario(Connection conn, String postoId) throws SQLException {
        try (PreparedStatement pstm = conn.prepareStatement("SELECT FuncionarioId FROM postos WHERE Id=?")) {
            pstm.setString(1, postoId);
            try (ResultSet rs = pstm.executeQuery()) {
                if (rs.next()) {
                    long id = rs.getLong("FuncionarioId");
                    return rs.wasNull() ? null : id;
                }
            }
        }
        return null;
    }

    private TipoPosto rsTipo(Connection conn, String postoId) throws SQLException {
        try (PreparedStatement pstm = conn.prepareStatement("SELECT Tipo FROM postos WHERE Id=?")) {
            pstm.setString(1, postoId);
            try (ResultSet rs = pstm.executeQuery()) {
                if (rs.next()) {
                    String tipo = rs.getString("Tipo");
                    try {
                        return TipoPosto.valueOf(tipo);
                    } catch (IllegalArgumentException e) {
                        return TipoPosto.CAIXA;
                    }
                }
            }
        }
        return TipoPosto.CAIXA;
    }

    @Override
    public Posto remove(Object key) {
        Posto old = this.get(key);
        try (Connection conn = DriverManager.getConnection(DAOconfig.URL, DAOconfig.USERNAME, DAOconfig.PASSWORD)) {
            try (PreparedStatement pstm = conn.prepareStatement("DELETE FROM posto_alimentos WHERE PostoId=?")) {
                pstm.setString(1, (String) key);
                pstm.executeUpdate();
            }
            try (PreparedStatement pstm = conn.prepareStatement("DELETE FROM postos WHERE Id=?")) {
                pstm.setString(1, (String) key);
                pstm.executeUpdate();
            }
        } catch (SQLException e) {
            e.printStackTrace();
            throw new NullPointerException(e.getMessage());
        }
        return old;
    }

    @Override
    public void putAll(Map<? extends String, ? extends Posto> m) {
        for (Posto p : m.values()) {
            this.put(p.getId(), p);
        }
    }

    @Override
public void clear() {
    try (Connection conn = DriverManager.getConnection(DAOconfig.URL, DAOconfig.USERNAME, DAOconfig.PASSWORD);
         Statement stm = conn.createStatement()) {
        stm.executeUpdate("DELETE FROM posto_alimentos");
        stm.executeUpdate("DELETE FROM postos");
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
    public Collection<Posto> values() {
        Collection<Posto> res = new ArrayList<>();
        try (Connection conn = DriverManager.getConnection(DAOconfig.URL, DAOconfig.USERNAME, DAOconfig.PASSWORD);
                Statement stm = conn.createStatement();
                ResultSet rs = stm.executeQuery("SELECT Id FROM postos")) {
            while (rs.next()) {
                String id = rs.getString("Id");
                Posto p = this.get(id);
                if (p != null) {
                    res.add(p);
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
            throw new NullPointerException(e.getMessage());
        }
        return res;
    }

    @Override
    public Set<Entry<String, Posto>> entrySet() {
        throw new UnsupportedOperationException("Not implemented");
    }
}
