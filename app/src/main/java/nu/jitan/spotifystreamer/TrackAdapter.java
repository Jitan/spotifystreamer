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
        if (convertView != null) {
            mHolder = (ViewHolder) convertView.getTag();
        } else {
            convertView = mInflater.inflate(R.layout.listitem_search, parent, false);
            mHolder = new ViewHolder(convertView);
            convertView.setTag(mHolder);
        }

        MyTrack track = getItem(position);

        mHolder.textView.setText(track.getAlbumName() + " - " + track.getTrackName());
        if (!track.getImgUrl().isEmpty()) {
            Picasso.with(getContext())
                .load(track.getImgUrl())
                .resizeDimen(R.dimen.listitem_imageview, R.dimen
                    .listitem_imageview)
                .centerCrop()
                .into(mHolder.imageView);
        }

        return convertView;
    }

    static class ViewHolder {
        @InjectView(R.id.listitem_search_imageview) ImageView imageView;
        @InjectView(R.id.listitem_search_textview) TextView textView;

        public ViewHolder(View view) {
            ButterKnife.inject(this, view);
        }
    }
}
