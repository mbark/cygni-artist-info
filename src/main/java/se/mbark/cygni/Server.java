/**
 * Created by mbark on 04/04/16.
 */
package se.mbark.cygni;

import io.vertx.core.*;
import io.vertx.core.http.HttpServer;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.web.Router;
import io.vertx.ext.web.RoutingContext;
import se.mbark.cygni.parsers.CoverArtArchiveResponseParser;
import se.mbark.cygni.parsers.MusicBrainzResponseParser;
import se.mbark.cygni.parsers.WikipediaResponeParser;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

public class Server extends AbstractVerticle {
    final private Vertx vertx = Vertx.vertx();
    final private WikipediaApi wikipedia = new WikipediaApi(vertx);
    final private MusicBrainzApi musicBrainz = new MusicBrainzApi(vertx);
    final private CovertArtArchiveApi coverArtArchive = new CovertArtArchiveApi(vertx);

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

    private void getAlbumInfo(RoutingContext context) {
        String mbid = context.request().getParam("mbid");
        if (mbid == null) {
            context.response().setStatusCode(400).end();
            return;
        }

        musicBrainz.getArtistInfo(mbid, musicBrainzRequest -> {
            if(musicBrainzRequest.succeeded()) {
                JsonObject musicBrainzResponse = musicBrainzRequest.result();
                ArtistInfo artist = MusicBrainzResponseParser.parseMusicBrainzResponse(mbid, musicBrainzResponse);

                if(artist == null) {
                    context.response().setStatusCode(400).end();
                    return;
                }
                String wikipediaTitle = MusicBrainzResponseParser.parseWikipediaArtistTitle(musicBrainzResponse);

                wikipedia.getArtistDescription(wikipediaTitle, wikipediaRequest -> {
                    String extract = WikipediaResponeParser.parseExtract(wikipediaRequest.result());
                });

                for(AlbumInfo album : artist.getAlbums()) {
                    coverArtArchive.getAlbumCover(album.getId(), coverArtArchiveRequest -> {
                        if(coverArtArchiveRequest.succeeded()) {
                            URL imageUrl = CoverArtArchiveResponseParser.parseImageUrl(coverArtArchiveRequest.result());
                            album.setImage(imageUrl);
                        }
                    });
                }
            }
        });

        context.response().setStatusCode(200).end();
    }

    private void completeStartup(AsyncResult<HttpServer> http, Future<Void> fut) {
        if (http.succeeded()) {
            fut.complete();
        } else {
            fut.fail(http.cause());
        }
    }
}
