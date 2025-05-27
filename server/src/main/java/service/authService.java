package service;
import dataAccess.DataAccessException;
import dataAccess.authDAO;

public class authService{
    private final authDAO auth_DAO;

    public authService(authDAO auth_DAO){
        this.auth_DAO = auth_DAO;
    }

    public void logout(String authToken) throws DataAccessException{
        auth_DAO.deleteAuth(authToken);
    }

    public void verifyAuth(String authToken) throws DataAccessException{
        auth_DAO.getAuth(authToken);
    }

    public void clear() throws DataAccessException{
        auth_DAO.clear();
    }
}