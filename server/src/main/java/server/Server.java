package server;

import com.google.gson.Gson;
import dataaccess.DataAccessException;
import handler.*;
import service.request.*;
import spark.*;

import java.util.Map;

public class Server {

    private Handler handler = new Handler();

    public int run(int desiredPort) {
        Spark.port(desiredPort);

        Spark.staticFiles.location("web");

        // Run All Endpoints
        registerEndpoint();

        loginEndpoint();

        logoutEndpoint();

        createEndpoint();

        listEndpoint();

        joinEndpoint();

        clearEndpoint();

        //This line initializes the server and can be removed once you have a functioning endpoint 
        Spark.init();

        Spark.awaitInitialization();
        return Spark.port();
    }

    public void stop() {
        Spark.stop();
        Spark.awaitStop();
    }

    private void registerEndpoint() {
        Spark.post("/user", (req, res) -> {
            try {
                String result = handler.register(req.body());

                res.type("application/json");
                return result;
            } catch (DataAccessException e) {
                var serializer = new Gson();

                res.status(convertErrorMessage(e));
                return serializer.toJson(Map.of("message", e.getMessage()));
            }
        });
    }

    private void loginEndpoint() {
        Spark.post("/session", (req, res) -> {
            try {
                String result = handler.login(req.body());

                res.type("application/json");
                return result;
            } catch (DataAccessException e) {
                var serializer = new Gson();

                res.status(convertErrorMessage(e));
                return serializer.toJson(Map.of("message", e.getMessage()));
            }
        });
    }

    private void logoutEndpoint() {
        Spark.delete("/session", (req, res) -> {
            try {
                String result = handler.logout(req.headers("Authorization"));

                res.type("application/json");
                return result;
            } catch (DataAccessException e) {
                var serializer = new Gson();

                res.status(convertErrorMessage(e));
                return serializer.toJson(Map.of("message", e.getMessage()));
            }
        });
    }

    private void createEndpoint() {
        Spark.post("/game", (req, res) -> {
            try {
                handler.authorize(req.headers("Authorization"));

                String result = handler.create(req.body());

                res.type("application/json");
                return result;
            } catch (DataAccessException e) {
                var serializer = new Gson();

                res.status(convertErrorMessage(e));
                return serializer.toJson(Map.of("message", e.getMessage()));
            }
        });
    }

    private void listEndpoint() {
        Spark.get("/game", (req, res) -> {
            try {
                System.out.println("AUTH: " + req.headers("Authorization"));
                handler.authorize(req.headers("Authorization"));

                String result = handler.list(req.headers("Authorization"));

                res.type("application/json");
                return result;
            } catch (DataAccessException e) {
                var serializer = new Gson();

                res.status(convertErrorMessage(e));
                return serializer.toJson(Map.of("message", e.getMessage()));
            }
        });
    }

    private void joinEndpoint() {
        Spark.put("/game", (req, res) -> {
            try {
                handler.authorize(req.headers("Authorization"));

                String result = handler.join(req.headers("Authorization"), req.body());

                res.type("application/json");
                return result;
            } catch (DataAccessException e) {
                var serializer = new Gson();

                res.status(convertErrorMessage(e));
                return serializer.toJson(Map.of("message", e.getMessage()));
            }
        });
    }

    private void clearEndpoint() {
        Spark.delete("/db", (req, res) -> {
            try {
                String result = handler.clear();

                res.type("application/json");
                return result;
            } catch (DataAccessException e) {
                var serializer = new Gson();

                res.status(convertErrorMessage(e));
                return serializer.toJson(Map.of("message", "Error: " + e.getMessage()));
            }
        });
    }


    private int convertErrorMessage(DataAccessException e) {
        int err = 500;

        switch (e.getMessage()) {
            case "Error: bad request" -> err = 400;
            case "Error: unauthorized" -> err = 401;
            case "Error: already taken" -> err = 403;
        }

        return err;
    }
}
