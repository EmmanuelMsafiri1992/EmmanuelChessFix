package service;
import dataAccess.DataAccessException;
import dataAccess.AuthDAO;

public class AuthService {
    private final AuthDAO auth_DAO;

    public AuthService(AuthDAO auth_DAO){
        this.auth_DAO = auth_DAO;
    }

    public void logout(String authToken) throws DataAccessException{
        auth_DAO.deleteAuthToken(authToken);
    }

    public void verifyAuth(String authToken) throws DataAccessException{
        auth_DAO.getAuthToken(authToken);
    }

    public void clear() throws DataAccessException{
        auth_DAO.clearAll();
    }
}