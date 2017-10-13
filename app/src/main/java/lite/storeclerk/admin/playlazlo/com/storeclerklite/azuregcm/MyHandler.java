package lite.storeclerk.admin.playlazlo.com.storeclerklite.azuregcm;


import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.NotificationCompat;

import com.microsoft.windowsazure.notifications.NotificationsHandler;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.Set;

import lite.storeclerk.admin.playlazlo.com.storeclerklite.MainActivity;
import lite.storeclerk.admin.playlazlo.com.storeclerklite.R;
import lite.storeclerk.admin.playlazlo.com.storeclerklite.database.AppDb;
import lite.storeclerk.admin.playlazlo.com.storeclerklite.model.PushMessage;

public class MyHandler extends NotificationsHandler {
    public static final int NOTIFICATION_ID = 1;
    private NotificationManager mNotificationManager;
    NotificationCompat.Builder builder;
    Context ctx;
    boolean isValidMessage = false;

    @Override
    public void onReceive(Context context, Bundle bundle) {
        ctx = context;
        String nhMessage = retrieveMessage(bundle);
        sendNotification(nhMessage);
        if(isValidMessage){
            saveMessage(nhMessage);
            startSilentDownload(nhMessage);
        }
    }

    private String retrieveMessage(Bundle bundle){
        isValidMessage = false;
        JSONObject json = new JSONObject();
        Set<String> keys = bundle.keySet();
        if(keys.contains("sasUri")){
            isValidMessage = true;
            for (String key : keys) {
                try {
                    json.put(key, bundle.get(key));
                } catch(JSONException e) {
                }
            }
            return json.toString();
        }
        return json.toString();
    }

    private void saveMessage(String msg){
        AppDb mAppDb = AppDb.getAppDatabase(ctx.getApplicationContext());
        mAppDb.daoPushMessage().insert(new PushMessage(msg));
        AppDb.destroyInstance();
    }

    private void sendNotification(String msg) {

        Intent intent = new Intent(ctx, MainActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);

        mNotificationManager = (NotificationManager)
                ctx.getSystemService(Context.NOTIFICATION_SERVICE);

        PendingIntent contentIntent = PendingIntent.getActivity(ctx, 0,
                intent, PendingIntent.FLAG_ONE_SHOT);

        Uri defaultSoundUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
        NotificationCompat.Builder mBuilder =
                new NotificationCompat.Builder(ctx)
                        .setSmallIcon(R.mipmap.ic_launcher)
                        .setContentTitle("Notification Hub Demo")
                        .setStyle(new NotificationCompat.BigTextStyle()
                                .bigText(msg))
                        .setSound(defaultSoundUri)
                        .setContentText(msg);

        mBuilder.setContentIntent(contentIntent);
        mNotificationManager.notify(NOTIFICATION_ID, mBuilder.build());
    }

    private void startSilentDownload(String msg){
        Intent mSilentDownloadService = new Intent(ctx,SilentDownloadService.class);
        mSilentDownloadService.putExtra("message",msg);
        ctx.startService(mSilentDownloadService);
    }

}
