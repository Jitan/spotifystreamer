package nu.jitan.spotifystreamer.ui.track;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;
import butterknife.Bind;
import butterknife.ButterKnife;
import com.squareup.picasso.Picasso;
import java.util.ArrayList;
import nu.jitan.spotifystreamer.R;
import nu.jitan.spotifystreamer.model.MyTrack;

public class TrackAdapter extends ArrayAdapter<MyTrack> {
    private ViewHolder mHolder;
    private LayoutInflater mInflater;

    public TrackAdapter(Context context) {
        super(context, 0);
        mInflater = LayoutInflater.from(context);
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        convertView = setupViewHolder(convertView, parent);

        MyTrack track = getItem(position);
        mHolder.trackName.setText(track.getTrackName());
        mHolder.albumName.setText(track.getAlbumName());

        if (!track.getThumbImgUrl().isEmpty()) {
            Picasso.with(getContext())
                .load(track.getThumbImgUrl())
                .resizeDimen(R.dimen.listitem_imageview, R.dimen
                    .listitem_imageview)
                .centerCrop()
                .into(mHolder.albumArt);
        }
        return convertView;
    }

    private View setupViewHolder(View convertView, ViewGroup parent) {
        if (convertView != null) {
            mHolder = (ViewHolder) convertView.getTag();
        } else {
            convertView = mInflater.inflate(R.layout.listitem_track, parent, false);
            mHolder = new ViewHolder(convertView);
            convertView.setTag(mHolder);
        }
        return convertView;
    }

    public ArrayList<MyTrack> getList() {
        ArrayList<MyTrack> list = new ArrayList<>();
        for (int i = 0; i < getCount(); i++)
            list.add(getItem(i));
        return list;
    }

    static class ViewHolder {
        @Bind(R.id.listitem_track_imageview) ImageView albumArt;
        @Bind(R.id.listitem_track_name) TextView trackName;
        @Bind(R.id.listitem_track_album) TextView albumName;

        public ViewHolder(View view) {
            ButterKnife.bind(this, view);
        }
    }
}
