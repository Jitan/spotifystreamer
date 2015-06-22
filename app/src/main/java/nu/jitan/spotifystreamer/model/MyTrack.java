package nu.jitan.spotifystreamer.model;

import android.os.Parcelable;
import auto.parcel.AutoParcel;

@AutoParcel
abstract public class MyTrack implements Parcelable {
    public abstract String getAlbumName();
    public abstract String getTrackName();
    public abstract String getThumbImgUrl();
    public abstract String getLargeImgUrl();
    public abstract String getPreviewUrl();

    public static MyTrack create(String albumName, String trackName, String thumbImgUrl, String
        largeImgUrl, String previewUrl) {
        return new AutoParcel_MyTrack(albumName, trackName, thumbImgUrl, largeImgUrl, previewUrl);
    }
}
