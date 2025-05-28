package dataAccess;

public class MySQLDataAccess implements DataAccess {
    private final UserDAO userDAO;
    private final GameDAO gameDAO;
    private final AuthDAO authDAO;
    private final DatabaseManager dbManager;

    public MySQLDataAccess() throws DataAccessException {
        try {
            this.dbManager = new DatabaseManager();
            this.userDAO = new MySQLUserDAO();
            this.gameDAO = new MySQLGameDAO();
            this.authDAO = new MySQLAuthDAO(dbManager);
        } catch (DataAccessException e) {
            throw new DataAccessException("Failed to initialize database or DAOs: " + e.getMessage());
        }
    }

    @Override
    public UserDAO getUserDAO() {
        return userDAO;
    }

    @Override
    public GameDAO getGameDAO() {
        return gameDAO;
    }

    @Override
    public AuthDAO getAuthDAO() {
        return authDAO;
    }

    @Override
    public void clear() throws DataAccessException {
        userDAO.clear();
        gameDAO.clear();
        authDAO.clearAll();
    }
}