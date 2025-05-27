package service;

import dataAccess.authDAO;
import dataAccess.gameDAO;
import model.gameData;
import dataAccess.DataAccessException;
import java.util.Collection;

// Service class for managing game-related operations
public class gameService {
    // Data access objects for games and authentication
    private final gameDAO game_DAO;
    private final authDAO auth_DAO;

    // Constructor initializes game and auth DAOs
    public gameService(gameDAO game_DAO, authDAO auth_DAO) {
        // Store gameDAO for game data operations
        this.game_DAO = game_DAO;
        // Store authDAO for authentication checks
        this.auth_DAO = auth_DAO;
    }

    // Creates a new game with the given auth token and name
    public gameData createGame(String authToken, String gameName) throws DataAccessException {
        // Verify auth token exists
        auth_DAO.getAuth(authToken);
        // Check if auth token is null or invalid
        if (authToken == null || auth_DAO.getAuth(authToken) == null) {
            // Throw exception for unauthorized access
            throw new DataAccessException("unauthorized");
        }
        // Create and return new game using gameDAO
        return game_DAO.createGame(gameName);
    }

    // Lists all games for a valid auth token
    public Collection<gameData> listGames(String authToken) throws DataAccessException {
        // Check if auth token is null or invalid
        if (authToken == null || auth_DAO.getAuth(authToken) == null) {
            // Throw exception for unauthorized access
            throw new DataAccessException("unauthorized");
        }
        // Return all games from gameDAO
        return game_DAO.listgame();
    }

    // Joins a user to a game with the specified color
    public void joinGame(String authToken, int gameID, String playerColor) throws DataAccessException {
        // Check if auth token is null or invalid
        if (authToken == null || auth_DAO.getAuth(authToken) == null) {
            // Throw exception for unauthorized access
            throw new DataAccessException("unauthorized");
        }
        // Check if player color is null or invalid (not WHITE or BLACK)
        if (playerColor == null || (!playerColor.equalsIgnoreCase("WHITE") && !playerColor.equalsIgnoreCase("BLACK"))) {
            // Throw exception for invalid player color
            throw new DataAccessException("bad request");
        }
        // Get game data by game ID
        gameData game = game_DAO.getGame(gameID);
        // Get username from auth token
        String username = auth_DAO.getAuth(authToken).username();
        // Check if white player slot is taken
        if (playerColor.equalsIgnoreCase("WHITE") && game.whiteUsername() != null) {
            // Throw exception if white slot is occupied
            throw new DataAccessException("Player color required");
        }
        // Check if black player slot is taken
        if (playerColor.equalsIgnoreCase("BLACK") && game.blackUsername() != null) {
            // Throw exception if black slot is occupied
            throw new DataAccessException("already exists");
        }
        // Declare variable for updated game data
        gameData updatedGame;
        // Assign user to white player slot if requested
        if (playerColor.equalsIgnoreCase("WHITE")) {
            // Create new gameData with user as white player
            updatedGame = new gameData(game.gameID(), username, game.blackUsername(), game.gameName(), game.game());
        } else {
            // Create new gameData with user as black player
            updatedGame = new gameData(game.gameID(), game.whiteUsername(), username, game.gameName(), game.game());
        }
        // Update game data in storage
        game_DAO.updateGame(updatedGame);
    }

    // Clears all game data
    public void clear() throws DataAccessException {
        // Clear all games using gameDAO
        game_DAO.clear();
    }
}