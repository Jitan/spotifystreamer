package nu.jitan.spotifystreamer.event;

import nu.jitan.spotifystreamer.model.MyTrack;

public class TrackClickedEvent {
    public MyTrack track;

    public TrackClickedEvent(MyTrack track) {
        this.track = track;
    }
}
