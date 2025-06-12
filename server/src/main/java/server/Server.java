package server;

import com.google.gson.Gson;
import dataaccess.DataAccessException;
import handler.*;
import model.GameData;
import request.LoginResult;
import request.UpdateRequest;
import server.websocket.WebSocketHandler;
import spark.*;

import java.util.Map;

public class Server {

    private Handler handler = new Handler();
    private final WebSocketHandler webSocketHandler = new WebSocketHandler(this);
    private String authToken;

    public int run(int desiredPort) {
        Spark.port(desiredPort);

        Spark.staticFiles.location("web");

        Spark.webSocket("/ws", webSocketHandler);

        // Run All Endpoints
        registerEndpoint();

        loginEndpoint();

        logoutEndpoint();

        createEndpoint();

        listEndpoint();

        joinEndpoint();

        updateEndpoint();

        authorizeEndpoint();

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

    private void updateEndpoint() {
        Spark.put("/update", (req, res) -> {
            try {
                handler.authorize(req.headers("Authorization"));

                String result = handler.updateGame(req.body());

                res.type("application/json");
                return result;
            } catch (DataAccessException e) {
                var serializer = new Gson();

                res.status(convertErrorMessage(e));
                return serializer.toJson(Map.of("message", e.getMessage()));
            }
        });
    }

    private void authorizeEndpoint() {
        Spark.get("/auth", (req, res) -> {
            try {
                String username = handler.authorize(req.headers("Authorization"));

                LoginResult loginResult = new LoginResult(username, req.headers("Authorization"));

                String json = new Gson().toJson(loginResult);

                res.type("application/json");
                return json;
            } catch (DataAccessException e) {
                var serializer = new Gson();

                res.status(convertErrorMessage(e));
                return serializer.toJson(Map.of("message", e.getMessage()));
            }
        });
    }

    public String authorize(String authToken) throws DataAccessException {
        return handler.authorize(authToken);
    }

    public GameData getGame(int gameID, String authToken) throws DataAccessException {
        return handler.getGame(gameID, authToken);
    }

    public int gameCount(String authToken) throws DataAccessException {
        return handler.gameCount(authToken);
    }

    public void updateGame(String authToken, UpdateRequest updateRequest) throws DataAccessException {
        handler.updateGame(updateRequest);
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
