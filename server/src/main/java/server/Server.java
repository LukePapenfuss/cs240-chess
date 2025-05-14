package server;

import com.google.gson.Gson;
import dataaccess.DataAccessException;
import handler.*;
import spark.*;

import java.util.Map;

public class Server {

    private Handler handler = new Handler();

    public int run(int desiredPort) {
        Spark.port(desiredPort);

        Spark.staticFiles.location("web");

        // Register your endpoints and handle exceptions here.

        // Register a user
        Spark.post("/user", (req, res) -> {
            try {
                String result = handler.register(req.body());

                res.type("application/json");
                return result;
            } catch (DataAccessException e) {
                var serializer = new Gson();

                res.status(403);
                return serializer.toJson(Map.of("message", e.getMessage()));
            }
        });

        // Login
        Spark.post("/session", (req, res) -> {
            try {
                String result = handler.login(req.body());

                res.type("application/json");
                return result;
            } catch (DataAccessException e) {
                var serializer = new Gson();

                res.status(403);
                return serializer.toJson(Map.of("message", e.getMessage()));
            }
        });

        //This line initializes the server and can be removed once you have a functioning endpoint 
        Spark.init();

        Spark.awaitInitialization();
        return Spark.port();
    }

    public void stop() {
        Spark.stop();
        Spark.awaitStop();
    }
}
