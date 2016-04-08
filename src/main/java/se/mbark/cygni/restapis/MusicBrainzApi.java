package se.mbark.cygni.restapis;

import io.vertx.core.Vertx;
import io.vertx.core.http.HttpClient;
import io.vertx.core.http.HttpClientRequest;
import io.vertx.core.json.JsonObject;

import java.util.function.BiConsumer;
import java.util.function.Consumer;

/**
 * Created by mbark on 04/04/16.
 */
public class MusicBrainzApi extends AbstractRestApi {
    private static final String USER_AGENT = "CygniProgrammingTest/0.0.1 ( barksten@kth.se )";

    private final HttpClient client;

    public MusicBrainzApi(Vertx vertx) {
        client = vertx.createHttpClient();
    }

    public void get(String url, Consumer<JsonObject> success, BiConsumer<Integer, String> fail) {
        HttpClientRequest request = getJsonRequest(client, url, success, (statusCode, errrorMsg) -> {
            if(statusCode == 503) {
                fail.accept(429, "Too many requests to MusicBrainz, try waiting a little");
            } else {
                JsonObject json = new JsonObject(errrorMsg);
                fail.accept(statusCode, json.getString("error"));
            }
        });
        request.putHeader("user-agent", USER_AGENT).end();
    }
}
