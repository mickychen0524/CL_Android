package lite.storeclerk.admin.playlazlo.com.storeclerklite.service;

import android.app.Activity;
import android.app.IntentService;
import android.content.Context;
import android.content.Intent;
import android.location.Location;
import android.os.Bundle;
import android.os.Handler;
import android.os.ResultReceiver;
import android.telephony.TelephonyManager;
import android.util.Log;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.List;

import lite.storeclerk.admin.playlazlo.com.storeclerklite.helper.APIInterface;

import lite.storeclerk.admin.playlazlo.com.storeclerklite.helper.AndroidUtilities;
import lite.storeclerk.admin.playlazlo.com.storeclerklite.helper.AppHelper;
import lite.storeclerk.admin.playlazlo.com.storeclerklite.helper.Constants;
import lite.storeclerk.admin.playlazlo.com.storeclerklite.helper.GeoLocationUtil;

/**
 * Created by mymac on 3/17/17.
 */

public class GettingRetailerListService extends IntentService {
    /**
     * A constructor is required, and must call the super IntentService(String)
     * constructor with a name for the worker thread.
     */

    public GettingRetailerListService() {
        super("HelloIntentService");
    }

    /**
     * The IntentService calls this method from the default worker thread with
     * the intent that started the service. When this method returns, IntentService
     * stops the service, as appropriate.
     */
    private ResultReceiver mResultReceiver;
    private JSONObject receivedObj;

    @Override
    protected void onHandleIntent(Intent workIntent) {
        // Gets data from the incoming Intent
        String dataString = workIntent.getDataString();

    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {

        boolean b_status = intent.getBooleanExtra("gettingStatus", false);
        mResultReceiver = intent.getParcelableExtra("receiver");

        if (Constants.b_getRetailerListFlg) {
            stopSelf();
            Bundle bundle = new Bundle();
            bundle.putInt("resultStatus", 33);
            mResultReceiver.send(Activity.RESULT_OK, bundle);
        } else {
            getRetailerList();
        }

        return super.onStartCommand(intent,flags,startId);
    }

    private void getRetailerList() {
        if (Constants.PLAYER_TOKEN.length() == 0) {
            return;
        }

        if (Constants.GEO_LATITUDE_DOUB > 0.0 && Constants.GEO_LATITUDE_DOUB > 0.0 ) {

            final Location myLocation = new Location("My location");
            myLocation.setLatitude(Double.parseDouble(Constants.GEO_LATITUDE));
            myLocation.setLongitude(Double.parseDouble(Constants.GEO_LONGITUDE));
            new Thread(new Runnable() {
                public void run() {
                    try {

                        String uuid = AndroidUtilities.getUUID(GettingRetailerListService.this);
                        receivedObj = APIInterface.getRetailerList(uuid);

                        if (receivedObj != null) {
                            try {
                                JSONArray jsonArr = receivedObj.getJSONArray("data");
                                if (jsonArr.length() == 0) {
                                    Thread.sleep(8000);
                                    getRetailerList();
                                } else {

                                    double minDistanceInMeters = 10000000.0;
                                    String retailerAddressInStr = "";

                                    List<JSONObject> resDataList = AppHelper.parseFromJsonList(jsonArr);
                                    Constants.globalRetailerArr = resDataList;
                                    for (JSONObject retailerItem : resDataList) {
                                        JSONObject addressLocation = retailerItem.getJSONObject("addressLocation");
                                        JSONArray coordsArr = addressLocation.getJSONArray("coordinates");

                                        Location newLocation = new Location("New Location");
                                        newLocation.setLatitude(coordsArr.getDouble(1));
                                        newLocation.setLongitude(coordsArr.getDouble(0));
                                        double newDistance = myLocation.distanceTo(newLocation);

                                        if (newDistance < minDistanceInMeters) {
                                            minDistanceInMeters = newDistance;

                                            retailerAddressInStr = retailerItem.getString("retailerName") + " " +
                                                    retailerItem.getString("addressLine1") + " " +
                                                    retailerItem.getString("addressStateProvince");

                                            Constants.retailerRefId = retailerItem.getString("retailerRefId");
                                        }
                                    }

                                    Constants.b_getRetailerListFlg = true;
                                    stopSelf();
                                    Bundle bundle = new Bundle();
                                    bundle.putInt("resultStatus", 1);
                                    bundle.putString("retailerAddress", retailerAddressInStr);
                                    mResultReceiver.send(Activity.RESULT_OK, bundle);
                                }
                            } catch (Exception e) {
                                Log.e("getting st error--->", e.getMessage());
                                stopSelf();
                                Bundle bundle = new Bundle();
                                bundle.putInt("resultStatus", 2);
                                mResultReceiver.send(Activity.RESULT_OK, bundle);
                            }

                        } else {
                            Thread.sleep(8000);
                            getRetailerList();
                        }

                    } catch (Exception e) {
                        Log.e("getting st error--->", e.getMessage());
                        stopSelf();
                        Bundle bundle = new Bundle();
                        bundle.putInt("resultStatus", 2);
                        mResultReceiver.send(Activity.RESULT_OK, bundle);

                    }
                }
            }).start();
        } else {
            GeoLocationUtil geoLocationUtil = new GeoLocationUtil();
            GeoLocationUtil.LocationResult geoLocationResult = new GeoLocationUtil.LocationResult() {
                @Override
                public void gotLocation(Location location) {
                    if(location!=null){
                        Constants.GEO_LATITUDE_DOUB = location.getLatitude();
                        Constants.GEO_LONGITUDE_DOUB = location.getLongitude();
                        getRetailerList();
                    }
                    else{
                        Toast.makeText(GettingRetailerListService.this, "Geo service is not working", Toast.LENGTH_SHORT).show();
                    }
                }
            };
            if(!geoLocationUtil.getLocation(GettingRetailerListService.this,geoLocationResult)){
                Toast.makeText(GettingRetailerListService.this, "Geo service is not working", Toast.LENGTH_SHORT).show();
            }
        }
    }

    private double getDistance(double lat1, double lon1, double lat2, double lon2) {
        double theta = lon1 - lon2;
        double dist = Math.sin(deg2rad(lat1))
                * Math.sin(deg2rad(lat2))
                + Math.cos(deg2rad(lat1))
                * Math.cos(deg2rad(lat2))
                * Math.cos(deg2rad(theta));
        dist = Math.acos(dist);
        dist = rad2deg(dist);
        dist = dist * 60 * 1.1515;
        return (dist);
    }

    private double deg2rad(double deg) {
        return (deg * Math.PI / 180.0);
    }

    private double rad2deg(double rad) {
        return (rad * 180.0 / Math.PI);
    }
}
