package dev.countryfair.player.playlazlo.com.countryfair.azuregcm;

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

import com.google.android.gms.gcm.GoogleCloudMessaging;
import com.google.android.gms.iid.InstanceID;
import com.microsoft.windowsazure.messaging.NotificationHub;

import org.json.JSONObject;

import dev.countryfair.player.playlazlo.com.countryfair.helper.APIInterface;
import dev.countryfair.player.playlazlo.com.countryfair.helper.AndroidUtilities;


public class RegistrationService extends Service {

    private static final String TAG = "RegistrationService";

    public RegistrationService() {
        super();
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.d(TAG, "onStartCommand: ");
        retrieveAndSendRegToken();
        return super.onStartCommand(intent, flags, startId);
    }

    private void retrieveAndSendRegToken(){
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    InstanceID instanceID = InstanceID.getInstance(RegistrationService.this);
                    String token = instanceID.getToken(NotificationSettings.SenderId,
                            GoogleCloudMessaging.INSTANCE_ID_SCOPE);
                    Log.i(TAG, "Got GCM Registration Token: " + token);
                    NotificationHub hub = new NotificationHub(NotificationSettings.HubName,
                            NotificationSettings.HubListenConnectionString, RegistrationService.this);
                    String regID = hub.register(token).getRegistrationId();
                    Log.i(TAG, "Got regID from NH  : " + regID);
                    if(token!=null){
                        saveOnServer(token);
                    }else {
                        displayResult(false);
                    }
                } catch (Exception e) {
                    displayResult(false);
                }
                stopSelf();
            }
        }).start();
    }


    private void saveOnServer(String regID){

        try {
            SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(RegistrationService.this.getApplicationContext());
            SharedPreferences.Editor editor = sharedPref.edit();
            editor.putString("deviceTokenForPush", regID);
            editor.apply();
            String uuid = AndroidUtilities.getUUID(RegistrationService.this);
            final JSONObject receivedObj = APIInterface.registerPushNotification(regID,uuid);
            if (receivedObj != null) {
                try {
                    displayResult(true);
                } catch (Exception e) {
                    displayResult(false);
                    Log.d("json_e-->", e.getMessage());
                }
            } else {
                displayResult(false);
            }
        } catch (Exception e) {
        }
    }

    private void displayResult(boolean isSuccessful){
        String message = "";
        if(isSuccessful){
            message = "Push notification registered.";
        }
        else{
            message = "Oops! Notifications failed to register.";
        }
        showToast(message);
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

