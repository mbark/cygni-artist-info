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

import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

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
                ArtistInfo artist = parseMusicBrainzResponse(mbid, musicBrainzResponse);

                if(artist == null) {
                    context.response().setStatusCode(400).end();
                    return;
                }
                String wikipediaTitle = wikipediaArtistTitle(musicBrainzResponse);

                wikipedia.getArtistDescription(wikipediaTitle, wikipediaRequest -> {
                    if(wikipediaRequest.succeeded()) {
                        JsonObject wikipediaResponse = wikipediaRequest.result();
                        JsonObject pagesJson = wikipediaResponse.getJsonObject("query").getJsonObject("pages");

                        Map<String, Object> pages = pagesJson.getMap();
                        String artistExtract = null;

                        for(Map.Entry<String, Object> entry : pages.entrySet()) {
                            JsonObject page = pagesJson.getJsonObject(entry.getKey());
                            // we only request one page
                            artistExtract = page.getString("extract");
                        }

                        System.out.println("Wikipedia extract:" + artistExtract);
                    }
                });

                for(AlbumInfo album : artist.getAlbums()) {
                    coverArtArchive.getAlbumCover(album.getId(), coverArtArchiveRequest -> {
                        if(coverArtArchiveRequest.succeeded()) {
                            JsonObject coverArtResponse = coverArtArchiveRequest.result();
                            try {
                                album.setImage(new URL(coverArtResponse.getString("image")));
                            } catch (MalformedURLException e) {
                                e.printStackTrace();
                            }
                        }
                    });
                }
            }
        });

        context.response().setStatusCode(200).end();
    }

    private String wikipediaArtistTitle(JsonObject musicBrainzResponse) {
        JsonArray relations = musicBrainzResponse.getJsonArray("relations");
        String bandName = null;
        for(int i = 0; i < relations.size(); i++) {
            JsonObject relation = relations.getJsonObject(i);
            if("wikipedia".equals(relation.getString("type"))) {
                JsonObject url = relation.getJsonObject("url");
                String wikipediaUrl = url.getString("resource");
                bandName = getLastBitFromUrl(wikipediaUrl);
            }
        }
        return bandName;
    }

    private static String getLastBitFromUrl(final String url){
        // thank you stack overflow
        return url.replaceFirst(".*/([^/?]+).*", "$1");
    }

    private ArtistInfo parseMusicBrainzResponse(String mbid, JsonObject musicBrainzResponse) {
        String name = musicBrainzResponse.getString("name");
        JsonArray jsonAlbums = musicBrainzResponse.getJsonArray("release-groups");

        if(name == null || jsonAlbums == null) {
            System.out.println("Unable to parse music brainz response");
            System.out.println(musicBrainzResponse);
            return null;
        }

        List<AlbumInfo> albums = new ArrayList<>();
        for(int i = 0; i < jsonAlbums.size(); i++) {
            JsonObject albumInfo = jsonAlbums.getJsonObject(i);
            if("Album".equals(albumInfo.getString("primary-type"))) {
                String id = albumInfo.getString("id");
                String title = albumInfo.getString("title");
                AlbumInfo album = new AlbumInfo(id);
                album.setTitle(title);
                albums.add(album);
            }
        }

        ArtistInfo artist = new ArtistInfo(mbid);
        artist.setName(name);
        artist.setAlbums(albums);
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
