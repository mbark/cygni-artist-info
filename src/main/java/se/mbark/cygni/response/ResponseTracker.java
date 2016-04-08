package se.mbark.cygni.response;

/**
 * Created by mbark on 06/04/16.
 */
public class ResponseTracker {
    private boolean hasWikipediaInfo = false;
    private int albumInfosNotSet = 0;

    final private Runnable callWhenDone;

    public ResponseTracker(Runnable isDone) {
        callWhenDone = isDone;
    }

    public void setExpectedAlbumInfoRespones(int count) {
        albumInfosNotSet = count;
    }

    private void callIfDone() {
        boolean done = hasWikipediaInfo && albumInfosNotSet <= 0;
        if(done) {
            callWhenDone.run();
        }
    }

    public void wikipediaRequestReceived() {
        hasWikipediaInfo = true;
        callIfDone();
    }

    public void albumInfoReceived() {
        albumInfosNotSet--;
        callIfDone();
    }

}
