package nu.jitan.spotifystreamer.service.events;

import nu.jitan.spotifystreamer.model.MyTrack;

public class UpdateUiEvent {
    public final MyTrack track;
    public final String action;

    public UpdateUiEvent(MyTrack track, String action) {
        this.track = track;
        this.action = action;
    }
}
