package se.mbark.cygni.parsers;

import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import io.vertx.core.logging.Logger;
import io.vertx.core.logging.LoggerFactory;
import se.mbark.cygni.AlbumInfo;
import se.mbark.cygni.ArtistInfo;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by mbark on 05/04/16.
 */
public class MusicBrainzResponseParser {
    private static final Logger LOGGER = LoggerFactory.getLogger("se.mbark.cygni.MusicBrainzResponseParser");

    public static String parseWikipediaArtistTitle(JsonObject musicBrainzResponse) {
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

    // thank you stack overflow
    private static String getLastBitFromUrl(final String url){
        return url.replaceFirst(".*/([^/?]+).*", "$1");
    }

    public static ArtistInfo parseMusicBrainzResponse(String mbid, JsonObject musicBrainzResponse) {
        String name = musicBrainzResponse.getString("name");
        JsonArray jsonAlbums = musicBrainzResponse.getJsonArray("release-groups");

        if(name == null || jsonAlbums == null) {
            LOGGER.warn("Unable to parse MusicBrainz response for mbid {0}", mbid);
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

        LOGGER.debug("MusicBrainzResponse gave result {0}", artist);
        return artist;
    }
}
