package service;

import dataAccess.authDAO;
import dataAccess.userDAO;
import model.authData;
import model.userData;
import dataAccess.DataAccessException;

// Service class for managing user-related operations
public class userService {
    // Data access objects for users and authentication
    private final userDAO user_DAO;
    private final authDAO auth_DAO;

    // Constructor initializes user and auth DAOs
    public userService(userDAO user_DAO, authDAO auth_DAO) {
        // Store userDAO for user data operations
        this.user_DAO = user_DAO;
        // Store authDAO for authentication operations
        this.auth_DAO = auth_DAO;
    }

    // Registers a new user and creates an auth token
    public authData register(userData user) throws DataAccessException {
        // Check if user or any required fields are null or empty
        if (user == null || user.username() == null || user.password() == null || user.email() == null ||
                user.username().trim().isEmpty() || user.password().trim().isEmpty() || user.email().trim().isEmpty()) {
            // Throw exception for invalid input
            throw new DataAccessException("Bad Request");
        }
        // Check if username is already taken
        if (user_DAO.getUser(user.username()) != null) {
            // Throw exception if username exists
            throw new DataAccessException("already taken");
        }
        // Create user in storage
        user_DAO.createUser(user);
        // Create and return auth token for the user
        return auth_DAO.createAuth(user.username());
    }

    // Logs in a user and creates an auth token
    public authData login(userData user) throws DataAccessException {
        // Check if user or required fields are null or empty
        if (user == null || user.username() == null || user.password() == null ||
                user.username().trim().isEmpty() || user.password().trim().isEmpty()) {
            // Throw exception for invalid input
            throw new DataAccessException("Bad Request");
        }
        // Get stored user data by username
        userData storedUser = user_DAO.getUser(user.username());
        // Check if user exists and password matches
        if (storedUser == null || !storedUser.password().equals(user.password())) {
            // Throw exception for invalid credentials
            throw new DataAccessException("unauthorized");
        }
        // Create and return auth token for the user
        return auth_DAO.createAuth(user.username());
    }

    // Clears all user and auth data
    public void clear() throws DataAccessException {
        // Clear all users from userDAO
        user_DAO.clear();
        // Clear all auth tokens from authDAO
        auth_DAO.clear();
    }
}