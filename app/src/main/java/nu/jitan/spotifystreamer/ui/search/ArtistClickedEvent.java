package nu.jitan.spotifystreamer.ui.search;

import nu.jitan.spotifystreamer.model.MyArtist;

public class ArtistClickedEvent {
    public final MyArtist artist;

    public ArtistClickedEvent(MyArtist artist) {
        this.artist = artist;
    }
}
