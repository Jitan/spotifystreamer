package nu.jitan.spotifystreamer;

import android.os.Parcelable;
import auto.parcel.AutoParcel;
import java.util.List;

@AutoParcel
public abstract class MyTrackList implements Parcelable {
    public abstract List<MyTrack> getMyTrackList();

    public static MyTrackList create(List<MyTrack> myTrackList) {
        return new AutoParcel_MyTrackList(myTrackList);
    }
}
