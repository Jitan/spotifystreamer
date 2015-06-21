package nu.jitan.spotifystreamer.model;

import android.os.Parcelable;
import auto.parcel.AutoParcel;

@AutoParcel
public abstract class MyArtist implements Parcelable {
    public abstract String getId();
    public abstract String getName();
    public abstract String getImgUrl();

    public static MyArtist create(String id, String name, String imgUrl) {
        return new AutoParcel_MyArtist(id, name, imgUrl);
    }
}
