package se.mbark.cygni.restapis;

import io.vertx.core.Vertx;
import io.vertx.core.http.HttpClient;
import io.vertx.core.http.HttpClientOptions;
import io.vertx.core.http.HttpClientRequest;
import io.vertx.core.json.JsonObject;
import io.vertx.core.logging.Logger;
import io.vertx.core.logging.LoggerFactory;

import java.util.function.BiConsumer;
import java.util.function.Consumer;

/**
 * Created by mbark on 08/04/16.
 */
public abstract class AbstractRestApi {
    final private static Logger LOGGER = LoggerFactory.getLogger("se.mbark.cygni.restapis.AbstractRestApi");

    public abstract void get(String url, Consumer<JsonObject> success, BiConsumer<Integer, String> fail);

    protected static HttpClient getSslClient(Vertx vertx) {
        HttpClientOptions options = new HttpClientOptions().
                setSsl(true).
                setTrustAll(true);
        return vertx.createHttpClient(options);
    }

    protected static HttpClientRequest getJsonRequest(HttpClient client, String url, Consumer<JsonObject> success, BiConsumer<Integer, String> fail) {
        LOGGER.debug("GET json from {0}", url);
        return client.getAbs(url, response -> {
            response.bodyHandler(body -> {
                String content = body.getString(0, body.length());

                if(response.statusCode() == 200) {
                    JsonObject json = new JsonObject(content);
                    LOGGER.debug("GET json from {0} got result {1}", url, json);
                    success.accept(json);
                } else {
                    LOGGER.warn("GET json from {0} failed with status code {1}", url, response.statusCode());
                    LOGGER.debug("Failed GET has content {0}", content);
                    fail.accept(response.statusCode(), content);
                }
            });
        });
    }
}

