package dev.countryfair.player.playlazlo.com.countryfair.adapter;

import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
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
import com.squareup.picasso.Picasso;


import org.json.JSONObject;

import java.io.File;
import java.io.FileInputStream;
import java.util.ArrayList;
import java.util.List;

import dev.countryfair.player.playlazlo.com.countryfair.R;
import dev.countryfair.player.playlazlo.com.countryfair.helper.AndroidUtilities;
import dev.countryfair.player.playlazlo.com.countryfair.helper.AppHelper;
import dev.countryfair.player.playlazlo.com.countryfair.helper.Constants;
import pl.droidsonroids.gif.GifDrawable;

/**
 * Created by mymac on 3/16/17.
 */

public class ShoppingCartListAdapter extends BaseSwipeAdapter {
    private Context mContext;

    private JSONObject shoppingCartItem = new JSONObject();
    private List<JSONObject> shoppingCartArr = new ArrayList<JSONObject>();

    public ShoppingCartListAdapter(Context mContext, List<JSONObject> shoppingCartArr) {
        this.mContext = mContext;
        this.shoppingCartArr = shoppingCartArr;
    }

    @Override
    public int getSwipeLayoutResourceId(int position) {
        return R.id.shopping_cart_list_item_swipe;
    }

    @Override
    public View generateView(final int position, ViewGroup parent) {
        View v = LayoutInflater.from(mContext).inflate(R.layout.shopping_cart_list_row, null);
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
                new AppHelper(mContext).saveInitShoppingCart();
                shoppingCartArr.clear();
                if(mOnDataChangeListener != null){
                    mOnDataChangeListener.onDataChanged(1);
                }
            }
        });
        v.findViewById(R.id.delete).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                new AppHelper(mContext).deleteOneShoppingCartItem(shoppingCartItem);
                shoppingCartArr.remove(position);
                if(mOnDataChangeListener != null){
                    mOnDataChangeListener.onDataChanged(1);
                }
            }
        });
        return v;
    }

    @Override
    public void fillValues(final int position, View convertView) {
        shoppingCartItem = shoppingCartArr.get(position);
        ImageView tileImage = (ImageView) convertView.findViewById(R.id.shopping_cart_list_tile_image);
        ImageView logoImage = (ImageView) convertView.findViewById(R.id.shopping_cart_list_gamelogo_image);
        TextView numberPlay = (TextView) convertView.findViewById(R.id.shopping_cart_list_playnumber_txt);
        TextView subtotalPrice = (TextView) convertView.findViewById(R.id.shopping_cart_list_subtotalprice_txt);

        String localFilePath = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS) + "/"+ Constants.DIR_ROOT+"/";

        Button plusShoppingCartbtn = (Button) convertView.findViewById(R.id.shopping_cart_list_plus_btn);
        Button minusShoppingCartbtn = (Button) convertView.findViewById(R.id.shopping_cart_list_minus_btn);

        plusShoppingCartbtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                try {
                    int i_newCount = shoppingCartItem.getInt("panelCount") + 1;
                    shoppingCartItem.put("panelCount", i_newCount);
                    new AppHelper(mContext).updateOneShoppingCartToLocal(shoppingCartItem);
                    shoppingCartArr.set(position, shoppingCartItem);
                    if(mOnDataChangeListener != null){
                        mOnDataChangeListener.onDataChanged(0);
                    }
                } catch (Exception e) {
                    Log.e("json parsing error -->", e.getMessage());
                }
            }
        });

        minusShoppingCartbtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                try {
                    if (shoppingCartItem.getInt("panelCount") == 1) {
                        new AppHelper(mContext).deleteOneShoppingCartItem(shoppingCartItem);
                        shoppingCartArr.remove(position);
                    } else {
                        int i_newCount = shoppingCartItem.getInt("panelCount") - 1;
                        shoppingCartItem.put("panelCount", i_newCount);
                        new AppHelper(mContext).updateOneShoppingCartToLocal(shoppingCartItem);
                        shoppingCartArr.set(position, shoppingCartItem);
                    }
                    if(mOnDataChangeListener != null){
                        mOnDataChangeListener.onDataChanged(1);
                    }
                } catch (Exception e) {
                    Log.e("json parsing error -->", e.getMessage());
                }
            }
        });

        try {

            // set play count and play price for each shopping cart
            numberPlay.setText(String.valueOf(shoppingCartItem.getInt("panelCount")));
            String price = String.format("$%.2f", shoppingCartItem.getDouble("playAmount"));
            subtotalPrice.setText(price);

            String animatedTileName = shoppingCartItem.getString("tileUrl");
            String staticTileName = shoppingCartItem.getString("tileStaticUrl");
            String logoName = shoppingCartItem.getString("gameLogoUrl");

            File staticImgFile = new File(localFilePath + staticTileName);
            File animatedImgFile = new File(localFilePath + animatedTileName);
            File logoFile = new File(localFilePath + logoName);


            if(staticImgFile.exists()){

                try {
                    GifDrawable animatedTileImage = new GifDrawable(animatedImgFile);
                    final SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(mContext);
                    if (sharedPref.getBoolean("powerSavingState", false)) {
                        AndroidUtilities.loadImage(tileImage,staticImgFile);
                    } else {
                        tileImage.setImageDrawable(animatedTileImage);
                    }
                } catch (Exception e) {
                    Log.e("input stream error -->", e.getMessage());
                }
            }

            if(logoFile.exists()){

                try {
                    AndroidUtilities.loadImage(logoImage,logoFile);
                } catch (Exception e) {
                    Log.e("input stream error -->", e.getMessage());
                }

            }

        } catch (Exception e) {
            Log.e("ChannelGroup-->", e.getMessage());
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
