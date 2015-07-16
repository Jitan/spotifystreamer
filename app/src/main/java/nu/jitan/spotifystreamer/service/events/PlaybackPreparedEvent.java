package nu.jitan.spotifystreamer.service.events;

public class PlaybackPreparedEvent {
    public final int duration;

    public PlaybackPreparedEvent(int duration) {
        this.duration = duration;
    }
}
