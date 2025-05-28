package dataAccess;

import model.UserData;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

// In-memory implementation of userDAO for managing user data
public class MemoryUserDAO implements UserDAO {
    // Map to store users with username as key and userData as value
    private final Map<String, UserData> users = new HashMap<>();

    // Creates a new user in storage
    @Override
    public void createUser(UserData user) throws DataAccessException {
        // Check if user or username is null
        if (user == null || user.username() == null) {
            // Throw exception for invalid user
            throw new DataAccessException("bad request");
        }
        // Check if username already exists
        if (users.containsKey(user.username())) {
            // Throw exception if username is taken
            throw new DataAccessException("already exists");
        }
        // Store userData in the map with username as key
        users.put(user.username(), user);
    }

    // Retrieves user data for a given username
    @Override
    public UserData getUser(String username) throws DataAccessException {
        // Get userData from map using username
        UserData user = users.get(username);
        // Check if username is null
        if (username == null) {
            // Throw exception for null username
            throw new DataAccessException("bad request");
        }
        // Return userData (may be null if not found)
        return user;
    }

    // Clears all user data from storage
    @Override
    public void clear() {
        // Remove all entries from the users map
        users.clear();
    }

    // Returns all users in storage
    public Collection<UserData> getAllUsers() {
        // Return all userData objects from the map
        return users.values();
    }
}