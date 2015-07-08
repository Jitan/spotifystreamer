package nu.jitan.spotifystreamer.event;

import nu.jitan.spotifystreamer.model.MyArtist;

public class ArtistClickedEvent {
    public final MyArtist artist;

    public ArtistClickedEvent(MyArtist artist) {
        this.artist = artist;
    }
}
