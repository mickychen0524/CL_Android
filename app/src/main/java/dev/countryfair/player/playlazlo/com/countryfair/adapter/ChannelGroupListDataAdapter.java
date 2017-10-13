package dev.countryfair.player.playlazlo.com.countryfair.adapter;

/**
 * Created by nyam on 2/17/17.
 */

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Environment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.File;
import java.io.FileInputStream;
import java.util.ArrayList;
import java.util.List;

import dev.countryfair.player.playlazlo.com.countryfair.R;
import dev.countryfair.player.playlazlo.com.countryfair.helper.AndroidUtilities;
import dev.countryfair.player.playlazlo.com.countryfair.helper.AppHelper;
import dev.countryfair.player.playlazlo.com.countryfair.helper.Constants;
import dev.countryfair.player.playlazlo.com.countryfair.model.ChannelGroupDataModel;
import dev.countryfair.player.playlazlo.com.countryfair.model.ChannelItemModel;


public class ChannelGroupListDataAdapter extends RecyclerView.Adapter<ChannelGroupListDataAdapter.ItemRowHolder> {

    private ArrayList<ChannelGroupDataModel> dataList = new ArrayList<>();
    private List<JSONObject> gameList = new ArrayList<>();
    private List<JSONObject> brandList = new ArrayList<>();
    private List<JSONObject> channelGroupList = new ArrayList<>();
    private List<String> priceStrList = new ArrayList<String>();
    private List<String> playAmountStrList = new ArrayList<String>();

    private JSONObject channelGroupData = new JSONObject();

    private String gameRefId = "";
    private String brandRefId = "";
    private String channelGroupRefid = "";

    private String gameLogoStr = "";
    private String brandLogoStr = "";

    private Context mContext;

    private JSONArray amountArr = new JSONArray();
    private int maxAmount = 0;

    public ChannelGroupListDataAdapter(Context context, List<JSONObject> gameList, List<JSONObject> brandList, List<JSONObject> channelGroupList) {

        for (JSONObject obj : channelGroupList) {

            ChannelGroupDataModel dm = new ChannelGroupDataModel();

            ArrayList<ChannelItemModel> singleItem = new ArrayList<ChannelItemModel>();
            List<JSONObject> channelData = new ArrayList<>();
            try {
                channelData = AppHelper.parseFromJsonList(obj.getJSONArray("channels"));
                for (JSONObject objChannel : channelData) {

                    singleItem.add(new ChannelItemModel(objChannel));
                }

            } catch (Exception jsonE) {
                Log.e("ChannelGroup-->", jsonE.getMessage());
            }

            dm.setAllItemsInSection(singleItem);

            this.dataList.add(dm);

        }
        this.mContext = context;
        this.gameList = gameList;
        this.brandList = brandList;
        this.channelGroupList = channelGroupList;
    }

    @Override
    public ItemRowHolder onCreateViewHolder(ViewGroup viewGroup, int i) {
        View v = LayoutInflater.from(mContext).inflate(R.layout.main_channelgroup_list, null);
        ItemRowHolder mh = new ItemRowHolder(v);
        return mh;
    }

    @Override
    public void onBindViewHolder(ItemRowHolder itemRowHolder, int i) {
        String localFilePath = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS) + "/"+ Constants.DIR_ROOT+"/";
        ArrayList singleSectionItems = dataList.get(i).getAllItemsInSection();
        channelGroupData = channelGroupList.get(i);
        try {
            gameRefId = channelGroupData.getString("gameRefId");
            brandRefId = channelGroupData.getString("brandRefId");
            channelGroupRefid = channelGroupData.getString("channelGroupRefId");

            gameLogoStr = findGameAndBrandImageUrl(gameRefId, 1);
            brandLogoStr = findGameAndBrandImageUrl(brandRefId, 2);
        } catch (Exception e) {
            Log.e("ChannelGroup-->", e.getMessage());
        }

        ChannelListDataAdapter itemListDataAdapter = new ChannelListDataAdapter(mContext,
                singleSectionItems,
                gameRefId,
                brandRefId,
                channelGroupRefid,
                gameLogoStr,
                brandLogoStr,
                priceStrList,
                playAmountStrList);

        itemRowHolder.recycler_view_list.setHasFixedSize(true);
        itemRowHolder.recycler_view_list.setLayoutManager(new LinearLayoutManager(mContext, LinearLayoutManager.HORIZONTAL, false));
        itemRowHolder.recycler_view_list.setAdapter(itemListDataAdapter);
        File imgFile = new File(localFilePath + gameLogoStr);
        if(imgFile.exists()){
            try {
                AndroidUtilities.loadImage(itemRowHolder.leftImage,imgFile);
            } catch (Exception e) {
                Log.e("input stream error -->", e.getMessage());
            }

        }
        imgFile = new File(localFilePath + brandLogoStr);
        if(imgFile.exists()){

            try {
                AndroidUtilities.loadImage(itemRowHolder.rightImage,imgFile);
            } catch (Exception e) {
                Log.e("input stream error -->", e.getMessage());
            }

        }

        itemRowHolder.recycler_view_list.setNestedScrollingEnabled(false);

    }

    @Override
    public int getItemCount() {
        return (null != dataList ? dataList.size() : 0);
    }

    public class ItemRowHolder extends RecyclerView.ViewHolder {

        protected ImageView leftImage;

        protected RecyclerView recycler_view_list;

        protected ImageView rightImage;

        public ItemRowHolder(View view) {
            super(view);

            this.leftImage = (ImageView) view.findViewById(R.id.imageLeft);
            this.recycler_view_list = (RecyclerView) view.findViewById(R.id.recycler_view_list);
            this.rightImage= (ImageView) view.findViewById(R.id.imgRight);

        }

    }

    private String findGameAndBrandImageUrl(String id, int type) {

        switch (type) {
            case 1:
                for (JSONObject gameObj : gameList) {
                    try {
                        priceStrList = new ArrayList<String>();
                        playAmountStrList = new ArrayList<String>();
                        maxAmount = 0;
                        if (gameObj.getString("gameRefId").equals(id)) {
                            amountArr = gameObj.getJSONArray("panelPrices");
                            for (int i = 0; i < amountArr.length(); i++) {
                                priceStrList.add(String.valueOf(amountArr.get(i)));
                            }

                            if (gameObj.getInt("panelsPerDrawMax") == 0) {
                                maxAmount = 10;
                            } else {
                                maxAmount = gameObj.getInt("panelsPerDrawMax");
                            }

                            for (int i = 0; i < maxAmount; i++) {
                                playAmountStrList.add(String.valueOf(i + 1));
                            }

                            return gameObj.getString("logoUrl");
                        }
                    } catch (Exception e) {
                        Log.e("ChannelGroup-->", e.getMessage());
                    }
                }
                break;
            case 2:
                for (JSONObject brandObj : brandList) {
                    try {
                        if (brandObj.getString("brandRefId").equals(id)) {
                            String returnUrlForBrand = brandObj.getString("logoUrl");

                            if (!channelGroupData.getString("channelGroupLogoUrl").equals("")) {
                                returnUrlForBrand = channelGroupData.getString("channelGroupLogoUrl");
                            }
                            return returnUrlForBrand;
                        }
                    } catch (Exception e) {
                        Log.e("ChannelGroup-->", e.getMessage());
                    }
                }
                break;
            default:
                break;
        }
        return "";
    }

}
