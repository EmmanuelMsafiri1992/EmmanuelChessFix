package dataAccess;

import model.userData;

// Interface defining methods for managing user data
public interface userDAO {
    /**
     * Creates a new user in storage.
     *
     * @param user the userData object containing user information
     * @throws DataAccessException if the user is invalid or creation fails
     */
    void createUser(userData user) throws DataAccessException;

    /**
     * Retrieves user data for a given username.
     *
     * @param username the username to look up
     * @return the userData object associated with the username, or null if not found
     * @throws DataAccessException if the username is invalid
     */
    userData getUser(String username) throws DataAccessException;

    /**
     * Clears all user data from storage.
     *
     * @throws DataAccessException if the operation fails
     */
    void clear() throws DataAccessException;
}