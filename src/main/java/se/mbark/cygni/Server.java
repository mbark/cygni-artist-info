/**
 * Created by mbark on 04/04/16.
 */
package se.mbark.cygni;

import io.vertx.core.*;
import io.vertx.core.http.HttpServer;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.web.Router;
import io.vertx.ext.web.RoutingContext;

import java.util.ArrayList;

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

    public void getAlbumInfo(RoutingContext context) {
        String mbid = context.request().getParam("mbid");
        if (mbid == null) {
            context.response().setStatusCode(400).end();
            return;
        }

        musicBrainz.getArtistInfo(mbid, musicBrainzRequest -> {
            if(musicBrainzRequest.succeeded()) {
                JsonObject musicBrainzResponse = musicBrainzRequest.result();
                ArtistInfo artist = parseMusicBrainzResponse(mbid, musicBrainzResponse);

                wikipedia.getArtistDescription(artist.getName(), wikipediaRequest -> {
                    System.out.println("Got response from wikipedia" + wikipediaRequest);
                });

                for(AlbumInfo album : artist.getAlbums()) {
                    coverArtArchive.getAlbumCover(album.getId(), coverArtArchiveRequest -> {
                        System.out.println("Got cover art for " + album.getId() + " with response " + coverArtArchiveRequest);
                    });
                }
            }
        });

        context.response().setStatusCode(200).end();
    }

    private ArtistInfo parseMusicBrainzResponse(String mbid, JsonObject musicBrainzResponse) {
        System.out.println(musicBrainzResponse);

        ArtistInfo artist = new ArtistInfo(mbid);
        artist.setName("");
        artist.setAlbums(new ArrayList<>());
        return artist;
    }

    private void completeStartup(AsyncResult<HttpServer> http, Future<Void> fut) {
        if (http.succeeded()) {
            fut.complete();
        } else {
            fut.fail(http.cause());
        }
    }
}
