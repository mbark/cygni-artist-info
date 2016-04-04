package se.mbark.cygni;

import io.vertx.core.AsyncResult;
import io.vertx.core.Handler;
import io.vertx.core.Vertx;
import io.vertx.core.json.JsonObject;
import io.vertx.lang.rxjava.InternalHelper;

import java.net.URL;

/**
 * Created by mbark on 04/04/16.
 */
public class CovertArtArchiveApi {
    public CovertArtArchiveApi(Vertx vertx) {
    }

    public void getAlbumCover(String mbid, Handler<AsyncResult<JsonObject>> callback) {
        callback.handle(InternalHelper.result(new JsonObject()));
    }
}
