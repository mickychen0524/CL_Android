package lite.storeclerk.admin.playlazlo.com.storeclerklite.adapter;

import android.app.Activity;
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

import lite.storeclerk.admin.playlazlo.com.storeclerklite.R;
import lite.storeclerk.admin.playlazlo.com.storeclerklite.helper.AndroidUtilities;
import lite.storeclerk.admin.playlazlo.com.storeclerklite.helper.AppHelper;

/**
 * Created by mymac on 3/18/17.
 */

public class MainGameListAdapter extends ArrayAdapter<JSONObject> {

    private final Activity context;
    private List<JSONObject> gameArr;
    private List<JSONObject> prizesArr;

    public MainGameListAdapter(Activity context, List<JSONObject> gameArr) {
        super(context, R.layout.main_activity_list_row, gameArr);
        this.context = context;
        this.gameArr = gameArr;

    }
    @Override
    public View getView(int position, View view, ViewGroup parent) {

        final JSONObject gameItem = gameArr.get(position);
        LayoutInflater inflater = context.getLayoutInflater();
        View rowView= inflater.inflate(R.layout.main_activity_list_row, null, true);
        TextView txtPrice = (TextView) rowView.findViewById(R.id.main_game_price_txt);
        TextView txtGameDate = (TextView) rowView.findViewById(R.id.main_game_date_txt);
        final ImageView imageView = (ImageView) rowView.findViewById(R.id.main_game_tile_image);
        try {
            prizesArr = AppHelper.parseFromJsonList(gameItem.getJSONArray("prizes"));

            double maxPrice = 0.0;
            JSONObject maxPrizeObj = new JSONObject();
            for (JSONObject prizeItem : prizesArr) {
                if (maxPrice < prizeItem.getDouble("amountAtOpen")) {
                    maxPrice = prizeItem.getDouble("amountAtOpen");
                    maxPrizeObj = prizeItem;
                }
            }
            txtPrice.setText("$" + String.valueOf(maxPrice));
            txtGameDate.setText("3/25/2017");

            new Thread(new Runnable() {
                public void run() {
                    try {
                        final String urlStr = gameItem.getString("logoUrl");
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

        }

        return rowView;
    }
}