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
import android.widget.ImageView;

import com.daimajia.swipe.SimpleSwipeListener;
import com.daimajia.swipe.SwipeLayout;
import com.daimajia.swipe.adapters.BaseSwipeAdapter;

import org.json.JSONObject;

import java.io.File;
import java.io.FileInputStream;
import java.util.ArrayList;
import java.util.List;

import dev.countryfair.player.playlazlo.com.countryfair.R;
import dev.countryfair.player.playlazlo.com.countryfair.helper.AndroidUtilities;
import dev.countryfair.player.playlazlo.com.countryfair.helper.Constants;
import pl.droidsonroids.gif.GifDrawable;

/**
 * Created by mymac on 3/21/17.
 */

public class TicketRefundListAdapter extends BaseSwipeAdapter {
    private Context mContext;

    private List<JSONObject> ticketErrprDataList = new ArrayList<JSONObject>();

    public TicketRefundListAdapter(Context mContext, List<JSONObject> ticketErrprDataList) {
        this.mContext = mContext;
        this.ticketErrprDataList = ticketErrprDataList;
    }

    @Override
    public int getSwipeLayoutResourceId(int position) {
        return R.id.ticket_refund_list_item_swipe;
    }

    @Override
    public View generateView(final int position, ViewGroup parent) {
        View v = LayoutInflater.from(mContext).inflate(R.layout.ticket_refund_row, null);
        SwipeLayout swipeLayout = (SwipeLayout)v.findViewById(getSwipeLayoutResourceId(position));
        swipeLayout.addSwipeListener(new SimpleSwipeListener() {
            @Override
            public void onOpen(SwipeLayout layout) {
                // swipe event listener
            }
        });
        v.findViewById(R.id.ticket_refund).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                if(mOnTicketRefundListener != null){
                    mOnTicketRefundListener.onTicketRefunded(position);
                }
            }
        });
        return v;
    }

    @Override
    public void fillValues(final int position, View convertView) {
        JSONObject ticketItem = ticketErrprDataList.get(position);

        ImageView tileImage = (ImageView) convertView.findViewById(R.id.ticket_refund_list_tile_image);
        ImageView logoImage = (ImageView) convertView.findViewById(R.id.ticket_refund_list_gamelogo_image);

        final String localFilePath = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS) + "/"+ Constants.DIR_ROOT+"/";

        try {
            JSONObject gameItem = ticketItem.getJSONObject("gameData");
            String staticTileName = gameItem.getString("tileUrl");
            String animatedTileName = gameItem.getString("animatedTileUrl");
            String logoName = gameItem.getString("gameLogoUrl");

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
        return ticketErrprDataList.size();
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
    public interface OnTicketRefundListener{
        public void onTicketRefunded(int position);
    }
    OnTicketRefundListener mOnTicketRefundListener;
    public void setOnTicketRefundListener(OnTicketRefundListener onTicketRefundListener){
        mOnTicketRefundListener = onTicketRefundListener;
    }
}
