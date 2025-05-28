package server;

import com.google.gson.Gson;

import dataaccess.DataAccessException;
import service.request.*;

import javax.xml.crypto.Data;
import java.io.*;
import java.net.*;

public class ServerFacade {

    private final String serverUrl;

    public ServerFacade(String url) {
        serverUrl = url;
    }

    public RegisterResult register(RegisterRequest request) throws DataAccessException {
        var path = "/user";
        return this.makeRequest("POST", path, request, RegisterResult.class);
    }

    public LoginResult login(LoginRequest loginRequest) throws DataAccessException {
        var path = "/session";
        return this.makeRequest("POST", path, loginRequest, LoginResult.class);
    }

    public void logout(LogoutRequest logoutRequest) throws DataAccessException {
        var path = "/session";
        this.makeRequest("DELETE", path, logoutRequest, null);
    }

    public CreateResult create(CreateRequest createRequest) throws DataAccessException {
        var path = "/game";
        return this.makeRequest("POST", path, createRequest, CreateResult.class);
    }

    public JoinResult join(String username, JoinRequest joinRequest) throws DataAccessException {
        var path = "/game";
        return this.makeRequest("PUT", path, joinRequest, JoinResult.class);
    }

    public ListResult list(ListRequest listRequest) throws DataAccessException {
        var path = "/game";
        return this.makeRequest("GET", path, listRequest, ListResult.class);
    }

    public ClearResult clear() throws DataAccessException {
        var path = "/db";
        return this.makeRequest("DELETE", path, null, ClearResult.class);
    }

    // ...

    private <T> T makeRequest(String method, String path, Object request, Class<T> responseClass) throws DataAccessException {
        try {
            URL url = (new URI(serverUrl + path)).toURL();
            HttpURLConnection http = (HttpURLConnection) url.openConnection();
            http.setRequestMethod(method);
            http.setDoOutput(true);

            writeBody(request, http);
            http.connect();
            throwIfNotSuccessful(http);
            return readBody(http, responseClass);
        } catch (Exception ex) {
            throw new DataAccessException(ex.getMessage());
        }
    }

    private static void writeBody(Object request, HttpURLConnection http) throws IOException {
        if (request != null) {
            http.addRequestProperty("Content-Type", "application/json");
            String reqData = new Gson().toJson(request);
            try (OutputStream reqBody = http.getOutputStream()) {
                reqBody.write(reqData.getBytes());
            }
        }
    }

    private void throwIfNotSuccessful(HttpURLConnection http) throws IOException, DataAccessException {
        var status = http.getResponseCode();
        if (!isSuccessful(status)) {
            try (InputStream respErr = http.getErrorStream()) {
                if (respErr != null) {
                    throw new DataAccessException(respErr.toString());
                }
            }

            throw new DataAccessException("other failure: " + status);
        }
    }

    private static <T> T readBody(HttpURLConnection http, Class<T> responseClass) throws IOException {
        T response = null;
        if (http.getContentLength() < 0) {
            try (InputStream respBody = http.getInputStream()) {
                InputStreamReader reader = new InputStreamReader(respBody);
                if (responseClass != null) {
                    response = new Gson().fromJson(reader, responseClass);
                }
            }
        }
        return response;
    }


    private boolean isSuccessful(int status) {
        return status / 100 == 2;
    }

}
