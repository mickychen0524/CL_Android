package dev.countryfair.player.playlazlo.com.countryfair.service;

import android.app.Service;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Handler;
import android.os.IBinder;
import android.os.Looper;
import android.preference.PreferenceManager;
import android.support.annotation.Nullable;
import android.util.Log;
import android.widget.Toast;

import net.hockeyapp.android.CrashManager;
import net.hockeyapp.android.CrashManagerListener;

import org.json.JSONObject;

import dev.countryfair.player.playlazlo.com.countryfair.PermissionActivity;
import dev.countryfair.player.playlazlo.com.countryfair.helper.APIInterface;
import dev.countryfair.player.playlazlo.com.countryfair.helper.Constants;

public class ConfigService extends Service {

    private static final String TAG = "ConfigService";

    public ConfigService() {
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.d(TAG, "onStartCommand: ");
        retrieveAndUpdateConfig();
        return super.onStartCommand(intent, flags, startId);
    }

    private void retrieveAndUpdateConfig() {
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    final JSONObject receivedObj = APIInterface.getConfiguration();
                    if(receivedObj!=null){
                        Log.d(TAG, "retrieveAndUpdateConfig: "+receivedObj.toString());
                        JSONObject dataObj = receivedObj.getJSONObject("data");
                        SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
                        SharedPreferences.Editor editor = sharedPref.edit();
                        editor.putString("hockeyAppId", dataObj.getString("hockeyAppId"));
                        editor.putString("ocrLicenseCodeAndroid", dataObj.getString("ocrLicenseCodeAndroid"));
                        editor.apply();
                        Constants.HOCKEY_APP_ID = sharedPref.getString("hockeyAppId","");
                        Constants.OCR_LICENSE_CODE = sharedPref.getString("ocrLicenseCodeAndroid","");
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
                initHockey();
                stopSelf();
            }
        }).start();

    }

    private void initHockey(){
        Log.d(TAG, "initHockey: ");
        try {
            CrashManager.register(ConfigService.this, Constants.HOCKEY_APP_ID);
        } catch (Exception e) {
            e.printStackTrace();
            showToast("Hockey App ID seems invalid!");
        }
    }

    public void showToast(final String message) {
        new Handler(Looper.getMainLooper()).post(new Runnable() {
            @Override
            public void run() {
                Toast.makeText(getApplicationContext(), message, Toast.LENGTH_SHORT).show();
            }
        });
    }


}
