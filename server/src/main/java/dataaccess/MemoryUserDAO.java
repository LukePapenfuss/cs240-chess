package dataaccess;

import model.UserData;

import java.util.ArrayList;
import java.util.Objects;

public class MemoryUserDAO implements UserDAO {

    private ArrayList<UserData> users = new ArrayList<>();

    public void createUser(UserData userData) throws DataAccessException {
        users.add(userData);
    }

    public UserData getUser(String username) throws DataAccessException {
        // Find the UserData from the username
        for (int i = 0; i < users.size(); ++i) {
            if (Objects.equals(users.get(i).username(), username)) {
                return users.get(i);
            }
        }

        return null;
    }

    public void clear() {
        users = new ArrayList<>();
    }
}
