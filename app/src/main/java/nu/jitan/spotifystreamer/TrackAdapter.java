package nu.jitan.spotifystreamer;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;
import butterknife.ButterKnife;
import butterknife.InjectView;
import com.squareup.picasso.Picasso;
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

    static class ViewHolder {
        @InjectView(R.id.listitem_track_imageview) ImageView albumArt;
        @InjectView(R.id.listitem_track_name) TextView trackName;
        @InjectView(R.id.listitem_track_album) TextView albumName;

        public ViewHolder(View view) {
            ButterKnife.inject(this, view);
        }
    }
}
