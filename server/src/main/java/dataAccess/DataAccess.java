package dataAccess;

public interface DataAccess {
    UserDAO getUserDAO();
    GameDAO getGameDAO();
    AuthDAO getAuthDAO();
    void clear() throws DataAccessException;
}