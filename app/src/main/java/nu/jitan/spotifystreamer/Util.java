package nu.jitan.spotifystreamer;

import java.util.ArrayList;
import java.util.List;
import kaaes.spotify.webapi.android.models.Artist;
import kaaes.spotify.webapi.android.models.ArtistsPager;
import kaaes.spotify.webapi.android.models.Track;
import kaaes.spotify.webapi.android.models.Tracks;
import nu.jitan.spotifystreamer.model.MyArtist;
import nu.jitan.spotifystreamer.model.MyTrack;

public final class Util {
    private Util() {
    }

    /**
     * Extracts the data we need from Spotify API Wrapper object and stores it in our own parcelable
     * MyArtist object.
     *
     * @param artistsPager Original ArtistsPager object from Spotify API Wrapper
     * @return A list with parcelable MyArtist objects
     */
    public static ArrayList<MyArtist> extractArtistData(ArtistsPager artistsPager) {
        final List<Artist> artistList = artistsPager.artists.items;
        final ArrayList<MyArtist> myArtistList = new ArrayList<>();
        String artistId, artistName, imgUrl;

        for (Artist artist : artistList) {
            artistId = artist.id;
            artistName = artist.name;

            // Get second last img in array which should always be approx 200-300px wide
            if (artist.images.size() > 0) {
                imgUrl = artist.images.get(artist.images.size() - 2).url;
            } else {
                imgUrl = "";
            }
            myArtistList.add(MyArtist.create(artistId, artistName, imgUrl));
        }
        return myArtistList;
    }

    /**
     * Extracts the data we need from Spotify API Wrapper object and stores it in our own parcelable
     * MyTrack object.
     *
     * @param tracks Original Tracks object from Spotify API Wrapper
     * @return A list with parcelable MyTrack objects
     */
    public static ArrayList<MyTrack> extractTrackData(Tracks tracks) {
        final ArrayList<MyTrack> myTrackList = new ArrayList<>();

        String albumName, trackName, thumbImgUrl, largeImgUrl, previewUrl;
        for (Track track : tracks.tracks) {
            albumName = track.album.name;
            trackName = track.name;
            previewUrl = track.preview_url;

            if (track.album.images.size() > 0) {
                thumbImgUrl = track.album.images.get(1).url;
                largeImgUrl = track.album.images.get(0).url;

            } else {
                thumbImgUrl = "";
                largeImgUrl = "";
            }
            myTrackList.add(MyTrack.create(albumName, trackName, thumbImgUrl, largeImgUrl,
                previewUrl));
        }
        return myTrackList;
    }
}
