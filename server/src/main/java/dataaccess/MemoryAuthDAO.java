package dataaccess;

import model.AuthData;

public class MemoryAuthDAO implements AuthDAO {

    public void createAuth(AuthData authData) throws DataAccessException {
        String authToken = UUID.randomUUID().toString();
    }

    public AuthData getAuth(String authToken) throws DataAccessException {
        return null;
    }

    public void deleteAuth(AuthData authData) throws DataAccessException  {

    }

}
