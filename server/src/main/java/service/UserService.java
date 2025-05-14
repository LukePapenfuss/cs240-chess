package service;
import dataaccess.*;
import service.request.*;
import model.*;

import java.util.Objects;
import java.util.UUID;

public class UserService {

    private MemoryUserDAO userDAO = new MemoryUserDAO();
    private MemoryAuthDAO authDAO = new MemoryAuthDAO();

    public RegisterResult register(RegisterRequest registerRequest) throws DataAccessException {
        UserData findUser = userDAO.getUser(registerRequest.username());

        if (findUser != null) {
            throw new DataAccessException("Error: username already taken");
        }

        UserData newUser = new UserData(registerRequest.username(), registerRequest.password(), registerRequest.email());

        userDAO.createUser(newUser);

        AuthData newAuth = new AuthData(UUID.randomUUID().toString(), registerRequest.username());

        authDAO.createAuth(newAuth);

        RegisterResult result = new RegisterResult(newAuth.username(), newAuth.authToken());

        return result;
    }

    public LoginResult login(LoginRequest loginRequest) throws DataAccessException {
        UserData findUser = userDAO.getUser(loginRequest.username());

        if (findUser == null) {
            throw new DataAccessException("Error: username does not exist");
        }

        if (!Objects.equals(findUser.password(), loginRequest.password())) {
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
            System.out.println("Logged out!");
        }

        // No return value
    }
}
