package dataaccess;

import model.AuthData;
import model.UserData;

import java.util.ArrayList;
import java.util.Objects;

public class MemoryAuthDAO implements AuthDAO {

    private ArrayList<AuthData> auths = new ArrayList<>();

    public void createAuth(AuthData authData) throws DataAccessException {
        auths.add(authData);
    }

    public AuthData getAuth(String authToken) throws DataAccessException {
        // Find the AuthData from the authToken
        for (int i = 0; i < auths.size(); ++i) {
            if (Objects.equals(auths.get(i).authToken(), authToken)) {
                return auths.get(i);
            }
        }

        return null;
    }

    public void deleteAuth(AuthData authData) throws DataAccessException  {
        // Remove all data that match the authToken
        for (int i = auths.size() - 1; i >= 0; --i) {
            if (Objects.equals(auths.get(i).authToken(), authData.authToken())) {
                auths.remove(i);
            }
        }
    }

    public String getUsername(String authToken) throws DataAccessException {
        for (int i = 0; i < auths.size(); ++i) {
            if (Objects.equals(auths.get(i).authToken(), authToken)) {
                return auths.get(i).username();
            }
        }

        return null;
    }

}
