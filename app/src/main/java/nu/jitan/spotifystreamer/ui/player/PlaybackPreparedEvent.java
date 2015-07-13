package nu.jitan.spotifystreamer.ui.player;

public class PlaybackPreparedEvent {
    public final int duration;

    public PlaybackPreparedEvent(int duration) {
        this.duration = duration;
    }
}
