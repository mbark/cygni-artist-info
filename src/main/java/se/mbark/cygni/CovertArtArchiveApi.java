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
public class CovertArtArchiveApi {
    private static final String URL = "http://coverartarchive.org/";
    private static final String RELEASE_GROUP = "release-group/";
    private static final String FRONT = "/front";

    private final HttpClient client;

    public CovertArtArchiveApi(Vertx vertx) {
        client = vertx.createHttpClient();
    }

    public void getAlbumCover(String mbid, Handler<AsyncResult<JsonObject>> callback) {
        String url = buildUrl(mbid);
        client.getAbs(url, response -> {
            if(response.statusCode() == 307 || response.statusCode() == 302) {
                String location = response.getHeader("Location");
                JsonObject image = new JsonObject();
                image.put("image", location);
                callback.handle(InternalHelper.result(image));
            } else {
                callback.handle(InternalHelper.failure(new Exception()));
            }
        }).end();
    }

    private String buildUrl(String mbid) {
        String url = URL + RELEASE_GROUP + mbid + FRONT;
        return url;
    }
}
