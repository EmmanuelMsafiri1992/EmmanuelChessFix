package dataAccess;

import model.authData;

// Interface defining methods for managing authentication data
public interface authDAO {
    /**
     * Creates a new authentication token for a given username.
     *
     * @param username the username to associate with the auth token
     * @return the created authData object containing the token and username
     * @throws DataAccessException if the username is invalid or creation fails
     */
    authData createAuth(String username) throws DataAccessException;

    /**
     * Retrieves authentication data for a given auth token.
     *
     * @param authToken the authentication token to look up
     * @return the authData object associated with the token
     * @throws DataAccessException if the token is invalid or not found
     */
    authData getAuth(String authToken) throws DataAccessException;

    /**
     * Deletes an authentication token from storage.
     *
     * @param authToken the authentication token to delete
     * @throws DataAccessException if the token is invalid or not found
     */
    void deleteAuth(String authToken) throws DataAccessException;

    /**
     * Clears all authentication data from storage.
     *
     * @throws DataAccessException if the operation fails
     */
    void clear() throws DataAccessException;
}