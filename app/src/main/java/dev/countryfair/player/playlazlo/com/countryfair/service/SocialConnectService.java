package dev.countryfair.player.playlazlo.com.countryfair.service;

import android.app.Service;
import android.content.Intent;
import android.os.Handler;
import android.os.IBinder;
import android.os.Looper;
import android.support.annotation.Nullable;
import android.util.Log;
import android.widget.Toast;
import org.json.JSONObject;
import dev.countryfair.player.playlazlo.com.countryfair.helper.APIInterface;
import dev.countryfair.player.playlazlo.com.countryfair.helper.AndroidUtilities;

public class SocialConnectService extends Service {

    private static final String TAG = "SocialConnectService";

    public SocialConnectService() {
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.d(TAG, "onStartCommand: ");
        String code = intent.getStringExtra("code");
        socialConnect(code);
        return super.onStartCommand(intent, flags, startId);
    }

    private void socialConnect(final String code) {
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    final JSONObject receivedObj = APIInterface.socialConnect(code);
                    if(receivedObj!=null){
                        Log.d(TAG, "socialConnect: "+receivedObj.toString());
                        showToast("Social Connect Success!");
                    }
                    else{
                        showToast("Oops! Invalid Scan Value.");
                        AndroidUtilities.vibrateDevice(SocialConnectService.this.getApplicationContext());
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
                stopSelf();
            }
        }).start();

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
