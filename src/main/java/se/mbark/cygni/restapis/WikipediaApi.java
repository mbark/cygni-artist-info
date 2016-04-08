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
public class WikipediaApi extends AbstractRestApi {
    private final HttpClient client;

    public WikipediaApi(Vertx vertx) {
        client = getSslClient(vertx);
    }

    public void get(String url, Consumer<JsonObject> success, BiConsumer<Integer, String> fail) {
        HttpClientRequest request = getJsonRequest(client, url, success, fail);
        request.end();
    }
}
