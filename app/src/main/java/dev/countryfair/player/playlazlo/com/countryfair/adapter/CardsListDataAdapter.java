package dev.countryfair.player.playlazlo.com.countryfair.adapter;

import android.app.Activity;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;



import org.json.JSONObject;

import java.net.URL;
import java.util.List;

import dev.countryfair.player.playlazlo.com.countryfair.R;
import dev.countryfair.player.playlazlo.com.countryfair.helper.AndroidUtilities;

/**
 * Created by mymac on 3/21/17.
 */

public class CardsListDataAdapter extends ArrayAdapter<JSONObject> {

    private final Activity context;
    private List<JSONObject> cardsDataList;
    private JSONObject cardItem;

    public CardsListDataAdapter(Activity context, List<JSONObject> cardsDataList) {
        super(context, R.layout.cards_list_row, cardsDataList);
        this.context = context;
        this.cardsDataList = cardsDataList;

    }
    @Override
    public View getView(final int position, View view, ViewGroup parent) {

        cardItem = cardsDataList.get(position);
        LayoutInflater inflater = context.getLayoutInflater();
        View rowView= inflater.inflate(R.layout.cards_list_row, null, true);
        TextView txtName = (TextView) rowView.findViewById(R.id.cards_list_name_txt);
        TextView txtDescription = (TextView) rowView.findViewById(R.id.cards_list_description_txt);
        final ImageView imageView = (ImageView) rowView.findViewById(R.id.cards_list_tile_image);
        try {
            txtName.setText(cardItem.getString("merchantName"));
            txtDescription.setText(cardItem.getString("merchantTerms"));
            new Thread(new Runnable() {
                public void run() {
                    try {
                        final String urlStr = cardItem.getString("merchandiseImageUrl");
                        urlStr.replace("https://demolngcdn.blob.core.windows.net", "https://dev2lngmstrasia.blob.core.windows.net");
                        context.runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                AndroidUtilities.loadImage(imageView,urlStr);
                            }

                        });
                    } catch (Exception e) {
                        Log.e("bmp_gettingerr-->", e.getMessage());
                    }
                }
            }).start();

        } catch (Exception ex) {
            Log.e("bmp_gettingerr-->", ex.getMessage());
        }

        return rowView;
    }
}