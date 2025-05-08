package handler;

import com.google.gson.Gson;
import service.GameService;
import service.request.*;

public class ListHandler {

    public String run(String request) {
        var serializer = new Gson();

        ListRequest listRequest = serializer.fromJson(request, ListRequest.class);

        ListResult listResult = new GameService().list(listRequest);

        String json = serializer.toJson(listResult);

        return json;
    }

}
