package dataAccess;

import chess.ChessGame;
import model.GameData;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collection;

public class MySQLGameDAO implements GameDAO {
    private final Gson gson;

    public MySQLGameDAO() throws DataAccessException {
        this.gson = new GsonBuilder()
                .registerTypeAdapter(ChessGame.class, new ChessGameTypeAdapter())
                .create();
    }

    @Override
    public GameData createGame(String gameName) throws DataAccessException {
        String sql = "INSERT INTO games (gameName, game) VALUES (?, ?)";
        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql, PreparedStatement.RETURN_GENERATED_KEYS)) {
            stmt.setString(1, gameName);
            ChessGame game = new ChessGame();
            stmt.setString(2, gson.toJson(game));
            stmt.executeUpdate();
            try (ResultSet rs = stmt.getGeneratedKeys()) {
                if (rs.next()) {
                    int gameID = rs.getInt(1);
                    return new GameData(gameID, null, null, gameName, game);
                }
            }
            throw new DataAccessException("Failed to retrieve gameID");
        } catch (SQLException e) {
            throw new DataAccessException("Unable to create game: " + e.getMessage());
        }
    }

    @Override
    public GameData getGame(int gameID) throws DataAccessException {
        String sql = "SELECT gameID, whiteUsername, blackUsername, gameName, game FROM games WHERE gameID = ?";
        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, gameID);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    ChessGame game = gson.fromJson(rs.getString("game"), ChessGame.class);
                    return new GameData(
                            rs.getInt("gameID"),
                            rs.getString("whiteUsername"),
                            rs.getString("blackUsername"),
                            rs.getString("gameName"),
                            game
                    );
                }
            }
            return null;
        } catch (SQLException e) {
            throw new DataAccessException("Unable to get game: " + e.getMessage());
        }
    }

    @Override
    public Collection<GameData> listgame() throws DataAccessException {
        Collection<GameData> games = new ArrayList<>();
        String sql = "SELECT gameID, whiteUsername, blackUsername, gameName, game FROM games";
        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {
            while (rs.next()) {
                ChessGame game = gson.fromJson(rs.getString("game"), ChessGame.class);
                games.add(new GameData(
                        rs.getInt("gameID"),
                        rs.getString("whiteUsername"),
                        rs.getString("blackUsername"),
                        rs.getString("gameName"),
                        game
                ));
            }
            return games;
        } catch (SQLException e) {
            throw new DataAccessException("Unable to list games: " + e.getMessage());
        }
    }

    @Override
    public void updateGame(GameData game) throws DataAccessException {
        String sql = "UPDATE games SET whiteUsername = ?, blackUsername = ?, gameName = ?, game = ? WHERE gameID = ?";
        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, game.whiteUsername());
            stmt.setString(2, game.blackUsername());
            stmt.setString(3, game.gameName());
            stmt.setString(4, gson.toJson(game.game()));
            stmt.setInt(5, game.gameID());
            stmt.executeUpdate();
        } catch (SQLException e) {
            throw new DataAccessException("Unable to update game: " + e.getMessage());
        }
    }

    @Override
    public void clear() throws DataAccessException {
        String sql = "DELETE FROM games";
        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.executeUpdate();
        } catch (SQLException e) {
            throw new DataAccessException("Unable to clear games: " + e.getMessage());
        }
    }
}