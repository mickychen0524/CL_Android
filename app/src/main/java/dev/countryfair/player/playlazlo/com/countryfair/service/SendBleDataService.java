package dev.countryfair.player.playlazlo.com.countryfair.service;

import android.app.IntentService;
import android.content.Context;
import android.content.Intent;
import android.location.Location;
import android.text.TextUtils;
import android.util.Log;

import com.google.gson.Gson;

import org.json.JSONObject;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import dev.countryfair.player.playlazlo.com.countryfair.helper.APIInterface;
import dev.countryfair.player.playlazlo.com.countryfair.helper.ApiClient;
import dev.countryfair.player.playlazlo.com.countryfair.helper.AppDelegate;
import dev.countryfair.player.playlazlo.com.countryfair.helper.Constants;
import dev.countryfair.player.playlazlo.com.countryfair.helper.LastKnownLocation;
import dev.countryfair.player.playlazlo.com.countryfair.model.BeaconEvent;
import dev.countryfair.player.playlazlo.com.countryfair.model.LocationInfo;
import dev.countryfair.player.playlazlo.com.countryfair.model.SendBeaconData;
import dev.countryfair.player.playlazlo.com.countryfair.model.Spatial;
import dev.countryfair.player.playlazlo.com.countryfair.model.db.DiscoveredBeacons;
import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import okhttp3.ResponseBody;
import retrofit2.Response;

public class SendBleDataService extends IntentService {
    private static final String CLASSNAME = SendBleDataService.class.getName();
    private static final String TAG = SendBleDataService.class.getSimpleName();
    private static final String ACTION_SEND = CLASSNAME+".SEND";

    public SendBleDataService() {
        super(TAG);
    }

    public static void startSend(Context context) {
        Intent intent = new Intent(context, SendBleDataService.class);
        intent.setAction(ACTION_SEND);
        context.startService(intent);
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        if (intent != null) {
            final String action = intent.getAction();
            if (ACTION_SEND.equals(action)) {
                handleActionSend();
            }
        }
    }

    private void handleActionSend() {
        List<DiscoveredBeacons> beacons = ((AppDelegate)getApplication()).getDaoSession().getDiscoveredBeaconsDao().loadAll();
        if (beacons.size()==0)
            return;
        String proximityUrl = getProximityUrl();
        if (proximityUrl!=null){
            SendBeaconData data = new SendBeaconData();
            LocationInfo locationInfo = new LocationInfo();
            Spatial spatial = new Spatial();
            Location location = LastKnownLocation.getLastKnownLocation(this);
            if (location!=null) {
                spatial.setLatitude(location.getLatitude());
                spatial.setLongitude(location.getLongitude());
                spatial.setAltitude(location.getAltitude());
            }
            locationInfo.setSpatial(spatial);
            List<BeaconEvent> events = new ArrayList<>();
            for (DiscoveredBeacons beacon:beacons){
                events.add(new BeaconEvent(beacon));
            }
            locationInfo.setBeaconEvents(events);
            locationInfo.setPlayerLicenseCode(Constants.PLAYER_TOKEN);
            if (!TextUtils.isEmpty(Constants.retailerRefId))
                locationInfo.setRetailerRefId(Constants.retailerRefId);
            else
                locationInfo.setRetailerRefId("00000000-0000-0000-0000-000000000000");
            data.setLocationInfo(locationInfo);
            data.setBrandLicenseCode(Constants.BRAND_LISENCE_CODE);
            String json = new Gson().toJson(data,SendBeaconData.class);
            File f = writeToFile(json);
            if (uploadFile(proximityUrl,f)){
                ((AppDelegate)getApplication()).getDaoSession().getDiscoveredBeaconsDao().deleteInTx(beacons);
            }
        }
    }

    private boolean uploadFile(String proximityUrl, File f) {
        final RequestBody reqFile = RequestBody.create(MediaType.parse("application/json"), f);
        MultipartBody.Part body = MultipartBody.Part.create(reqFile);

        Map<String, String> headersMap = new HashMap<>();
        headersMap.put("Lazlo-UserLicenseCode", Constants.PLAYER_TOKEN);
        headersMap.put("Accept-Type", "application/json");
        headersMap.put("x-ms-blob-type","BlockBlob");

        try {
            Response<ResponseBody> response = ApiClient.getInstance(this).uploadBleData(proximityUrl, headersMap, body).execute();
            if (!response.isSuccessful())
                Log.d(TAG, "uploadFile: failed: "+response.message());
            return response.isSuccessful();
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }
    }
    private File writeToFile(String json) {
        File f = new File(getFilesDir(),"beaconFile.txt");
        try
        {
            f.createNewFile();
            FileOutputStream fOut = new FileOutputStream(f);
            OutputStreamWriter myOutWriter = new OutputStreamWriter(fOut);
            myOutWriter.append(json);
            myOutWriter.close();
            fOut.flush();
            fOut.close();
        }
        catch (IOException e)
        {
            return null;
        }
        return f;
    }

    private String getProximityUrl() {
        try {
            JSONObject obj = APIInterface.getProximityUrl();
            Log.d(TAG, "getProximityUrl: "+obj);
            if (obj!=null&&obj.has("data"))
                return obj.getString("data");
        }catch (Exception e){
            e.printStackTrace();
        }
        return null;
    }
}
