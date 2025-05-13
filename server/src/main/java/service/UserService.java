package service;
import dataaccess.*;
import service.request.*;
import model.*;

import java.util.UUID;

public class UserService {

    private UserDAO userDAO = new MemoryUserDAO();
    private AuthDAO authDAO = new MemoryAuthDAO();

    public RegisterResult register(RegisterRequest registerRequest) throws DataAccessException {

        UserData findUser = userDAO.getUser(registerRequest.username());

        if (findUser != null) {
            throw new DataAccessException("AlreadyTakenException");
        }

        UserData newUser = new UserData(registerRequest.username(), registerRequest.password(), registerRequest.email());

        userDAO.createUser(newUser);

        AuthData newAuth = new AuthData(UUID.randomUUID().toString(), registerRequest.username());

        authDAO.createAuth(newAuth);

        RegisterResult result = new RegisterResult(newAuth.username(), newAuth.authToken());

        return result;
    }

    public LoginResult login(LoginRequest loginRequest) {
        return null;
    }

    public void logout(LogoutRequest logoutRequest) {
        // Just started with the service classes
    }
}
