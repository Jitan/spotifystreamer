package nu.jitan.spotifystreamer.model;

import android.os.Parcelable;
import auto.parcel.AutoParcel;

@AutoParcel
abstract public class MyTrack implements Parcelable {
    String albumName, trackName, imgUrl;

    public abstract String getAlbumName();
    public abstract String getTrackName();
    public abstract String getImgUrl();

    public static MyTrack create(String albumName, String trackName, String imgUrl) {
        return new AutoParcel_MyTrack(albumName, trackName, imgUrl);
    }
}
