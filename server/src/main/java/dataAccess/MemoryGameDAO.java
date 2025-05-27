package dataAccess;

import model.gameData;
import chess.ChessGame;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

// In-memory implementation of gameDAO for managing game data
public class MemoryGameDAO implements gameDAO {
    // Map to store games with game ID as key and gameData as value
    private final Map<Integer, gameData> games = new HashMap<>();
    // Counter for generating unique game IDs
    private int nextGameID = 1;

    // Creates a new game with the given name
    @Override
    public gameData createGame(String gameName) throws DataAccessException {
        // Check if game name is null or empty
        if (gameName == null || gameName.trim().isEmpty()) {
            // Throw exception for invalid game name
            throw new DataAccessException("bad request");
        }
        // Create new gameData with next ID, null players, given name, and new ChessGame
        gameData game = new gameData(nextGameID++, null, null, gameName, new ChessGame());
        // Store gameData in the map with game ID as key
        games.put(game.gameID(), game);
        // Return the created gameData
        return game;
    }

    // Retrieves game data for a given game ID
    @Override
    public gameData getGame(int gameID) throws DataAccessException {
        // Get gameData from map using game ID
        gameData game = games.get(gameID);
        // Check if game exists
        if (game == null) {
            // Throw exception if game is not found
            throw new DataAccessException("Game not found");
        }
        // Return the found gameData
        return game;
    }

    // Lists all games in storage
    @Override
    public Collection<gameData> listgame() throws DataAccessException {
        // Return all gameData objects from the map
        return games.values();
    }

    // Updates an existing game
    @Override
    public void updateGame(gameData game) throws DataAccessException {
        // Check if game is null or not in map
        if (game == null || !games.containsKey(game.gameID())) {
            // Throw exception for invalid game
            throw new DataAccessException("Bad request");
        }
        // Update gameData in the map
        games.put(game.gameID(), game);
    }

    // Clears all game data from storage
    @Override
    public void clear() {
        // Remove all entries from the games map
        games.clear();
    }
}