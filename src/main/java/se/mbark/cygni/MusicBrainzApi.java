package se.mbark.cygni;

import io.vertx.core.AsyncResult;
import io.vertx.core.Handler;
import io.vertx.core.Vertx;
import io.vertx.core.http.HttpClient;
import io.vertx.core.json.JsonObject;
import io.vertx.lang.rxjava.InternalHelper;

/**
 * Created by mbark on 04/04/16.
 */
public class MusicBrainzApi {
    private static final String API_URL = "http://musicbrainz.org/ws/2/artist/";
    private static final String INC_PARAM = "url-rels+release-groups";
    private static final String FMT_PARAM = "json";
    private final HttpClient client;

    public MusicBrainzApi(Vertx vertx) {
        client = vertx.createHttpClient();
    }

    public void getArtistInfo(String mbid, Handler<AsyncResult<JsonObject>> callback) {
        String url = buildUrl(mbid);
        client.getAbs(url, response -> {
            response.bodyHandler(body -> {
                String content = body.getString(0, body.length());
                JsonObject json = new JsonObject(content);
                callback.handle(InternalHelper.result(json));
            });
        }).putHeader("user-agent", "CygniProgrammingTest/0.0.1 ( barksten@kth.se )").end();
    }

    private String buildUrl(String mbid) {
        String url = API_URL + mbid + "?" + "&fmt=" + FMT_PARAM + "&inc=" + INC_PARAM;
        return url;
    }
}
