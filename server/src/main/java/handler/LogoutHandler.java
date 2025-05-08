package handler;

import com.google.gson.Gson;
import service.UserService;
import service.request.*;

public class LogoutHandler {

    public String run(String request) {
        var serializer = new Gson();

        LogoutRequest logoutRequest = serializer.fromJson(request, LogoutRequest.class);

        new UserService().logout(logoutRequest);

        return "{}";
    }

}
