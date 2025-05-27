package dataAccess;

/**
 * Custom exception for data access errors in the application.
 */
public class DataAccessException extends Exception {
    /**
     * Constructs a new DataAccessException with the specified error message.
     *
     * @param message the error message describing the cause of the exception
     */
    public DataAccessException(String message) {
        // Pass the message to the parent Exception class constructor
        super(message);
    }
}