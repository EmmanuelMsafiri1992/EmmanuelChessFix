package dataAccess;

import model.GameData;
import java.util.Collection;

// Interface defining methods for managing game data
public interface GameDAO {
    /**
     * Creates a new game with the specified name.
     *
     * @param gameName the name of the game to create
     * @return the created gameData object
     * @throws DataAccessException if the game name is invalid or creation fails
     */
    GameData createGame(String gameName) throws DataAccessException;

    /**
     * Retrieves a game by its ID.
     *
     * @param gameID the ID of the game to retrieve
     * @return the gameData object for the specified ID
     * @throws DataAccessException if the game ID is invalid or the game is not found
     */
    GameData getGame(int gameID) throws DataAccessException;

    /**
     * Lists all games in storage.
     *
     * @return a collection of all gameData objects
     * @throws DataAccessException if the operation fails
     */
    Collection<GameData> listgame() throws DataAccessException;

    /**
     * Updates an existing game with new data.
     *
     * @param game the gameData object containing updated game information
     * @throws DataAccessException if the game is invalid or not found
     */
    void updateGame(GameData game) throws DataAccessException;

    /**
     * Clears all game data from storage.
     *
     * @throws DataAccessException if the operation fails
     */
    void clear() throws DataAccessException;
}