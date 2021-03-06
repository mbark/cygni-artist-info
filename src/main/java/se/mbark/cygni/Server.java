/**
 * Created by mbark on 04/04/16.
 */
package se.mbark.cygni;

import io.vertx.core.*;
import io.vertx.core.http.HttpServer;
import io.vertx.core.json.Json;
import io.vertx.core.json.JsonObject;
import io.vertx.core.logging.Logger;
import io.vertx.core.logging.LoggerFactory;
import io.vertx.ext.web.Router;
import io.vertx.ext.web.RoutingContext;
import se.mbark.cygni.beans.AlbumInfo;
import se.mbark.cygni.beans.ArtistInfo;
import se.mbark.cygni.response.ResponseHandler;
import se.mbark.cygni.response.ResponseTracker;
import se.mbark.cygni.restapis.CovertArtArchiveApi;
import se.mbark.cygni.restapis.MusicBrainzApi;
import se.mbark.cygni.restapis.WikipediaApi;
import se.mbark.cygni.util.UrlBuilder;

import java.util.List;

public class Server extends AbstractVerticle {
    private static final String CONTENT_TYPE = "application/json; charset=utf-8";
    private static final Logger LOGGER = LoggerFactory.getLogger("se.mbark.cygni.Server");

    final private Vertx vertx = Vertx.vertx();
    final private WikipediaApi wikipedia = new WikipediaApi(vertx);
    final private MusicBrainzApi musicBrainz = new MusicBrainzApi(vertx);
    final private CovertArtArchiveApi coverArtArchive = new CovertArtArchiveApi(vertx);

    private UrlBuilder urlBuilder = new UrlBuilder();

    @Override
    public void start(Future<Void> fut) {
        startWebApp((http) -> completeStartup(http, fut));
    }

    public void stop(Future<Void> fut) {
        fut.complete();
    }

    private void startWebApp(Handler<AsyncResult<HttpServer>> next) {
        Router router = Router.router(vertx);

        router.get("/")
                .handler(this::getAlbumInfo)
                .failureHandler(this::failureHandler);

        vertx.createHttpServer()
                .requestHandler(router::accept)
                .listen(config().getInteger("http.port", 8080), next::handle);
    }

    private void getAlbumInfo(RoutingContext context) {
        LOGGER.debug("Received request at url {0}", context.request().absoluteURI());

        final String mbid = context.request().getParam("mbid");
        if (mbid == null || mbid == "") {
            failWithMessage(context, "Parameter mbid must be supplied", 400);
            return;
        }

        LOGGER.info("Requesting info about artist with mbid = {0}", mbid);
        final ResponseHandler responseHandler = new ResponseHandler(mbid);
        final ResponseTracker responseTracker = new ResponseTracker(() -> {
            respond(context, responseHandler);
        });

        final ApiController controller = new ApiController(context, urlBuilder, responseHandler, responseTracker);
        controller.doMusicBrainzGet(musicBrainz, () -> {
            controller.doWikipediaGet(wikipedia);
            controller.doCoverArtArchiveGet(coverArtArchive);
        }, (statusCode, errorMsg) -> {
            LOGGER.warn("MusicBrainz request failed, returning status code = {0}", statusCode);
            failWithMessage(context, errorMsg, statusCode);
        });
    }

    private void failWithMessage(RoutingContext context, String message, int statusCode) {
        JsonObject json = new JsonObject();
        json.put("error", message);

        LOGGER.debug("Failing request with status code = {0} and message {1}", statusCode, message);
        context.
                response()
                .putHeader("content-type", CONTENT_TYPE)
                .setStatusCode(statusCode)
                .end(Json.encodePrettily(json));
    }

    private void respond(RoutingContext context, ResponseHandler handler) {
        ArtistInfo artistInfo = handler.getArtistInfo();

        LOGGER.info("Done handling response with context {0}", context);
        LOGGER.debug("Responding with {0}", artistInfo);

        context
                .response()
                .putHeader("content-type", CONTENT_TYPE)
                .setStatusCode(200)
                .end(Json.encodePrettily(artistInfo));
    }

    private void failureHandler(RoutingContext context) {
        int statusCode = context.statusCode();
        String body = context.getBodyAsString();

        LOGGER.warn("Failure handling request, got status code {0}", statusCode);
        LOGGER.debug("Response body is {0}", body);

        JsonObject json = new JsonObject();
        json.put("error", "Internal server error processing request");

        context.response()
                .setStatusCode(500)
                .end(Json.encodePrettily(json));
    }

    private void completeStartup(AsyncResult<HttpServer> http, Future<Void> fut) {
        if (http.succeeded()) {
            fut.complete();
        } else {
            LOGGER.warn("Unable to complete startup", http.cause());
            fut.fail(http.cause());
        }
    }
}
