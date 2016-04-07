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
import se.mbark.cygni.parsers.MusicBrainzResponseParser;
import se.mbark.cygni.restapis.CovertArtArchiveApi;
import se.mbark.cygni.restapis.MusicBrainzApi;
import se.mbark.cygni.restapis.WikipediaApi;
import se.mbark.cygni.util.ResponseHandler;

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
        musicBrainz.getArtistInfo(mbid, musicBrainzRequest -> {
            if(musicBrainzRequest.succeeded()) {
                LOGGER.debug("MusicBrainz request finished successfullyy");
                JsonObject musicBrainzResponse = musicBrainzRequest.result();
                ArtistInfo artist = MusicBrainzResponseParser.parseMusicBrainzResponse(mbid, musicBrainzResponse);
                artist = null;

                if(artist == null) {
                    LOGGER.debug("Unable to parse MusicBrainz response {0}", musicBrainzResponse);
                    failWithMessage(context, "Unable to parse MusicBrainz response", 500);
                    return;
                }

                ResponseHandler handler = new ResponseHandler(artist);

                String wikipediaTitle = MusicBrainzResponseParser.parseWikipediaArtistTitle(musicBrainzResponse);

                wikipedia.getArtistDescription(wikipediaTitle, wikipediaRequest -> {
                    LOGGER.debug("Wikipedia request done {0}", wikipediaRequest);

                    handler.handleWikipediaResponse(wikipediaRequest);
                    respondIfDone(context, handler);
                });

                for(AlbumInfo album : artist.getAlbums()) {
                    coverArtArchive.getAlbumCover(album.getId(), coverArtArchiveRequest -> {
                        LOGGER.debug("CoverArt archive request {0} done for album with id={1}", coverArtArchiveRequest, album.getId());

                        handler.handleCoverArtResponse(album, coverArtArchiveRequest);
                        respondIfDone(context, handler);
                    });
                }
            } else {
                LOGGER.debug("MusicBrainz request failed with cause {0}", musicBrainzRequest.cause().getMessage());
                failWithMessage(context, "MusicBrainz request failed, probably due to rate limiting - try again later", 429);
            }
        });
    }

    private void failWithMessage(RoutingContext context, String message, int statusCode) {
        JsonObject json = new JsonObject();
        json.put("error", message);

        LOGGER.debug("Request failed with status code = {0} and message {1}", statusCode, message);
        context.
                response()
                .setStatusCode(statusCode)
                .end(Json.encodePrettily(json));
    }

    private void respondIfDone(RoutingContext context, ResponseHandler handler) {
        if(handler.isDone()) {
            LOGGER.info("Done handling response with context {0}", context);
            LOGGER.debug("Responding with {0}", handler.getArtistInfo());

            context
                    .response()
                    .putHeader("content-type", "application/json; charset=utf-8")
                    .setStatusCode(200)
                    .end(Json.encodePrettily(handler.getArtistInfo()));
        }
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
