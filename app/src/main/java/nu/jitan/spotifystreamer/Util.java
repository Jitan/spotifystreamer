package nu.jitan.spotifystreamer;

import java.util.ArrayList;
import java.util.List;
import kaaes.spotify.webapi.android.models.Track;
import kaaes.spotify.webapi.android.models.Tracks;
import nu.jitan.spotifystreamer.model.MyTrack;

public final class Util {
    private Util() {
    }

    /**
     * Extracts the data we need from Spotify API Wrapper object and stores it in our own parcelable
     * MyTrack object.
     *
     * @param tracks Original Tracks object from Spotify API Wrapper
     * @return A list with parcelable MyTrack objects
     */
    public static List<MyTrack> extractTrackData(Tracks tracks) {
        List<MyTrack> myTrackList = new ArrayList<>();

        String albumName, trackName, imgUrl;
        for (Track track : tracks.tracks) {
            albumName = track.album.name;
            trackName = track.name;

            if (track.album.images.size() > 0) {
                imgUrl = track.album.images.get(0).url;
            } else {
                imgUrl = "";
            }
            myTrackList.add(MyTrack.create(albumName, trackName, imgUrl));
        }
        return myTrackList;
    }
}
