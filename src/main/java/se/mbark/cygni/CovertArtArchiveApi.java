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

    private final HttpClient client;

    public CovertArtArchiveApi(Vertx vertx) {
        client = vertx.createHttpClient();
    }

    public void getAlbumCover(String mbid, Handler<AsyncResult<JsonObject>> callback) {
        String url = buildUrl(mbid);
        followRedirects(url, callback);
    }

    private void followRedirects(String url, Handler<AsyncResult<JsonObject>> callback) {
        client.getAbs(url, response -> {
            if(response.statusCode() == 307 || response.statusCode() == 302) {
                String location = response.getHeader("Location");
                followRedirects(location, callback);
            } else {
                if(response.statusCode() != 200) {
                    callback.handle(InternalHelper.failure(new Exception()));
                    return;
                }
                response.bodyHandler(body -> {
                    String content = body.getString(0, body.length());
                    JsonObject json = new JsonObject(content);
                    callback.handle(InternalHelper.result(json));
                });
            }
        }).end();

    }

    private String buildUrl(String mbid) {
        String url = URL + RELEASE_GROUP + mbid;
        System.out.println(url);
        return url;
    }
}
