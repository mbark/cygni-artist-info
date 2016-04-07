package se.mbark.cygni;

import io.vertx.core.AsyncResult;
import io.vertx.core.Handler;
import io.vertx.core.Vertx;
import io.vertx.core.http.HttpClient;
import io.vertx.core.http.HttpClientOptions;
import io.vertx.core.http.HttpClientRequest;
import io.vertx.core.json.JsonObject;
import io.vertx.core.net.NetClient;
import io.vertx.core.net.NetClientOptions;
import io.vertx.lang.rxjava.InternalHelper;

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

    public void getArtistDescription(String title, Handler<AsyncResult<JsonObject>> callback) {
        String url = buildUrl(title);
        HttpClientRequest request = RestClientUtil.getJsonRequest(client, url, callback);
        request.end();
    }

    private String buildUrl(String title) {
        String url = BASE_URL + TITLES + title + FORMAT + ACTION + PROP + REDIRECTS;
        return url;
    }
}
