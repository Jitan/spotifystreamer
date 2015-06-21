package nu.jitan.spotifystreamer.model;

import android.os.Parcelable;
import auto.parcel.AutoParcel;
import java.util.List;

@AutoParcel
public abstract class MyArtistList implements Parcelable {
    public abstract List<MyArtist> getMyArtistList();

    public static MyArtistList create(List<MyArtist> myArtistList) {
        return new AutoParcel_MyArtistList(myArtistList);
    }
}
