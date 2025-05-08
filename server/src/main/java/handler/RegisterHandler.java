package handler;

import com.google.gson.Gson;
import service.UserService;
import service.request.*;

public class RegisterHandler {

    public String run(String request) {
        var serializer = new Gson();

        RegisterRequest registerRequest = serializer.fromJson(request, RegisterRequest.class);

        RegisterResult registerResult = new UserService().register(registerRequest);

        String json = serializer.toJson(registerResult);

        return json;
    }

}
