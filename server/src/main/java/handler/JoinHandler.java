package handler;

import com.google.gson.Gson;
import service.GameService;
import service.request.*;

public class JoinHandler {

    public String run(String request) {
        var serializer = new Gson();

        JoinRequest joinRequest = serializer.fromJson(request, JoinRequest.class);

        JoinResult joinResult = new GameService().join(joinRequest);

        String json = serializer.toJson(joinResult);

        return json;
    }

}
