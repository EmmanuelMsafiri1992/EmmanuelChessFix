package dataAccess;

import model.AuthData;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * In-memory implementation of the AuthDAO interface for managing authentication data.
 * Stores authentication tokens and associated data in a HashMap.
 */
public class MemoryAuthDAO implements AuthDAO {
    /** Map to store authentication tokens and their associated AuthData objects. */
    private final Map<String, AuthData> auths = new HashMap<>();

    /**
     * Creates a new authentication token for the specified username.
     *
     * @param username the username to associate with the new authentication token
     * @return the created AuthData object containing the token and username
     * @throws DataAccessException if the username is null or empty
     */
    @Override
    public AuthData createAuthToken(String username) throws DataAccessException {
        if (username == null || username.trim().isEmpty()) {
            throw new DataAccessException("bad request");
        }
        // Generate a unique authentication token
        String authToken = UUID.randomUUID().toString();
        AuthData authData = new AuthData(authToken, username);
        auths.put(authToken, authData);
        return authData;
    }

    /**
     * Retrieves authentication data for the specified authentication token.
     *
     * @param authToken the authentication token to look up
     * @return the AuthData object associated with the token
     * @throws DataAccessException if the token is null or not found
     */
    @Override
    public AuthData getAuthToken(String authToken) throws DataAccessException {
        if (authToken == null) {
            throw new DataAccessException("unauthorized");
        }
        AuthData authData = auths.get(authToken);
        if (authData == null) {
            throw new DataAccessException("unauthorized");
        }
        return authData;
    }

    /**
     * Deletes the specified authentication token from storage.
     *
     * @param authToken the authentication token to remove
     * @throws DataAccessException if the token is null or not found
     */
    @Override
    public void deleteAuthToken(String authToken) throws DataAccessException {
        if (authToken == null || !auths.containsKey(authToken)) {
            throw new DataAccessException("unauthorized");
        }
        auths.remove(authToken);
    }

    /**
     * Clears all authentication data from storage.
     *
     * @throws DataAccessException if the clear operation fails
     */
    @Override
    public void clearAll() throws DataAccessException {
        auths.clear();
    }
}