package handler;

import com.google.gson.Gson;
import dataaccess.DataAccessException;
import model.UserData;
import service.UserService;
import service.request.*;

public class RegisterHandler {

    private UserService userService = new UserService();

    public String run(String request) throws DataAccessException {
        var serializer = new Gson();

        RegisterRequest registerRequest = serializer.fromJson(request, RegisterRequest.class);

        RegisterResult registerResult = userService.register(registerRequest);

        String json = serializer.toJson(registerResult);

        return json;
    }

}
