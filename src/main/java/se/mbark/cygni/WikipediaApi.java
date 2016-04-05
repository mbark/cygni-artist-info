package se.mbark.cygni;

import io.vertx.core.AsyncResult;
import io.vertx.core.Handler;
import io.vertx.core.Vertx;
import io.vertx.core.http.HttpClient;
import io.vertx.core.http.HttpClientOptions;
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
        HttpClientOptions options = new HttpClientOptions().
                setSsl(true).
                setTrustAll(true);
        client = vertx.createHttpClient(options);
    }

    public void getArtistDescription(String title, Handler<AsyncResult<JsonObject>> callback) {
        String url = buildUrl(title);

        client.getAbs(url, response -> {
            response.bodyHandler(body -> {
                String content = body.getString(0, body.length());

                if(response.statusCode() == 200) {
                    JsonObject json = new JsonObject(content);
                    callback.handle(InternalHelper.result(json));
                } else {
                    System.out.println("Not 200: " + response.statusCode() + ", response body: " + content);
                    System.out.println(response.getHeader("Location"));
                    callback.handle(InternalHelper.failure(new Exception()));
                }
            });
        }).end();
    }

    private String buildUrl(String title) {
        String url = BASE_URL + TITLES + title + FORMAT + ACTION + PROP + REDIRECTS;
        System.out.println(url);
        return url;
    }
}
