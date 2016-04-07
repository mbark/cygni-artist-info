package se.mbark.cygni.restapis;

import io.vertx.core.AsyncResult;
import io.vertx.core.Handler;
import io.vertx.core.Vertx;
import io.vertx.core.http.HttpClient;
import io.vertx.core.http.HttpClientRequest;
import io.vertx.core.json.JsonObject;
import se.mbark.cygni.util.RestClientUtil;

/**
 * Created by mbark on 04/04/16.
 */
public class MusicBrainzApi {
    private static final String API_URL = "http://musicbrainz.org/ws/2/artist/";
    private static final String INC_PARAM = "url-rels+release-groups";
    private static final String FMT_PARAM = "json";
    private static final String USER_AGENT = "CygniProgrammingTest/0.0.1 ( barksten@kth.se )";

    private final HttpClient client;

    public MusicBrainzApi(Vertx vertx) {
        client = vertx.createHttpClient();
    }

    public void getArtistInfo(String mbid, Handler<AsyncResult<JsonObject>> callback) {
        String url = buildUrl(mbid);
        HttpClientRequest request = RestClientUtil.getJsonRequest(client, url, callback);
        request.putHeader("user-agent", USER_AGENT).end();
    }

    private String buildUrl(String mbid) {
        String url = API_URL + mbid + "?" + "&fmt=" + FMT_PARAM + "&inc=" + INC_PARAM;
        return url;
    }
}
