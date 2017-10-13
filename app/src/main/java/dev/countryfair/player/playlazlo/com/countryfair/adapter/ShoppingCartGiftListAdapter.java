package dev.countryfair.player.playlazlo.com.countryfair.adapter;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Environment;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.daimajia.swipe.SimpleSwipeListener;
import com.daimajia.swipe.SwipeLayout;
import com.daimajia.swipe.adapters.BaseSwipeAdapter;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import dev.countryfair.player.playlazlo.com.countryfair.R;
import dev.countryfair.player.playlazlo.com.countryfair.helper.AndroidUtilities;
import dev.countryfair.player.playlazlo.com.countryfair.helper.AppHelper;
import pl.droidsonroids.gif.GifDrawable;

/**
 * Created by mymac on 3/16/17.
 */

public class ShoppingCartGiftListAdapter extends BaseSwipeAdapter {
    private Activity mContext;

    private JSONObject shoppingCartItem = new JSONObject();
    private List<JSONObject> shoppingCartArr = new ArrayList<JSONObject>();

    public ShoppingCartGiftListAdapter(Activity mContext, List<JSONObject> shoppingCartArr) {
        this.mContext = mContext;
        this.shoppingCartArr = shoppingCartArr;
    }

    @Override
    public int getSwipeLayoutResourceId(int position) {
        return R.id.shopping_cart_list_item_swipe;
    }

    @Override
    public View generateView(final int position, ViewGroup parent) {
        View v = LayoutInflater.from(mContext).inflate(R.layout.shopping_cart_gift_list_row, null);
        SwipeLayout swipeLayout = (SwipeLayout)v.findViewById(getSwipeLayoutResourceId(position));
        swipeLayout.addSwipeListener(new SimpleSwipeListener() {
            @Override
            public void onOpen(SwipeLayout layout) {
                // swipe event listener
            }
        });
        v.findViewById(R.id.delete_all).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                new AppHelper(mContext).saveInitShoppingCartGift();
                shoppingCartArr.clear();
                if(mOnDataChangeListener != null){
                    mOnDataChangeListener.onDataChanged(1);
                }
            }
        });
        v.findViewById(R.id.btnDelete).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                new AppHelper(mContext).deleteOneShoppingCartItemGift(shoppingCartItem,position);
                shoppingCartArr.remove(position);
                if(mOnDataChangeListener != null){
                    mOnDataChangeListener.onDataChanged(1);
                }
            }
        });
        return v;
    }

    @Override
    public void fillValues(final int position, View rowView) {
        shoppingCartItem = shoppingCartArr.get(position);

        TextView txtName = (TextView) rowView.findViewById(R.id.giftcards_list_name_txt);
        TextView txtLowVal = (TextView) rowView.findViewById(R.id.giftcards_list_lowvalue_txt);

        final ImageView imageView = (ImageView) rowView.findViewById(R.id.giftcards_list_tile_image);

        try {
            txtName.setText(shoppingCartItem.getString("merchantName"));

            JSONArray priceArr = shoppingCartItem.getJSONArray("ranges");
            if (priceArr.length() > 0) {
                JSONObject rangeItem = priceArr.getJSONObject(0);
                txtLowVal.setText("$" + String.valueOf(shoppingCartItem.getDouble("Value")));
            }
            new Thread(new Runnable() {
                public void run() {
                    try {
                        final String urlStr = shoppingCartItem.getString("merchandiseImageUrl");
                        urlStr.replace("https://demolngcdn.blob.core.windows.net", "https://dev2lngmstrasia.blob.core.windows.net");
                        mContext.runOnUiThread(new Runnable() {
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
    }

    @Override
    public int getCount() {
        return shoppingCartArr.size();
    }

    @Override
    public Object getItem(int position) {
        return null;
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    // adapter interface to update activity textview value
    public interface OnDataChangeListener{
        public void onDataChanged(int size);
    }
    OnDataChangeListener mOnDataChangeListener;
    public void setOnDataChangeListener(OnDataChangeListener onDataChangeListener){
        mOnDataChangeListener = onDataChangeListener;
    }
}
