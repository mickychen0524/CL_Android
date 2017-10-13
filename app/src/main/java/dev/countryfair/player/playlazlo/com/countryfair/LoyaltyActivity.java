package dev.countryfair.player.playlazlo.com.countryfair;

import android.app.ProgressDialog;
import android.graphics.Color;
import android.graphics.drawable.LayerDrawable;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.FragmentActivity;
import android.support.v4.content.ContextCompat;
import android.support.v4.graphics.drawable.DrawableCompat;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.RatingBar;
import android.widget.TextView;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

import cn.refactor.lib.colordialog.PromptDialog;
import dev.countryfair.player.playlazlo.com.countryfair.adapter.CardsListDataAdapter;
import dev.countryfair.player.playlazlo.com.countryfair.helper.APIInterface;
import dev.countryfair.player.playlazlo.com.countryfair.helper.AppHelper;
import dev.countryfair.player.playlazlo.com.countryfair.helper.Constants;
import dev.countryfair.player.playlazlo.com.countryfair.helper.DbManager;
import dev.countryfair.player.playlazlo.com.countryfair.model.Loyalty;

/**
 * Created by Android Developer on 16/08/17.
 */

public class LoyaltyActivity extends FragmentActivity {

    private static final String TAG = LoyaltyActivity.class.getSimpleName();

    private List<JSONObject> loyaltyList = new ArrayList<>();
    private RecyclerView rvLoyalty;
    private LoyaltyListAdapter mLoyaltyListAdapter;
    private ProgressDialog mProgressDialog;
    private JSONObject receivedObj = new JSONObject();

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_loyalty);
        init();
    }

    private void init(){

        mLoyaltyListAdapter = new LoyaltyListAdapter();
        rvLoyalty = (RecyclerView) findViewById(R.id.rvLoyalty);
        rvLoyalty.setLayoutManager(new LinearLayoutManager(getApplicationContext(),LinearLayoutManager.VERTICAL,false));
        rvLoyalty.setHasFixedSize(true);
        rvLoyalty.setAdapter(mLoyaltyListAdapter);


        loyaltyList.clear();
        getAllRewardsList();
    }

    private void getAllRewardsList() {
        try {
            mProgressDialog = new ProgressDialog(this);
            mProgressDialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
            mProgressDialog.setMessage("Getting rewards...");
            mProgressDialog.show();
            mProgressDialog.setCancelable(false);
            mProgressDialog.setCanceledOnTouchOutside(false);
            new Thread(new Runnable() {
                public void run() {
                    try {
                        receivedObj = APIInterface.getRewards();
                        Log.d(TAG, "run: "+receivedObj.toString());
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {

                                if (mProgressDialog.isShowing()) {
                                    mProgressDialog.dismiss();
                                }

                                if (receivedObj != null) {
                                    try {
                                        loyaltyList = AppHelper.parseFromJsonList(receivedObj.getJSONArray("data"));
                                        mLoyaltyListAdapter.notifyDataSetChanged();
                                    } catch (Exception e) {
                                        Log.d("json_e-->", e.getMessage());
                                    }

                                } else {
                                    new PromptDialog(LoyaltyActivity.this)
                                            .setDialogType(PromptDialog.DIALOG_TYPE_WRONG)
                                            .setAnimationEnable(true)
                                            .setTitleText(Constants.ERROR_TITLE)
                                            .setContentText(Constants.ERROR_MESSAGE)
                                            .setPositiveListener("Ok", new PromptDialog.OnPositiveListener() {
                                                @Override
                                                public void onClick(PromptDialog dialog) {
                                                    dialog.dismiss();
                                                }
                                            }).show();
                                }

                            }
                        });
                    } catch (final Exception e) {
                        Log.e("register error--->", e.getMessage());
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                if (mProgressDialog.isShowing()) {
                                    mProgressDialog.dismiss();
                                }
                                new PromptDialog(LoyaltyActivity.this)
                                        .setDialogType(PromptDialog.DIALOG_TYPE_WRONG)
                                        .setAnimationEnable(true)
                                        .setTitleText(Constants.ERROR_TITLE)
                                        .setContentText(Constants.ERROR_MESSAGE)
                                        .setPositiveListener("Ok", new PromptDialog.OnPositiveListener() {
                                            @Override
                                            public void onClick(PromptDialog dialog) {
                                                dialog.dismiss();
                                            }
                                        }).show();
                            }
                        });

                    }
                }
            }).start();

        } catch (Exception e) {

            Log.e("json_error-->", e.getMessage());
        }
    }



    public class LoyaltyListAdapter extends RecyclerView.Adapter<LoyaltyListAdapter.ItemHolder>{

        public class ItemHolder extends RecyclerView.ViewHolder {
            public TextView tvName;
            public RatingBar rbPendingPoints;
            public TextView tvLifetimePoints;

            public ItemHolder(View itemView) {
                super(itemView);
                tvName = (TextView) itemView.findViewById(R.id.tvName);
                rbPendingPoints = (RatingBar) itemView.findViewById(R.id.rbPendingPoints);
                tvLifetimePoints = (TextView) itemView.findViewById(R.id.tvLifetimePoints);
            }
        }


        public LoyaltyListAdapter() {
        }

        @Override
        public ItemHolder onCreateViewHolder(ViewGroup parent, int type) {
            View v = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.item_loyalty, parent, false);
            ItemHolder holder = new ItemHolder(v);
            return holder;
        }

        @Override
        public void onBindViewHolder(ItemHolder holder, final int position) {
            final JSONObject loyalty = loyaltyList.get(position);
            try {

                LayerDrawable layerDrawable = (LayerDrawable) holder.rbPendingPoints.getProgressDrawable();
                DrawableCompat.setTint(DrawableCompat.wrap(layerDrawable.getDrawable(0)), Color.WHITE);   // Empty star
                DrawableCompat.setTint(DrawableCompat.wrap(layerDrawable.getDrawable(1)), ContextCompat.getColor(LoyaltyActivity.this,R.color.colorAccent)); // Partial star
                DrawableCompat.setTint(DrawableCompat.wrap(layerDrawable.getDrawable(2)), ContextCompat.getColor(LoyaltyActivity.this,R.color.colorAccent));

                holder.tvName.setText(loyalty.getString("skuName"));
                holder.rbPendingPoints.setRating((float)loyalty.getDouble("pendingCount")/2);
                holder.tvLifetimePoints.setText(String.valueOf(loyalty.getDouble("lifetimeCount")));
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }

        @Override
        public int getItemCount() {
            return loyaltyList.size();
        }
    }


}
