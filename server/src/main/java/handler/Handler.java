package handler;

import com.google.gson.Gson;
import dataaccess.DataAccessException;
import service.*;
import service.request.*;

public class Handler {

    UserService userService = new UserService();
    GameService gameService = new GameService();
    ClearService clearService = new ClearService();

    public String register(String request) throws DataAccessException {
        var serializer = new Gson();

        RegisterRequest registerRequest = serializer.fromJson(request, RegisterRequest.class);

        RegisterResult registerResult = userService.register(registerRequest);

        String json = serializer.toJson(registerResult);

        return json;
    }

    public String logout(String request) {
        var serializer = new Gson();

        LogoutRequest logoutRequest = serializer.fromJson(request, LogoutRequest.class);

        new UserService().logout(logoutRequest);

        return "{}";
    }

    public String login(String request) {
        var serializer = new Gson();

        LoginRequest loginRequest = serializer.fromJson(request, LoginRequest.class);

        LoginResult loginResult = new UserService().login(loginRequest);

        String json = serializer.toJson(loginResult);

        return json;
    }

    public String list(String request) {
        var serializer = new Gson();

        ListRequest listRequest = serializer.fromJson(request, ListRequest.class);

        ListResult listResult = new GameService().list(listRequest);

        String json = serializer.toJson(listResult);

        return json;
    }

    public String join(String request) {
        var serializer = new Gson();

        JoinRequest joinRequest = serializer.fromJson(request, JoinRequest.class);

        JoinResult joinResult = new GameService().join(joinRequest);

        String json = serializer.toJson(joinResult);

        return json;
    }

    public String create(String request) {
        var serializer = new Gson();

        CreateRequest createRequest = serializer.fromJson(request, CreateRequest.class);

        CreateResult createResult = new GameService().create(createRequest);

        String json = serializer.toJson(createResult);

        return json;
    }

    public String clear() {
        var serializer = new Gson();

        ClearResult clearResult = new ClearService().clear();

        String json = serializer.toJson(clearResult);

        return json;
    }

}
