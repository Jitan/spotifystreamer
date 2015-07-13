package nu.jitan.spotifystreamer.ui.track;

import java.util.ArrayList;
import nu.jitan.spotifystreamer.model.MyTrack;

public class TrackClickedEvent {
    public ArrayList<MyTrack> trackList;
    public int trackListPos;

    public TrackClickedEvent(ArrayList<MyTrack> trackList, int trackListPos) {
        this.trackList = trackList;
        this.trackListPos = trackListPos;
    }
}
