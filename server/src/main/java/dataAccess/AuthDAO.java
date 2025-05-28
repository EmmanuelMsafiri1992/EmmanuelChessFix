package dataAccess;

import model.AuthData;


/**
 * Interface for managing authentication data operations.
 * Provides methods to create, retrieve, delete, and clear authentication tokens.
 */
public interface AuthDAO {
    /**
     * Creates a new authentication token for a specified username.
     *
     * @param username the username to associate with the new authentication token
     * @return the created AuthData object containing the token and username
     * @throws DataAccessException if the username is invalid or token creation fails
     */
    AuthData createAuthToken(String username) throws DataAccessException;

    /**
     * Retrieves authentication data associated with a given authentication token.
     *
     * @param authToken the authentication token to look up
     * @return the AuthData object associated with the provided token
     * @throws DataAccessException if the token is invalid or not found
     */
    AuthData getAuthToken(String authToken) throws DataAccessException;

    /**
     * Deletes a specified authentication token from storage.
     *
     * @param authToken the authentication token to remove
     * @throws DataAccessException if the token is invalid or not found
     */
    void deleteAuthToken(String authToken) throws DataAccessException;

    /**
     * Clears all authentication data from storage.
     *
     * @throws DataAccessException if the clear operation fails
     */
    void clearAll() throws DataAccessException;
}