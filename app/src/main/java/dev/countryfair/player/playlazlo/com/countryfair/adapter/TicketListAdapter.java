package dev.countryfair.player.playlazlo.com.countryfair.adapter;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.ThumbnailUtils;
import android.os.Environment;
import android.preference.PreferenceManager;
import android.provider.MediaStore;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.daimajia.swipe.SimpleSwipeListener;
import com.daimajia.swipe.SwipeLayout;
import com.daimajia.swipe.adapters.BaseSwipeAdapter;

import org.json.JSONObject;

import java.io.File;
import java.io.FileInputStream;
import java.util.ArrayList;
import java.util.List;

import dev.countryfair.player.playlazlo.com.countryfair.R;
import dev.countryfair.player.playlazlo.com.countryfair.TicketDetailActivty;
import dev.countryfair.player.playlazlo.com.countryfair.helper.AndroidUtilities;
import dev.countryfair.player.playlazlo.com.countryfair.helper.Constants;
import pl.droidsonroids.gif.GifDrawable;

/**
 * Created by mymac on 3/21/17.
 */

public class TicketListAdapter extends BaseSwipeAdapter {
    private Context mContext;

    private List<JSONObject> ticketDataList = new ArrayList<JSONObject>();
    private float startX;
    private float startY;
    private int CLICK_ACTION_THRESHHOLD = 100;

    public TicketListAdapter(Context mContext, List<JSONObject> ticketDataList) {
        this.mContext = mContext;
        this.ticketDataList = ticketDataList;
    }

    @Override
    public int getSwipeLayoutResourceId(int position) {
        return R.id.ticket_list_item_swipe;
    }

    @Override
    public View generateView(final int position, ViewGroup parent) {
        View v = LayoutInflater.from(mContext).inflate(R.layout.ticket_list_row, null);

        SwipeLayout swipeLayout = (SwipeLayout)v.findViewById(getSwipeLayoutResourceId(position));
        swipeLayout.addDrag(SwipeLayout.DragEdge.Left, swipeLayout.findViewById(R.id.ticket_list_wrapper_left));

        swipeLayout.addSwipeListener(new SimpleSwipeListener() {
            @Override
            public void onOpen(SwipeLayout layout) {
                // swipe event listener
            }
        });

        v.findViewById(R.id.ticket_delete).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                if(mOnTicketDeleteListener != null){
                    mOnTicketDeleteListener.onTicketDeleted(position);
                }
            }
        });

        v.findViewById(R.id.ticket_claim_btn).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(mOnTicketClaimListener != null){
                    mOnTicketClaimListener.onTicketClaimed(position);
                }
            }
        });

        v.findViewById(R.id.ticket_delete_btn).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(mOnTicketDeleteListener != null){
                    mOnTicketDeleteListener.onTicketDeleted(position);
                }
            }
        });

        return v;
    }

    @Override
    public void fillValues(final int position, final View convertView) {
        JSONObject ticketItem = ticketDataList.get(position);

        ImageView tileImage = (ImageView) convertView.findViewById(R.id.ticket_list_tile_image);
        ImageView logoImage = (ImageView) convertView.findViewById(R.id.ticket_list_gamelogo_image);
        ImageView previewImage = (ImageView) convertView.findViewById(R.id.ticket_list_preview_img);
        TextView ticketDateTxt = (TextView) convertView.findViewById(R.id.ticket_list_createdon_txt);
        TextView ticketClaimStateTxt = (TextView) convertView.findViewById(R.id.ticket_claim_state_txt);

        final String localFilePath = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS) + "/"+Constants.DIR_ROOT+"/";
        final String localFilePathForTicket = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS) + "/"+Constants.DIR_ROOT+"/"+Constants.DIR_TICKETS+"/";

        try {
            JSONObject gameItem = ticketItem.getJSONObject("gameData");
            // set play count and play price for each shopping cart
            ticketDateTxt.setText(ticketItem.getString("ticketDownloadDate"));

            final String fileName = ticketItem.getString("fileName");
            String staticTileName = gameItem.getString("tileUrl");
            String animatedTileName = gameItem.getString("animatedTileUrl");
            String logoName = gameItem.getString("gameLogoUrl");
            boolean ticketClaimState = ticketItem.getBoolean("isClaimed");
            if (ticketClaimState) {
                ticketClaimStateTxt.setVisibility(View.VISIBLE);
            } else {
                ticketClaimStateTxt.setVisibility(View.GONE);
            }

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
                        if (sharedPref.getBoolean("ticketRecentFlag", false)) {
                            tileImage.setImageDrawable(animatedTileImage);
                        } else {
                            AndroidUtilities.loadImage(tileImage,staticImgFile);
                        }
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

            File ticketFile = new File(localFilePathForTicket + fileName);

            if (ticketFile.exists()) {
                if (fileName.contains("mp4")) {
                    Bitmap bMap = ThumbnailUtils.createVideoThumbnail(ticketFile.getAbsolutePath(), MediaStore.Video.Thumbnails.MICRO_KIND);
                    previewImage.setImageBitmap(bMap);
                } else {
                    AndroidUtilities.loadImage(previewImage,ticketFile);
                }
            }
            convertView.setOnTouchListener(new View.OnTouchListener() {
                @Override
                public boolean onTouch(View v, MotionEvent event) {

                    switch (event.getAction()) {

                        case MotionEvent.ACTION_DOWN:
                            startX = event.getX();
                            startY = event.getY();
                            break;
                        case MotionEvent.ACTION_UP:
                            float endX = event.getX();
                            float endY = event.getY();
                            if (isAClick(startX, endX, startY, endY)) {
                                Intent i = new Intent(mContext, TicketDetailActivty.class);
                                i.putExtra("ticketFilePath", localFilePathForTicket + fileName);
                                mContext.startActivity(i);
                            }
                            break;
                    }

                    return false;
                }
            });
        } catch (Exception e) {
            Log.e("ChannelGroup-->", e.getMessage());
        }
    }

    private boolean isAClick(float startX, float endX, float startY, float endY) {
        float differenceX = Math.abs(startX - endX);
        float differenceY = Math.abs(startY - endY);
        if (differenceX > CLICK_ACTION_THRESHHOLD/* =5 */ || differenceY > CLICK_ACTION_THRESHHOLD) {
            return false;
        }
        return true;
    }

    @Override
    public int getCount() {
        return ticketDataList.size();
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
    public interface OnTicketClaimListener{
        public void onTicketClaimed(int position);
    }

    public interface OnTicketDeleteListener{
        public void onTicketDeleted(int position);
    }

    OnTicketClaimListener mOnTicketClaimListener;
    OnTicketDeleteListener mOnTicketDeleteListener;
    public void setOnTicketClaimListener(OnTicketClaimListener onTicketClaimListener){
        mOnTicketClaimListener = onTicketClaimListener;
    }

    public void setOnTicketDeleteListener(OnTicketDeleteListener onTicketDeleteListener){
        mOnTicketDeleteListener = onTicketDeleteListener;
    }
}
