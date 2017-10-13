package dev.countryfair.player.playlazlo.com.countryfair.azuregcm;

import android.app.IntentService;
import android.content.Intent;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.util.Log;
import android.widget.Toast;

import com.google.android.gms.gcm.GoogleCloudMessaging;
import com.google.android.gms.iid.InstanceID;
import com.google.firebase.FirebaseApp;
import com.google.firebase.iid.FirebaseInstanceId;
import com.microsoft.windowsazure.messaging.NotificationHub;

import org.json.JSONObject;

import dev.countryfair.player.playlazlo.com.countryfair.azuregcm.NotificationSettings;
import dev.countryfair.player.playlazlo.com.countryfair.helper.APIInterface;
import dev.countryfair.player.playlazlo.com.countryfair.helper.AndroidUtilities;


public class RegistrationIntentService extends IntentService {

    private static final String TAG = "RegIntentService";

    private NotificationHub hub;
    String regID = null;

    public RegistrationIntentService() {
        super(TAG);
    }

    @Override
    protected void onHandleIntent(Intent intent) {

        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);
        String resultString = null;

        try {
            InstanceID instanceID = InstanceID.getInstance(this);
            String token = instanceID.getToken(NotificationSettings.SenderId,
                    GoogleCloudMessaging.INSTANCE_ID_SCOPE);

            Log.i(TAG, "Got GCM Registration Token: " + token);

            // Storing the registration id that indicates whether the generated token has been
            // sent to your server. If it is not stored, send the token to your server,
            // otherwise your server should have already received the token.
            if ((regID=sharedPreferences.getString("registrationID", null)) == null) {

                NotificationHub hub = new NotificationHub(NotificationSettings.HubName,
                        NotificationSettings.HubListenConnectionString, this);
                Log.i(TAG, "Attempting to register with NH using token : " + token);

                regID = hub.register(token).getRegistrationId();
                saveOnServer();

                // If you want to use tags...
                // Refer to : https://azure.microsoft.com/en-us/documentation/articles/notification-hubs-routing-tag-expressions/
                // regID = hub.register(token, "tag1,tag2").getRegistrationId();
                
                resultString = "Registered Successfully - RegId : " + regID;
                Log.i(TAG, resultString);

                sharedPreferences.edit().putString("registrationID", regID ).apply();
            } else {
                resultString = "Previously Registered Successfully - RegId : " + regID;
            }
        } catch (Exception e) {
            Log.e(TAG, resultString="Failed to complete token refresh", e);
            // If an exception happens while fetching the new token or updating our registration data
            // on a third-party server, this ensures that we'll attempt the update at a later time.
        }

        // Notify UI that registration has completed.
//        Toast.makeText(RegistrationIntentService.this.getApplicationContext(), resultString, Toast.LENGTH_LONG).show();
    }

    private void saveOnServer(){
        new Thread(new Runnable() {
            public void run() {
                try {
                    SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(RegistrationIntentService.this.getApplicationContext());
                    SharedPreferences.Editor editor = sharedPref.edit();
                    editor.putString("deviceTokenForPush", regID);
                    editor.apply();
                    String uuid = AndroidUtilities.getUUID(RegistrationIntentService.this);
                    final JSONObject receivedObj = APIInterface.registerPushNotification(regID,uuid);
                    if (receivedObj != null) {
                        try {
                            JSONObject jsonData = receivedObj.getJSONObject("data");
                            Toast.makeText(RegistrationIntentService.this.getApplicationContext(), "Push Notification Registered", Toast.LENGTH_SHORT).show();

                        } catch (Exception e) {
                            Toast.makeText(RegistrationIntentService.this.getApplicationContext(), "Notification register error", Toast.LENGTH_SHORT).show();
                            Log.d("json_e-->", e.getMessage());
                        }

                    } else {
                        Toast.makeText(RegistrationIntentService.this.getApplicationContext(), "Notification register error", Toast.LENGTH_SHORT).show();
                    }
                } catch (Exception e) {
                }
            }
        }).start();
    }

}

