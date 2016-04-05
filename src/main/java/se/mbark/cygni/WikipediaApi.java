package se.mbark.cygni;

import io.vertx.core.AsyncResult;
import io.vertx.core.Handler;
import io.vertx.core.Vertx;
import io.vertx.core.json.JsonObject;
import io.vertx.lang.rxjava.InternalHelper;

/**
 * Created by mbark on 04/04/16.
 */
public class WikipediaApi {
    public WikipediaApi(Vertx vertx) {
    }

    public void getArtistDescription(String artistName, Handler<AsyncResult<JsonObject>> callback) {
        callback.handle(InternalHelper.result(new JsonObject()));
    }
}