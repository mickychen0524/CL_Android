package dev.countryfair.player.playlazlo.com.countryfair.adapter;

/**
 * Created by nyam on 2/17/17.
 */

import android.app.Activity;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.ColorDrawable;
import android.os.Environment;
import android.preference.PreferenceManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.FileInputStream;
import java.text.DateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import dev.countryfair.player.playlazlo.com.countryfair.R;
import dev.countryfair.player.playlazlo.com.countryfair.helper.APIInterface;
import dev.countryfair.player.playlazlo.com.countryfair.helper.AdvancedHTTPClient;
import dev.countryfair.player.playlazlo.com.countryfair.helper.AndroidUtilities;
import dev.countryfair.player.playlazlo.com.countryfair.helper.AppHelper;
import dev.countryfair.player.playlazlo.com.countryfair.helper.Constants;
import dev.countryfair.player.playlazlo.com.countryfair.model.ChannelItemModel;
import pl.droidsonroids.gif.GifDrawable;

public class ChannelListDataAdapter extends RecyclerView.Adapter<ChannelListDataAdapter.SingleItemRowHolder> {

    private ArrayList<ChannelItemModel> itemsList;
    private Context mContext;

    private JSONObject receivedObj = new JSONObject();

    private List<JSONObject> drawDataList = new ArrayList<>();

    private List<String> drawStrList = new ArrayList<String>();
    private List<String> priceStrList = new ArrayList<String>();
    private List<String> playAmountStrList = new ArrayList<String>();

    private String drawRefId = "";
    private String gameRefId = "";
    private String brandRefid = "";
    private String channelGroupRefId = "";
    private String brandLogoStr = "";
    private String gameLogoStr = "";


    private int i_playNumber = 0;
    private float f_playPrice = 0.0f;

    public ChannelListDataAdapter(Context context,
                                  ArrayList<ChannelItemModel> itemsList,
                                  String gameRefId,
                                  String brandRefId,
                                  String channelGroupRefId,
                                  String gameLogoStr,
                                  String brandLogoStr,
                                  List<String> priceList,
                                  List<String> playNumberList) {
        this.itemsList = itemsList;
        this.mContext = context;
        this.gameRefId = gameRefId;
        this.brandRefid = brandRefId;
        this.channelGroupRefId = channelGroupRefId;
        this.gameLogoStr = gameLogoStr;
        this.brandLogoStr = brandLogoStr;
        this.priceStrList = priceList;
        this.playAmountStrList = playNumberList;
    }

    @Override
    public SingleItemRowHolder onCreateViewHolder(ViewGroup viewGroup, int i) {
        View v = LayoutInflater.from(mContext).inflate(R.layout.main_channelgroup_list_card, null);
        SingleItemRowHolder mh = new SingleItemRowHolder(v);
        return mh;
    }

    @Override
    public void onBindViewHolder(SingleItemRowHolder holder, final int i) {
        String localFilePath = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS) + "/"+Constants.DIR_ROOT+"/";

        String fileName = "";
        String fileAnimatedName = "";
        try {
            JSONObject ticketTemplate = itemsList.get(i).getChannelData().getJSONObject("ticketTemplate");
            fileName = ticketTemplate.getString("tileUrl");
            fileAnimatedName = ticketTemplate.getString("tileAnimatedUrl");
        } catch (Exception e) {
            Log.e("Channel-->", e.getMessage());
        }

        File imgFile = new File(localFilePath + fileName);
        File gifFile = new File(localFilePath + fileAnimatedName);
        if(imgFile.exists()){

            try {
                GifDrawable animatedTileImage = new GifDrawable(gifFile);
                SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(mContext);
                if (sharedPref.getBoolean("powerSavingState", false)) {
                    AndroidUtilities.loadImage(holder.itemImage,imgFile);
                } else {
                    holder.itemImage.setImageDrawable(animatedTileImage);
                }
            } catch (Exception e) {
                Log.e("input stream error -->", e.getMessage());
            }
        }

        holder.view.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                getDrawList(itemsList.get(i).getChannelData());

            }
        });
        /* Glide.with(mContext)
                .load(feedItem.getImageURL())
                .diskCacheStrategy(DiskCacheStrategy.ALL)
                .centerCrop()
                .error(R.drawable.bg)
                .into(feedListRowHolder.thumbView);*/
    }

    @Override
    public int getItemCount() {
        return (null != itemsList ? itemsList.size() : 0);
    }

    public class SingleItemRowHolder extends RecyclerView.ViewHolder {

        protected ImageView itemImage;
        protected View view;

        public SingleItemRowHolder(View view) {
            super(view);

            this.itemImage = (ImageView) view.findViewById(R.id.itemImage);
            this.view = view;
        }

    }

    private void getDrawList(final JSONObject channelData) {


        final Activity activity = (Activity) mContext;
        final ProgressDialog mLoginProgressDialog = new ProgressDialog(mContext);
        mLoginProgressDialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        mLoginProgressDialog.setMessage("Getting Draw List...");
        mLoginProgressDialog.show();
        mLoginProgressDialog.setCancelable(false);
        mLoginProgressDialog.setCanceledOnTouchOutside(false);
        new Thread(new Runnable() {
            public void run() {
                try {
                    receivedObj = APIInterface.getDrawList(gameRefId);

                    activity.runOnUiThread(new Runnable() {
                        @Override
                        public void run() {

                            if (mLoginProgressDialog.isShowing()) {
                                mLoginProgressDialog.dismiss();
                            }

                            if (receivedObj != null) {

                                try {
                                    JSONArray jsonArr = receivedObj.getJSONArray("data");
                                    drawDataList = AppHelper.parseFromJsonList(jsonArr);

                                    for (JSONObject obj : drawDataList) {
                                        drawStrList.add(obj.getString("closeScheduledOn"));
                                    }

                                    if (jsonArr.length() != 0) {
                                        createAddShoppingCartDialog(channelData);
                                    } else {
                                        Toast.makeText(mContext, "Draw list is empty", Toast.LENGTH_SHORT).show();
                                    }

                                } catch (JSONException e) {
                                    Log.d("json_e-->", e.getMessage());
                                }
                            } else {
                                Log.e("game_list--->", "receivedObj is null");
                                Toast.makeText(mContext, "Get Draw List Error", Toast.LENGTH_SHORT).show();
                            }

                        }
                    });
                } catch (Exception e) {
                    Log.e("game_list--->", e.getMessage());
                    activity.runOnUiThread(new Runnable() {
                        @Override
                        public void run() {

                            if (mLoginProgressDialog.isShowing()) {
                                mLoginProgressDialog.dismiss();
                            }
                            Toast.makeText(mContext, "Get Draw List Error", Toast.LENGTH_SHORT).show();
                        }
                    });

                }
            }
        }).start();
    }

    private void createAddShoppingCartDialog(final JSONObject channelData) {

        final SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(mContext);
        String localFilePath = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS) + "/"+Constants.DIR_ROOT+"/";

        i_playNumber = Integer.parseInt(playAmountStrList.get(0));
        f_playPrice = Float.parseFloat(priceStrList.get(0));

        final Dialog dialog = new Dialog(mContext);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.getWindow().setBackgroundDrawable(new ColorDrawable(android.graphics.Color.TRANSPARENT));
        dialog.setContentView(R.layout.modal_add_shoppingcart_layout);

        final Spinner drawSpinner = (Spinner) dialog.findViewById(R.id.modal_draw_list);
        final Spinner priceSpinner = (Spinner) dialog.findViewById(R.id.modal_price_list);
        final Spinner playAmountSpinner = (Spinner) dialog.findViewById(R.id.modal_playnumber_list);

        final TextView totalPriceTxt = (TextView) dialog.findViewById(R.id.modal_total_price_txt);

        ArrayAdapter<String> drawAdapter = new ArrayAdapter<String>(mContext, android.R.layout.simple_spinner_item, drawStrList);
        ArrayAdapter<String> priceAdapter = new ArrayAdapter<String>(mContext, android.R.layout.simple_spinner_item, priceStrList);
        ArrayAdapter<String> playAmountAdapter = new ArrayAdapter<String>(mContext, android.R.layout.simple_spinner_item, playAmountStrList);

        drawAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        priceAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        playAmountAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);

        drawSpinner.setAdapter(drawAdapter);
        priceSpinner.setAdapter(priceAdapter);
        playAmountSpinner.setAdapter(playAmountAdapter);


        // spinner selected listener
        drawSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                TextView tv = (TextView)view.findViewById(android.R.id.text1);

                String displayText = drawStrList.get(position);
                displayText = AndroidUtilities.DateUtils.getFormattedDate("MM/dd HH/mm","yyyy-MM-dd'T'HH:mm:ss",displayText,AndroidUtilities.APP_TIMEZONE,AndroidUtilities.APP_TIMEZONE);
                tv.setText("Draw  " + displayText);

                view.setPadding(0, view.getPaddingTop(), view.getPaddingRight(), view.getPaddingBottom());
                view.invalidate();
                JSONObject selectedDrawObj = drawDataList.get(position);
                try {
                    drawRefId = selectedDrawObj.getString("drawRefId");
                } catch (JSONException e) {
                    Log.d("json_e-->", e.getMessage());
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });
        priceSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                f_playPrice = Float.parseFloat(priceStrList.get(position));
                TextView tv = (TextView)view.findViewById(android.R.id.text1);
                tv.setText("Play Amount  $" + priceStrList.get(position));
                totalPriceTxt.setText("Total  $" + String.valueOf(String.format("%.2f", i_playNumber * f_playPrice)));
                view.setPadding(0, view.getPaddingTop(), view.getPaddingRight(), view.getPaddingBottom());
                view.invalidate();

            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });
        playAmountSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                i_playNumber = Integer.parseInt(playAmountStrList.get(position));
                TextView tv = (TextView)view.findViewById(android.R.id.text1);
                tv.setText("Number Of Plays  " + playAmountStrList.get(position));
                totalPriceTxt.setText("Total  $" + String.valueOf(String.format("%.2f", i_playNumber * f_playPrice)));
                view.setPadding(0, view.getPaddingTop(), view.getPaddingRight(), view.getPaddingBottom());
                view.invalidate();

            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });

        ImageView tileImage = (ImageView) dialog.findViewById(R.id.shopping_cart_tile_image);
        ImageView gameLogoImage = (ImageView) dialog.findViewById(R.id.shopping_cart_game_logo);
        ImageView brandLogoImage = (ImageView) dialog.findViewById(R.id.shopping_cart_brand_logo);

        String fileName = "";
        String fileAnimatedName = "";
        try {
            JSONObject ticketTemplate = channelData.getJSONObject("ticketTemplate");
            fileName = ticketTemplate.getString("tileUrl");
            fileAnimatedName = ticketTemplate.getString("tileAnimatedUrl");
        } catch (Exception e) {
            Log.e("Channel-->", e.getMessage());
        }

        File imgFile = new File(localFilePath + fileName);
        File gifFile = new File(localFilePath + fileAnimatedName);
        if(imgFile.exists()){

            try {
                GifDrawable animatedTileImage = new GifDrawable(gifFile);
                if (sharedPref.getBoolean("powerSavingState", false)) {
                    AndroidUtilities.loadImage(tileImage,imgFile);
                } else {
                    tileImage.setImageDrawable(animatedTileImage);
                }
            } catch (Exception e) {
                Log.e("input stream error -->", e.getMessage());
            }
        }

        File imgBrandFile = new File(localFilePath + brandLogoStr);

        if(imgBrandFile.exists()){

            try {
                AndroidUtilities.loadImage(brandLogoImage,imgBrandFile);
            } catch (Exception e) {
                Log.e("input stream error -->", e.getMessage());
            }

        }

        File imgGameFile = new File(localFilePath + gameLogoStr);

        if(imgGameFile.exists()){

            try {
                AndroidUtilities.loadImage(gameLogoImage,imgGameFile);
            } catch (Exception e) {
                Log.e("input stream error -->", e.getMessage());
            }

        }



        Button addBtn = (Button) dialog.findViewById(R.id.shopping_cart_add);
        Button cancelBtn = (Button) dialog.findViewById(R.id.shopping_cart_cancel);

        addBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                JSONObject sendData = new JSONObject();
                String currentDateTimeString = DateFormat.getDateTimeInstance().format(new Date());
                try {
                    JSONObject ticketTemplate = channelData.getJSONObject("ticketTemplate");

                    sendData.put("gameRefId", gameRefId);
                    sendData.put("brandRefId", brandRefid);
                    sendData.put("drawRefId", drawRefId);
                    sendData.put("channelGroupRefId", channelGroupRefId);
                    sendData.put("channelRefId", channelData.getString("channelRefId"));
                    sendData.put("playAmount", f_playPrice);
                    sendData.put("panelCount", i_playNumber);
                    sendData.put("addedOn", currentDateTimeString);
                    sendData.put("tileUrl", ticketTemplate.getString("tileAnimatedUrl"));
                    sendData.put("tileStaticUrl", ticketTemplate.getString("tileUrl"));
                    sendData.put("animatedState", true);
                    sendData.put("gameLogoUrl", gameLogoStr);
                    sendData.put("templateRefId", ticketTemplate.getString("ticketTemplateRefId"));
                    sendData.put("channelTemplateData", channelData);
                } catch (JSONException e) {
                    Log.d("json_e-->", e.getMessage());
                }

                new AppHelper(mContext).saveOneShoppingCartToLocal(sendData);
                Toast.makeText(mContext, "shopping cart added successfully", Toast.LENGTH_SHORT).show();

                dialog.dismiss();
            }
        });

        cancelBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.dismiss();
            }
        });

        dialog.show();
         AndroidUtilities.setDialogLayoutParams((Activity) mContext,dialog);

        if (!sharedPref.getBoolean("addToCartHelpOverlayHasShown", false)) {
            final Dialog dialogHelp = new Dialog(mContext);
            dialogHelp.requestWindowFeature(Window.FEATURE_NO_TITLE);
            dialogHelp.getWindow().setBackgroundDrawable(new ColorDrawable(android.graphics.Color.TRANSPARENT));
            dialogHelp.setContentView(R.layout.modal_help_shoppingcart_layout);

            TextView helpTxt = (TextView) dialogHelp.findViewById(R.id.modal_help_overlay);
            helpTxt.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    SharedPreferences.Editor editor = sharedPref.edit();
                    editor.putBoolean("addToCartHelpOverlayHasShown", true);
                    editor.apply();
                    dialogHelp.dismiss();
                }
            });

            dialogHelp.show();
        }
    }
}
