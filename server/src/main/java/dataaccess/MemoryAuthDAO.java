package dataaccess;

import model.AuthData;
import model.UserData;

import java.util.ArrayList;

public class MemoryAuthDAO implements AuthDAO {

    private ArrayList<AuthData> auths = new ArrayList<>();

    public void createAuth(AuthData authData) throws DataAccessException {
        auths.add(authData);
    }

    public AuthData getAuth(String authToken) throws DataAccessException {
        return null;
    }

    public void deleteAuth(AuthData authData) throws DataAccessException  {

    }

}
