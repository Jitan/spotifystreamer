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
import kaaes.spotify.webapi.android.models.Artist;

public class SearchAdapter extends ArrayAdapter<Artist> {
    private ViewHolder mHolder;
    private LayoutInflater mInflater;

    public SearchAdapter(Context context) {
        super(context, 0);
        mInflater = LayoutInflater.from(context);
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        if (convertView != null) {
            mHolder = (ViewHolder) convertView.getTag();
        } else {
            convertView = mInflater.inflate(R.layout.list_item_search, parent, false);
            mHolder = new ViewHolder(convertView);
            convertView.setTag(mHolder);
        }

        mHolder.textView.setText(getItem(position).name);

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
