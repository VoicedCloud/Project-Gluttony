package com.github.glutton;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Net;
import com.badlogic.gdx.net.HttpRequestBuilder;
import com.badlogic.gdx.utils.JsonReader;
import com.badlogic.gdx.utils.JsonValue;

public class AuthClient {
    private static final String BASE_URL = "https://careless-overdrive-valiant.ngrok-free.dev/glutton/gateway.php";

    public interface AuthListener {
        void onSuccess(String message, JsonValue data);
        void onError(String message);
    }

    public static void register(String username, String password, AuthListener listener) {
        sendRequest("register", username, password, 0, 0, 0, 0, 0, listener);
    }

    public static void login(String username, String password, AuthListener listener) {
        sendRequest("login", username, password, 0, 0, 0, 0, 0, listener);
    }

    public static void getStats(long userId, AuthListener listener) {
        sendRequest("get_stats", null, null, userId, 0, 0, 0, 0, listener);
    }

    public static void saveStats(long userId, int completion, int cash, int waves, int monsters, AuthListener listener) {
        sendRequest("save_stats", null, null, userId, completion, cash, waves, monsters, listener);
    }

    public static void checkSession(AuthListener listener) {
        HttpRequestBuilder builder = new HttpRequestBuilder();
        Net.HttpRequest request = builder.newRequest()
                .method(Net.HttpMethods.POST)
                .url(BASE_URL)
                .header("Content-Type", "application/json")
                .timeout(3000)
                .build();
        request.setContent("{\"operation\":\"check_session\"}");

        Gdx.net.sendHttpRequest(request, new Net.HttpResponseListener() {
            @Override
            public void handleHttpResponse(Net.HttpResponse httpResponse) {
                String responseStr = httpResponse.getResultAsString();
                Gdx.app.postRunnable(() -> {
                    try {
                        JsonValue root = new JsonReader().parse(responseStr);
                        if ("success".equals(root.getString("status", ""))) {
                            listener.onSuccess("Session found", root);
                        } else {
                            listener.onError("No session");
                        }
                    } catch (Exception e) {
                        listener.onError("Parse error");
                    }
                });
            }
            @Override public void failed(Throwable t) {}
            @Override public void cancelled() {}
        });
    }

    private static void sendRequest(String operation, String username, String password, long userId, 
                                    int completion, int cash, int waves, int monsters, AuthListener listener) {
        
        HttpRequestBuilder builder = new HttpRequestBuilder();
        Net.HttpRequest request = builder.newRequest()
                .method(Net.HttpMethods.POST)
                .url(BASE_URL)
                .header("Content-Type", "application/json")
                .timeout(5000)
                .build();

        StringBuilder json = new StringBuilder();
        json.append("{");
        json.append("\"operation\":\"").append(operation).append("\"");
        if (username != null) json.append(",\"username\":\"").append(username).append("\"");
        if (password != null) json.append(",\"password\":\"").append(password).append("\"");
        if (userId > 0) json.append(",\"user_id\":").append(userId);
        if (operation.equals("save_stats")) {
            json.append(",\"completion\":").append(completion);
            json.append(",\"cash_earned\":").append(cash);
            json.append(",\"waves_survived\":").append(waves);
            json.append(",\"monsters_killed\":").append(monsters);
        }
        json.append("}");

        request.setContent(json.toString());

        Gdx.net.sendHttpRequest(request, new Net.HttpResponseListener() {
            @Override
            public void handleHttpResponse(Net.HttpResponse httpResponse) {
                String responseStr = httpResponse.getResultAsString();
                Gdx.app.postRunnable(() -> {
                    try {
                        JsonValue root = new JsonReader().parse(responseStr);
                        String status = root.getString("status", "error");
                        if ("success".equals(status)) {
                            listener.onSuccess(root.getString("message", "OK"), root);
                        } else {
                            listener.onError(root.getString("message", "Unknown error"));
                        }
                    } catch (Exception e) {
                        listener.onError("Malformed server response");
                    }
                });
            }

            @Override
            public void failed(Throwable t) {
                Gdx.app.postRunnable(() -> listener.onError("Connection failed: " + t.getMessage()));
            }

            @Override
            public void cancelled() {
                Gdx.app.postRunnable(() -> listener.onError("Request cancelled"));
            }
        });
    }
}