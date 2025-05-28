package service;

import dataAccess.AuthDAO;
import dataAccess.UserDAO;
import model.AuthData;
import model.UserData;
import dataAccess.DataAccessException;

/**
 * Service class for managing user-related operations such as registration, login, and data clearing.
 */
public class UserService {
    /** Data access object for user data operations. */
    private final UserDAO userDAO;
    /** Data access object for authentication operations. */
    private final AuthDAO authDAO;

    /**
     * Constructs a UserService with the specified user and auth DAOs.
     *
     * @param userDAO the data access object for user operations
     * @param authDAO the data access object for authentication operations
     */
    public UserService(UserDAO userDAO, AuthDAO authDAO) {
        this.userDAO = userDAO;
        this.authDAO = authDAO;
    }

    /**
     * Registers a new user and creates an authentication token.
     *
     * @param user the UserData object containing username, password, and email
     * @return the created AuthData object containing the authentication token and username
     * @throws DataAccessException if the user data is invalid or the username is already taken
     */
    public AuthData register(UserData user) throws DataAccessException {
        // Validate user input
        if (user == null || user.username() == null || user.password() == null || user.email() == null ||
                user.username().trim().isEmpty() || user.password().trim().isEmpty() || user.email().trim().isEmpty()) {
            throw new DataAccessException("Bad Request");
        }
        // Check for existing username
        if (userDAO.getUser(user.username()) != null) {
            throw new DataAccessException("already taken");
        }
        // Store new user
        userDAO.createUser(user);
        // Generate and return authentication token
        return authDAO.createAuthToken(user.username());
    }

    /**
     * Logs in a user and creates an authentication token.
     *
     * @param user the UserData object containing username and password
     * @return the created AuthData object containing the authentication token and username
     * @throws DataAccessException if the credentials are invalid or missing
     */
    public AuthData login(UserData user) throws DataAccessException {
        // Validate user input
        if (user == null || user.username() == null || user.password() == null ||
                user.username().trim().isEmpty() || user.password().trim().isEmpty()) {
            throw new DataAccessException("Bad Request");
        }
        // Retrieve stored user
        UserData storedUser = userDAO.getUser(user.username());
        // Verify user existence and password
        if (storedUser == null || !storedUser.password().equals(user.password())) {
            throw new DataAccessException("unauthorized");
        }
        // Generate and return authentication token
        return authDAO.createAuthToken(user.username());
    }

    /**
     * Clears all user and authentication data from storage.
     *
     * @throws DataAccessException if the clear operation fails
     */
    public void clearAll() throws DataAccessException {
        userDAO.clear();
        authDAO.clearAll();
    }
}