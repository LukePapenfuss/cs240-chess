package handler;

import com.google.gson.Gson;
import service.UserService;
import service.request.*;

public class LoginHandler {

    public String run(String request) {
        var serializer = new Gson();

        LoginRequest loginRequest = serializer.fromJson(request, LoginRequest.class);

        LoginResult loginResult = new UserService().login(loginRequest);

        String json = serializer.toJson(loginResult);

        return json;
    }

}
