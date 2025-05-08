package handler;

import com.google.gson.Gson;
import service.GameService;
import service.request.*;

public class CreateHandler {

    public String run(String request) {
        var serializer = new Gson();

        CreateRequest createRequest = serializer.fromJson(request, CreateRequest.class);

        CreateResult createResult = new GameService().create(createRequest);

        String json = serializer.toJson(createResult);

        return json;
    }

}
