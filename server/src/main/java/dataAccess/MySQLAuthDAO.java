package dataAccess;

import model.AuthData;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.UUID;

public class MySQLAuthDAO implements AuthDAO {
    private final DatabaseManager dbManager;

    public MySQLAuthDAO() throws DataAccessException {
        try {
            this.dbManager = new DatabaseManager();
        } catch (SQLException e) {
            throw new DataAccessException("Unable to initialize database: " + e.getMessage());
        }
    }

    public MySQLAuthDAO(DatabaseManager dbManager) throws DataAccessException {
        this.dbManager = dbManager;
    }

    @Override
    public AuthData createAuthToken(String username) throws DataAccessException {
        String authToken = UUID.randomUUID().toString();
        String sql = "INSERT INTO authTokens (authToken, username) VALUES (?, ?)";
        try (Connection conn = dbManager.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, authToken);
            stmt.setString(2, username);
            stmt.executeUpdate();
            return new AuthData(authToken, username);
        } catch (SQLException e) {
            throw new DataAccessException("Unable to create auth token: " + e.getMessage());
        }
    }

    @Override
    public AuthData getAuthToken(String authToken) throws DataAccessException {
        String sql = "SELECT authToken, username FROM authTokens WHERE authToken = ?";
        try (Connection conn = dbManager.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, authToken);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return new AuthData(
                            rs.getString("authToken"),
                            rs.getString("username")
                    );
                }
            }
            return null;
        } catch (SQLException e) {
            throw new DataAccessException("Unable to get auth: " + e.getMessage());
        }
    }

    @Override
    public void deleteAuthToken(String authToken) throws DataAccessException {
        String sql = "DELETE FROM authTokens WHERE authToken = ?";
        try (Connection conn = dbManager.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, authToken);
            stmt.executeUpdate();
        } catch (SQLException e) {
            throw new DataAccessException("Unable to delete auth: " + e.getMessage());
        }
    }

    @Override
    public void clearAll() throws DataAccessException {
        String sql = "DELETE FROM authTokens";
        try (Connection conn = dbManager.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.executeUpdate();
        } catch (SQLException e) {
            throw new DataAccessException("Unable to clear authTokens: " + e.getMessage());
        }
    }
}