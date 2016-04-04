/**
 * Created by mbark on 04/04/16.
 */
package se.mbark.cygni;

import io.vertx.core.*;
import io.vertx.core.http.HttpServer;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.web.Router;
import io.vertx.ext.web.RoutingContext;
import io.vertx.ext.web.handler.BodyHandler;

public class Server extends AbstractVerticle {
    final private Vertx vertx = Vertx.vertx();
    final private WikipediaApi wikipedia = new WikipediaApi();
    final private MusicBrainzApi musicBrainz = new MusicBrainzApi();
    final private CovertArtArchiveApi covertArtArchive = new CovertArtArchiveApi();

    @Override
    public void start(Future<Void> fut) {
        startWebApp((http) -> completeStartup(http, fut));
    }

    public void stop(Future<Void> fut) {
        fut.complete();
    }

    private void startWebApp(Handler<AsyncResult<HttpServer>> next) {
        Router router = Router.router(vertx);

        router.get("/").handler(this::getAlbumInfo);

        vertx.createHttpServer()
                .requestHandler(router::accept)
                .listen(config().getInteger("http.port", 8081), next::handle);
    }

    public void getAlbumInfo(RoutingContext context) {
        try {
            Handler<AsyncResult<JsonObject>> callback = result -> {
                System.out.println("got callback with result" + result.result());
            };

            String mbid = context.request().getParam("mbid");
            if (mbid == null) {
                context.response().setStatusCode(400).end();
                return;
            }

            musicBrainz.getArtistInfo(mbid, callback);
            wikipedia.getArtistDescription(mbid, callback);
            covertArtArchive.getAlbumCover(mbid, callback);

            context.response().setStatusCode(200).end();
        } catch(Exception e) {
            System.err.println("Got exception during request")
            System.err.println(e);
            context.response().setStatusCode(400).end();
        }
    }

    private void completeStartup(AsyncResult<HttpServer> http, Future<Void> fut) {
        if (http.succeeded()) {
            fut.complete();
        } else {
            fut.fail(http.cause());
        }
    }
}
