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

import model.gestao.Funcionario;
import model.gestao.Perfil;

/**
 * DAO simples para Funcionários. Usa o Id (número de trabalhador) como chave primária.
 */
public class FuncionarioDAO implements Map<Long, Funcionario> {

    private static FuncionarioDAO singleton = null;

    private FuncionarioDAO() {
        try (Connection conn = DriverManager.getConnection(DAOconfig.URL, DAOconfig.USERNAME, DAOconfig.PASSWORD);
                Statement stm = conn.createStatement()) {

            String sql = "CREATE TABLE IF NOT EXISTS funcionarios ("
                    + "Id BIGINT NOT NULL PRIMARY KEY,"
                    + "Nome VARCHAR(100) NOT NULL,"
                    + "Password VARCHAR(100) NOT NULL,"
                    + "Perfil VARCHAR(20) NOT NULL)";
            stm.executeUpdate(sql);

        } catch (SQLException e) {
            e.printStackTrace();
            throw new NullPointerException(e.getMessage());
        }
    }

    public static FuncionarioDAO getInstance() {
        if (singleton == null) {
            singleton = new FuncionarioDAO();
        }
        return singleton;
    }

    @Override
    public int size() {
        int i = 0;
        try (Connection conn = DriverManager.getConnection(DAOconfig.URL, DAOconfig.USERNAME, DAOconfig.PASSWORD);
                Statement stm = conn.createStatement();
                ResultSet rs = stm.executeQuery("SELECT count(*) FROM funcionarios")) {
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
                PreparedStatement pstm = conn.prepareStatement("SELECT Id FROM funcionarios WHERE Id=?")) {
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
        if (!(value instanceof Funcionario)) {
            return false;
        }
        Funcionario f = (Funcionario) value;
        return this.containsKey(f.getId());
    }

    @Override
    public Funcionario get(Object key) {
        if (!(key instanceof Long)) {
            return null;
        }
        long id = (Long) key;
        Funcionario f = null;
        try (Connection conn = DriverManager.getConnection(DAOconfig.URL, DAOconfig.USERNAME, DAOconfig.PASSWORD);
                PreparedStatement pstm = conn
                        .prepareStatement("SELECT Nome, Password, Perfil FROM funcionarios WHERE Id=?")) {
            pstm.setLong(1, id);
            try (ResultSet rs = pstm.executeQuery()) {
                if (rs.next()) {
                    f = new Funcionario();
                    f.setId(id);
                    f.setNome(rs.getString("Nome"));
                    f.setPassword(rs.getString("Password"));
                    f.setPerfil(Perfil.valueOf(rs.getString("Perfil")));
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
            throw new NullPointerException(e.getMessage());
        }
        return f;
    }

    @Override
    public Funcionario put(Long key, Funcionario f) {
        Funcionario old = this.get(key);
        try (Connection conn = DriverManager.getConnection(DAOconfig.URL, DAOconfig.USERNAME, DAOconfig.PASSWORD);
                PreparedStatement pstm = conn.prepareStatement(
                        "INSERT INTO funcionarios (Id, Nome, Password, Perfil) VALUES (?, ?, ?, ?) "
                                + "ON DUPLICATE KEY UPDATE Nome=VALUES(Nome), Password=VALUES(Password), Perfil=VALUES(Perfil)")) {
            pstm.setLong(1, f.getId());
            pstm.setString(2, f.getNome());
            pstm.setString(3, f.getPassword());
            pstm.setString(4, f.getPerfil().toString());
            pstm.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
            throw new NullPointerException(e.getMessage());
        }
        return old;
    }

    @Override
    public Funcionario remove(Object key) {
        Funcionario old = this.get(key);
        try (Connection conn = DriverManager.getConnection(DAOconfig.URL, DAOconfig.USERNAME, DAOconfig.PASSWORD);
                PreparedStatement pstm = conn.prepareStatement("DELETE FROM funcionarios WHERE Id=?")) {
            pstm.setLong(1, (Long) key);
            pstm.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
            throw new NullPointerException(e.getMessage());
        }
        return old;
    }

    @Override
    public void putAll(Map<? extends Long, ? extends Funcionario> m) {
        for (Funcionario f : m.values()) {
            this.put(f.getId(), f);
        }
    }

    @Override
    public void clear() {
        try (Connection conn = DriverManager.getConnection(DAOconfig.URL, DAOconfig.USERNAME, DAOconfig.PASSWORD);
                Statement stm = conn.createStatement()) {
            stm.executeUpdate("TRUNCATE funcionarios");
        } catch (SQLException e) {
            e.printStackTrace();
            throw new NullPointerException(e.getMessage());
        }
    }

    @Override
    public Set<Long> keySet() {
        throw new UnsupportedOperationException("Not implemented");
    }

    @Override
    public Collection<Funcionario> values() {
        throw new UnsupportedOperationException("Not implemented");
    }

    @Override
    public Set<Entry<Long, Funcionario>> entrySet() {
        throw new UnsupportedOperationException("Not implemented");
    }
}
