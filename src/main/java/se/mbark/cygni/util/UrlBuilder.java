package se.mbark.cygni.util;

/**
 * Created by mbark on 08/04/16.
 */
public class UrlBuilder {
    private static final String URL = "http://coverartarchive.org/";
    private static final String RELEASE_GROUP = "release-group/";
    private static final String FRONT = "/front";

    private static final String API_URL = "http://musicbrainz.org/ws/2/artist/";
    private static final String INC_PARAM = "url-rels+release-groups";
    private static final String FMT_PARAM = "json";

    private static final String BASE_URL = "https://en.wikipedia.org/w/api.php?";
    private static final String FORMAT = "&format=json";
    private static final String TITLES = "&titles=";
    private static final String ACTION = "&action=query";
    private static final String PROP = "&prop=extracts";
    private static final String REDIRECTS = "&redirects";

    public String getCoverArtArchiveUrl(String mbid) {
        String url = URL + RELEASE_GROUP + mbid + FRONT;
        return url;
    }

    public String getMusicBrainzUrl(String mbid) {
        String url = API_URL + mbid + "?" + "&fmt=" + FMT_PARAM + "&inc=" + INC_PARAM;
        return url;
    }

    public String getWikipediaUrl(String title) {
        String url = BASE_URL + TITLES + title + FORMAT + ACTION + PROP + REDIRECTS;
        return url;
    }
}
