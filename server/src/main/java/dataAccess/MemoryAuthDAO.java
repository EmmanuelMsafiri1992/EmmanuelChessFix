package dataAccess;

import model.authData;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

// In-memory implementation of authDAO for managing authentication data
public class MemoryAuthDAO implements authDAO {
    // Map to store auth tokens and their associated authData
    private final Map<String, authData> auths = new HashMap<>();

    // Creates a new auth token for a given username
    @Override
    public authData createAuth(String username) throws DataAccessException {
        // Check if username is null or empty
        if (username == null || username.trim().isEmpty()) {
            // Throw exception for invalid username
            throw new DataAccessException("bad request");
        }
        // Generate a unique auth token using UUID
        String authToken = UUID.randomUUID().toString();
        // Create new authData object with token and username
        authData auth = new authData(authToken, username);
        // Store authData in the map with token as key
        auths.put(authToken, auth);
        // Return the created authData
        return auth;
    }

    // Retrieves auth data for a given auth token
    @Override
    public authData getAuth(String authToken) throws DataAccessException {
        // Check if auth token is null
        if (authToken == null) {
            // Throw exception for null token
            throw new DataAccessException("unauthorized");
        }
        // Get authData from map using token
        authData auth = auths.get(authToken);
        // Check if authData exists
        if (auth == null) {
            // Throw exception if token is invalid
            throw new DataAccessException("unauthorized");
        }
        // Return the found authData
        return auth;
    }

    // Deletes an auth token from storage
    @Override
    public void deleteAuth(String authToken) throws DataAccessException {
        // Check if token is null or not in map
        if (authToken == null || !auths.containsKey(authToken)) {
            // Throw exception for invalid token
            throw new DataAccessException("unauthorized");
        }
        // Remove authData from map
        auths.remove(authToken);
    }

    // Clears all auth data from storage
    @Override
    public void clear() {
        // Remove all entries from the map
        auths.clear();
    }
}