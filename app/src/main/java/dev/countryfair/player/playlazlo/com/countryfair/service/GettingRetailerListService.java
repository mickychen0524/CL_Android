package dev.countryfair.player.playlazlo.com.countryfair.service;

import android.app.Activity;
import android.app.IntentService;
import android.content.Intent;
import android.location.Location;
import android.os.Bundle;
import android.os.ResultReceiver;
import android.util.Log;

import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;

import org.json.JSONObject;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import dev.countryfair.player.playlazlo.com.countryfair.helper.APIInterface;
import dev.countryfair.player.playlazlo.com.countryfair.helper.AndroidUtilities;
import dev.countryfair.player.playlazlo.com.countryfair.helper.Constants;
import dev.countryfair.player.playlazlo.com.countryfair.model.AddressLocation;
import dev.countryfair.player.playlazlo.com.countryfair.model.Retailer;

/**
 * Created by mymac on 3/17/17.
 */

public class GettingRetailerListService extends IntentService {
    private static final String TAG = GettingRetailerListService.class.getSimpleName();
    public static final String EXTRA_BEACONS = "extra.beacons";
    public static final String EXTRA_RETAILER_ID = "extra.retailer.id";
    private final Object syncReceivers = new Object();

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
    private Set<ResultReceiver> mResultReceiver = new HashSet<>();
    private JSONObject receivedObj;

    @Override
    protected void onHandleIntent(Intent workIntent) {
        // Gets data from the incoming Intent
        String dataString = workIntent.getDataString();

    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {

        boolean b_status = intent.getBooleanExtra("gettingStatus", false);
        ResultReceiver receiver = intent.getParcelableExtra("receiver");
        if (receiver != null) {
            synchronized (syncReceivers) {
                mResultReceiver.add(receiver);


                if (Constants.b_getRetailerListFlg) {
                    stopSelf();
                    Bundle bundle = new Bundle();
                    bundle.putInt("resultStatus", 33);
                    receiver.send(Activity.RESULT_OK, bundle);
                } else {
                    getRetailerList();
                }
            }
        }

        return super.onStartCommand(intent,flags,startId);
    }

    private void getRetailerList() {

        final Location myLocation = new Location("My location");
        myLocation.setLatitude(Double.parseDouble(Constants.GEO_LATITUDE));
        myLocation.setLongitude(Double.parseDouble(Constants.GEO_LONGITUDE));

        new Thread(new Runnable() {
        public void run() {
            try {
                String uuid = AndroidUtilities.getUUID(GettingRetailerListService.this);
                receivedObj = APIInterface.getRetailerByLocation(uuid);

                if (receivedObj != null) {
                    try {
                        List<Retailer> retailers = null;
                        try {
                            retailers = new Gson().fromJson(receivedObj.get("data").toString(), Retailer.LIST_TYPE);
                        }
                        catch (JsonSyntaxException e){
                            e.printStackTrace();
                        }
                        if (retailers==null||retailers.size() == 0) {
                            Thread.sleep(8000);
                            getRetailerList();
                        } else {

                            double minDistanceInMeters = 10000000.0;
                            String retailerAddressInStr = "";
                            Retailer nearestRetailer = null;
                            for (Retailer retailer:retailers) {
                                AddressLocation addressLocation = retailer.getAddressLocation();
                                if (addressLocation==null)
                                    continue;
                                List<Double> coords = addressLocation.getCoordinates();
                                if (coords==null||coords.size()<2)
                                    continue;
                                Location newLocation = new Location("New Location");
                                newLocation.setLatitude(coords.get(1));
                                newLocation.setLongitude(coords.get(0));
                                double newDistance = myLocation.distanceTo(newLocation);

                                if (newDistance < minDistanceInMeters) {
                                    minDistanceInMeters = newDistance;
                                    nearestRetailer = retailer;
                                }
                            }
                            if (nearestRetailer!=null) {
                                retailerAddressInStr =  nearestRetailer.getRetailerName() + " " +
                                        nearestRetailer.getAddressLine1() + " " +
                                        nearestRetailer.getAddressStateProvince();

                                Constants.retailerRefId = nearestRetailer.getRetailerRefId();

                                Constants.b_getRetailerListFlg = true;
                                stopSelf();
                                Bundle bundle = new Bundle();
                                bundle.putInt("resultStatus", 1);
                                bundle.putString("retailerAddress", retailerAddressInStr);
                                bundle.putString(EXTRA_RETAILER_ID,nearestRetailer.getRetailerRefId());
                                bundle.putParcelableArrayList(EXTRA_BEACONS,nearestRetailer.getBeacons());
                                synchronized (syncReceivers) {
                                    for (ResultReceiver resultReceiver : mResultReceiver) {
                                        resultReceiver.send(Activity.RESULT_OK, bundle);
                                    }
                                    mResultReceiver.clear();
                                }
                            }
                        }
                    } catch (Exception e) {
                        Log.e("getting st error--->", e.getMessage());
                        stopSelf();
                        Bundle bundle = new Bundle();
                        bundle.putInt("resultStatus", 2);
                        synchronized (syncReceivers) {
                            for (ResultReceiver resultReceiver : mResultReceiver) {
                                resultReceiver.send(Activity.RESULT_OK, bundle);
                            }
                            mResultReceiver.clear();
                        }
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
                synchronized (syncReceivers) {
                    for (ResultReceiver resultReceiver : mResultReceiver) {
                        if (resultReceiver != null) {
                            resultReceiver.send(Activity.RESULT_OK, bundle);
                        }
                    }
                    mResultReceiver.clear();
                }

            }
            }
        }).start();
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