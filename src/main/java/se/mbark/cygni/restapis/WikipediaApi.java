package se.mbark.cygni.restapis;

import io.vertx.core.Vertx;
import io.vertx.core.http.HttpClient;
import io.vertx.core.http.HttpClientRequest;
import io.vertx.core.json.JsonObject;
import se.mbark.cygni.util.RestClientUtil;

import java.util.function.BiConsumer;
import java.util.function.Consumer;

/**
 * Created by mbark on 04/04/16.
 */
public class WikipediaApi {
    private static final String BASE_URL = "https://en.wikipedia.org/w/api.php?";
    private static final String FORMAT = "&format=json";
    private static final String TITLES = "&titles=";
    private static final String ACTION = "&action=query";
    private static final String PROP = "&prop=extracts";
    private static final String REDIRECTS = "&redirects";

    private final HttpClient client;


    public WikipediaApi(Vertx vertx) {
        client = RestClientUtil.getSslClient(vertx);
    }

    public void getArtistDescription(String title, Consumer<JsonObject> success, BiConsumer<Integer, String> fail) {
        String url = buildUrl(title);
        HttpClientRequest request = RestClientUtil.getJsonRequest(client, url, success, fail);
        request.end();
    }

    private String buildUrl(String title) {
        String url = BASE_URL + TITLES + title + FORMAT + ACTION + PROP + REDIRECTS;
        return url;
    }
}
