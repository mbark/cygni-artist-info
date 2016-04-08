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

import java.util.List;

public class Server extends AbstractVerticle {
    final private Vertx vertx = Vertx.vertx();
    final private WikipediaApi wikipedia = new WikipediaApi(vertx);
    final private MusicBrainzApi musicBrainz = new MusicBrainzApi(vertx);
    final private CovertArtArchiveApi coverArtArchive = new CovertArtArchiveApi(vertx);
    final private static Logger LOGGER = LoggerFactory.getLogger("se.mbark.cygni.Server");

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

        String mbid = context.request().getParam("mbid");
        if (mbid == null || mbid == "") {
            failWithMessage(context, "Parameter mbid must be supplied", 400);
            return;
        }

        LOGGER.info("Requesting info about artist with mbid = {0}", mbid);
        final ResponseHandler responseHandler = new ResponseHandler(mbid);
        ResponseTracker responseTracker = new ResponseTracker(() -> {
           respond(context, responseHandler);
        });

        // who doesn't love callbacks, amirite?
        musicBrainz.getArtistInfo(mbid, musicBrainzResponse -> {
            responseHandler.handleMusicBrainzResponse(musicBrainzResponse);

            wikipedia.getArtistDescription(responseHandler.getWikipediaTitle(), wikipediaResponse -> {
                responseHandler.handleWikipediaResponse(wikipediaResponse);
                responseTracker.wikipediaRequestReceived();
            }, (statusCode, errorMsg) -> {
                LOGGER.warn("Wikipedia request failed with status code = {0} and content {1}", statusCode, errorMsg);
                responseTracker.wikipediaRequestReceived();
            });

            List<AlbumInfo> albums = responseHandler.getArtistInfo().getAlbums();
            responseTracker.setExpectedAlbumInfoRespones(albums.size());

            for(AlbumInfo album : responseHandler.getArtistInfo().getAlbums()) {
                coverArtArchive.getAlbumCover(album.getId(), coverArtResponse -> {
                    responseHandler.handleCoverArtArchiveResponse(album, coverArtResponse);
                    responseTracker.albumInfoReceived();
                }, (statusCode, errorMsg) -> {
                    if(statusCode == 404) {
                        LOGGER.info("No cover art for album with id {0}", album.getId());
                    } else {
                        LOGGER.warn("CoverArt archive request failed with status code = {0}", statusCode);
                    }
                    responseTracker.albumInfoReceived();
                });
            }
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
                .setStatusCode(statusCode)
                .end(Json.encodePrettily(json));
    }

    private void respond(RoutingContext context, ResponseHandler handler) {
        ArtistInfo artistInfo = handler.getArtistInfo();

        LOGGER.info("Done handling response with context {0}", context);
        LOGGER.debug("Responding with {0}", artistInfo);

        context
                .response()
                .putHeader("content-type", "application/json; charset=utf-8")
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
                .setStatusCode(statusCode)
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
