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
import se.mbark.cygni.parsers.MusicBrainzResponseParser;

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

        router.get("/").handler(this::getAlbumInfo);

        vertx.createHttpServer()
                .requestHandler(router::accept)
                .listen(config().getInteger("http.port", 8080), next::handle);
    }

    private void getAlbumInfo(RoutingContext context) {
        LOGGER.info("Received request for album info {0}", context);

        String mbid = context.request().getParam("mbid");
        if (mbid == null) {
            respondWithError(context, 400);
            return;
        }

        musicBrainz.getArtistInfo(mbid, musicBrainzRequest -> {
            if(musicBrainzRequest.succeeded()) {
                LOGGER.debug("MusicBrainz request finished succesfully");
                JsonObject musicBrainzResponse = musicBrainzRequest.result();
                ArtistInfo artist = MusicBrainzResponseParser.parseMusicBrainzResponse(mbid, musicBrainzResponse);

                if(artist == null) {
                    LOGGER.debug("Unable to parse MusicBrainz response {0}", musicBrainzResponse);
                    respondWithError(context, 400);
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
                respondWithError(context, 400);
            }
        });
    }

    private void respondWithError(RoutingContext context, int statusCode) {
        LOGGER.info("Error processing request, responding with status code {0}", statusCode);
        LOGGER.info("Request had context {0}", context);
        context.response().setStatusCode(statusCode).end();
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

    private void completeStartup(AsyncResult<HttpServer> http, Future<Void> fut) {
        if (http.succeeded()) {
            fut.complete();
        } else {
            LOGGER.warn("Unable to complete startup", http.cause());
            fut.fail(http.cause());
        }
    }
}
