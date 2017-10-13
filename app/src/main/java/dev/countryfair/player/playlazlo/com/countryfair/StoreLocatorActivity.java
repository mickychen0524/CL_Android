package dev.countryfair.player.playlazlo.com.countryfair;

import android.app.ProgressDialog;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.FragmentActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.MapStyleOptions;
import com.google.android.gms.maps.model.MarkerOptions;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

import cn.refactor.lib.colordialog.PromptDialog;
import dev.countryfair.player.playlazlo.com.countryfair.adapter.StoreListAdapter;
import dev.countryfair.player.playlazlo.com.countryfair.helper.APIInterface;
import dev.countryfair.player.playlazlo.com.countryfair.helper.AndroidUtilities;
import dev.countryfair.player.playlazlo.com.countryfair.helper.AppHelper;
import dev.countryfair.player.playlazlo.com.countryfair.helper.Constants;

/**
 * Created by Android Developer on 20/08/17.
 */

public class StoreLocatorActivity extends FragmentActivity{

    private static final String TAG = StoreLocatorActivity.class.getSimpleName();

    private JSONObject receivedObj;
    private List<JSONObject> storeListOrig = new ArrayList<JSONObject>();

    private StoreListAdapter mAdapter;

    private ProgressDialog mProgressDialog;

    private EditText etSearchBox;
    private ImageView ivType;
    private RecyclerView rvStores;
    private SupportMapFragment mapFragment;
    private GoogleMap mMap;

    private List<LatLng> mStoreLatlngs = new ArrayList<>();
    private LatLngBounds.Builder mLatLngBuilder;


    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_storelocator);
        init();
        getStoreList();
    }

    private void init(){

        etSearchBox = (EditText) findViewById(R.id.etSearchBox);
        ivType = (ImageView) findViewById(R.id.ivType);
        mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        rvStores = (RecyclerView) findViewById(R.id.rvStores);

        updateStoreDisplayType();
        storeListOrig.clear();
        mAdapter = new StoreListAdapter(StoreLocatorActivity.this, storeListOrig);
        etSearchBox.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                filterStores(charSequence.toString());
            }

            @Override
            public void afterTextChanged(Editable editable) {

            }
        });
        ivType.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                toggleDisplayType();
            }
        });
        mapFragment.getMapAsync(new OnMapReadyCallback() {
            @Override
            public void onMapReady(GoogleMap googleMap) {
                mMap = googleMap;
                MapStyleOptions darkMapStyle = MapStyleOptions.loadRawResourceStyle(StoreLocatorActivity.this, R.raw.dark_map_style);
                mMap.setMapStyle(darkMapStyle);
            }
        });
        rvStores.setLayoutManager(new LinearLayoutManager(getApplicationContext(),LinearLayoutManager.VERTICAL,false));
        rvStores.setHasFixedSize(true);
        rvStores.setAdapter(mAdapter);
}

    private void getStoreList() {
        try {
            mProgressDialog = new ProgressDialog(StoreLocatorActivity.this);
            mProgressDialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
            mProgressDialog.setMessage("Getting retailers...");
            mProgressDialog.show();
            mProgressDialog.setCancelable(false);
            mProgressDialog.setCanceledOnTouchOutside(false);
            new Thread(new Runnable() {
                public void run() {
                    try {
                        receivedObj = APIInterface.getRetailerByLocation(AndroidUtilities.getUUID(getApplicationContext()));
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {

                                if (mProgressDialog.isShowing()) {
                                    mProgressDialog.dismiss();
                                }

                                if (receivedObj != null) {
                                    try {
                                        storeListOrig = AppHelper.parseFromJsonList(receivedObj.getJSONArray("data"));

                                        mAdapter.setStoreList(storeListOrig);
                                        mAdapter.notifyDataSetChanged();

                                        updateMap("");
                                    } catch (Exception e) {
                                        Log.d("json_e-->", e.getMessage());
                                    }

                                } else {
                                    new PromptDialog(StoreLocatorActivity.this)
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
                                new PromptDialog(StoreLocatorActivity.this)
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

    private void updateMap(String constraint){

        if (receivedObj != null) {
            try {
                mStoreLatlngs.clear();
                mMap.clear();
                mLatLngBuilder = new LatLngBounds.Builder();
                storeListOrig = AppHelper.parseFromJsonList(receivedObj.getJSONArray("data"));
                if(storeListOrig.size()>0){
                    for(JSONObject store:storeListOrig){

                        JSONObject addressLocation = store.getJSONObject("addressLocation");
                        JSONArray cordinates = addressLocation.getJSONArray("coordinates");
                        String storeName = store.getString("retailerName");
                        LatLng latLng = new LatLng(cordinates.getDouble(1),cordinates.getDouble(0));
                        if(constraint!=null && constraint.trim().length()>0){
                            if(store.getString("retailerName").contains(constraint))
                            {
                                //add only if it matches
                                mStoreLatlngs.add(latLng);
                                mMap.addMarker(new MarkerOptions().position(latLng).title(storeName));
                                mLatLngBuilder.include(latLng);
                            }
                        }
                        else{
                            //there is no constraint, so add all
                            mStoreLatlngs.add(latLng);
                            mMap.addMarker(new MarkerOptions().position(latLng).title(storeName));
                            mLatLngBuilder.include(latLng);
                        }

                    }
                }

                //apply bound on map
                if(mStoreLatlngs.size()>0 && mapFragment.getView().getVisibility()==View.VISIBLE){
                    mMap.animateCamera(CameraUpdateFactory.newLatLngBounds(mLatLngBuilder.build(), 100));
                }

            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
    }

    private void filterStores(String filterString){
        mAdapter.getFilter().filter(filterString);
        updateMap(filterString);
    }

    private void toggleDisplayType(){
        if(((String)ivType.getTag()).equals("list")){
            ivType.setTag("map");
            ivType.setImageResource(R.drawable.ic_list);
        }
        else if(((String)ivType.getTag()).equals("map")){
            ivType.setTag("list");
            ivType.setImageResource(R.drawable.storelocatorwhite);
        }
        updateStoreDisplayType();
    }

    private void updateStoreDisplayType(){
        if(((String)ivType.getTag()).equals("list")){
            rvStores.setVisibility(View.VISIBLE);
            mapFragment.getView().setVisibility(View.GONE);
        }
        else if(((String)ivType.getTag()).equals("map")){
            rvStores.setVisibility(View.GONE);
            mapFragment.getView().setVisibility(View.VISIBLE);
            mapFragment.getView().addOnLayoutChangeListener(new View.OnLayoutChangeListener() {
                @Override
                public void onLayoutChange(View view, int i, int i1, int i2, int i3, int i4, int i5, int i6, int i7) {
                    Log.d(TAG, "onLayoutChange: ");
                    mapFragment.getView().removeOnLayoutChangeListener(this);
                    try {
                        if(mStoreLatlngs.size()>0){
                            mMap.animateCamera(CameraUpdateFactory.newLatLngBounds(mLatLngBuilder.build(), 100));
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            });
        }
    }



}
