package handler;

import com.google.gson.Gson;
import dataaccess.DataAccessException;
import request.*;
import service.*;

public class Handler {

    UserService userService;
    GameService gameService;

    public Handler() {
        gameService = new GameService();
        userService = new UserService();
    }

    public String register(String request) throws DataAccessException {
        var serializer = new Gson();

        RegisterRequest registerRequest = serializer.fromJson(request, RegisterRequest.class);

        RegisterResult registerResult = userService.register(registerRequest);

        String json = serializer.toJson(registerResult);

        return json;
    }

    public String logout(String request) throws DataAccessException {
        var serializer = new Gson();

        LogoutRequest logoutRequest = new LogoutRequest(request);

        userService.logout(logoutRequest);

        return "{}";
    }

    public String login(String request) throws DataAccessException {
        var serializer = new Gson();

        LoginRequest loginRequest = serializer.fromJson(request, LoginRequest.class);

        LoginResult loginResult = userService.login(loginRequest);

        String json = serializer.toJson(loginResult);

        return json;
    }

    public String authorize(String request) throws DataAccessException {
        if(userService.authorize(request) != null) {
            return userService.authorize(request);
        }

        throw new DataAccessException("Error: unauthorized");
    }

    public String list(String request) throws DataAccessException {
        var serializer = new Gson();

        ListRequest listRequest = new ListRequest(request);

        ListResult listResult = gameService.list(listRequest);

        String json = serializer.toJson(listResult);

        return json;
    }

    public String join(String authToken, String request) throws DataAccessException {
        var serializer = new Gson();

        JoinRequest joinRequest = serializer.fromJson(request, JoinRequest.class);

        JoinResult joinResult = gameService.join(userService.getUsername(authToken), joinRequest);

        String json = serializer.toJson(joinResult);

        return json;
    }

    public String updateGame(String request) throws DataAccessException {
        var serializer = new Gson();

        UpdateRequest updateRequest = serializer.fromJson(request, UpdateRequest.class);

        gameService.updateGame(updateRequest);

        String json = serializer.toJson(null);

        return json;
    }

    public String create(String request) throws DataAccessException {
        var serializer = new Gson();

        CreateRequest createRequest = serializer.fromJson(request, CreateRequest.class);

        CreateResult createResult = gameService.create(createRequest);

        String json = serializer.toJson(createResult);

        return json;
    }

    public String clear() throws DataAccessException {
        var serializer = new Gson();

        userService.clear();
        gameService.clear();

        ClearResult clearResult = new ClearResult();

        String json = serializer.toJson(clearResult);

        return json;
    }

}
