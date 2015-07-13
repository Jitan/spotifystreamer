package nu.jitan.spotifystreamer.ui.search;

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
import nu.jitan.spotifystreamer.model.MyArtist;

public class SearchAdapter extends ArrayAdapter<MyArtist> {
    private ViewHolder mHolder;
    private LayoutInflater mInflater;

    public SearchAdapter(Context context) {
        super(context, 0);
        mInflater = LayoutInflater.from(context);
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        convertView = setupViewHolder(convertView, parent);

        MyArtist artist = getItem(position);
        mHolder.textView.setText(artist.getName());

        if (!artist.getImgUrl().isEmpty()) {
            String artistThumbUrl = artist.getImgUrl();
            Picasso.with(getContext())
                .load(artistThumbUrl)
                .resizeDimen(R.dimen.listitem_imageview, R.dimen.listitem_imageview)
                .centerCrop()
                .into(mHolder.imageView);
        }
        return convertView;
    }

    private View setupViewHolder(View convertView, ViewGroup parent) {
        if (convertView != null) {
            mHolder = (ViewHolder) convertView.getTag();
        } else {
            convertView = mInflater.inflate(R.layout.listitem_search, parent, false);
            mHolder = new ViewHolder(convertView);
            convertView.setTag(mHolder);
        }
        return convertView;
    }

    public ArrayList<MyArtist> getList() {
        ArrayList<MyArtist> myArtistList = new ArrayList<>();
        for (int i = 0; i < getCount(); i++) {
            myArtistList.add(getItem(i));
        }
        return myArtistList;
    }

    static class ViewHolder {
        @Bind(R.id.listitem_search_imageview) ImageView imageView;
        @Bind(R.id.listitem_search_textview) TextView textView;

        public ViewHolder(View view) {
            ButterKnife.bind(this, view);
        }
    }
}
