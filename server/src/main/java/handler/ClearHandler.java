package handler;

import com.google.gson.Gson;
import service.ClearService;
import service.request.ClearResult;

public class ClearHandler {

    public String run() {
        var serializer = new Gson();

        ClearResult clearResult = new ClearService().clear();

        String json = serializer.toJson(clearResult);

        return json;
    }

}
