package service;
import dataaccess.*;
import org.mindrot.jbcrypt.BCrypt;
import service.request.*;
import model.*;

import java.util.Objects;
import java.util.UUID;

public class UserService {

    private UserDAO userDAO;

    {
        try {
            userDAO = new MySQLUserDAO();
        } catch (DataAccessException e) {
            throw new RuntimeException(e);
        }
    }

    private AuthDAO authDAO;

    {
        try {
            authDAO = new MySQLAuthDAO();
        } catch (DataAccessException e) {
            throw new RuntimeException(e);
        }
    }

    public RegisterResult register(RegisterRequest registerRequest) throws DataAccessException {
        UserData findUser = userDAO.getUser(registerRequest.username());

        if (registerRequest.username() == null || registerRequest.password() == null || registerRequest.email() == null) {
            throw new DataAccessException("Error: bad request");
        }

        if (findUser != null) {
            throw new DataAccessException("Error: already taken");
        }

        UserData newUser = new UserData(registerRequest.username(), registerRequest.password(), registerRequest.email());

        userDAO.createUser(newUser);

        AuthData newAuth = new AuthData(UUID.randomUUID().toString(), registerRequest.username());

        authDAO.createAuth(newAuth);

        RegisterResult result = new RegisterResult(newAuth.username(), newAuth.authToken());

        return result;
    }

    public LoginResult login(LoginRequest loginRequest) throws DataAccessException {
        if (loginRequest.username() == null || loginRequest.password() == null) {
            throw new DataAccessException("Error: bad request");
        }

        UserData findUser = userDAO.getUser(loginRequest.username());

        if (findUser == null) {
            throw new DataAccessException("Error: unauthorized");
        }

        if (!BCrypt.checkpw(loginRequest.password(), findUser.password())) {
            throw new DataAccessException("Error: unauthorized");
        } else {
            AuthData newAuth = new AuthData(UUID.randomUUID().toString(), loginRequest.username());

            authDAO.createAuth(newAuth);

            LoginResult result = new LoginResult(newAuth.username(), newAuth.authToken());

            return result;
        }
    }

    public void logout(LogoutRequest logoutRequest) throws DataAccessException {
        AuthData authData = authDAO.getAuth(logoutRequest.authToken());

        if (authData == null) {
            throw new DataAccessException("Error: unauthorized");
        } else {
            authDAO.deleteAuth(authData);
        }

        // No return value
    }

    public boolean authorize(String authToken) throws DataAccessException {
        return authDAO.getAuth(authToken) != null;
    }

    public String getUsername(String authToken) throws DataAccessException {
        return authDAO.getAuth(authToken).username();
    }

    public void clear() throws DataAccessException {
        userDAO.clear();
        authDAO.clear();
    }
}
