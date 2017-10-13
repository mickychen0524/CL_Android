package dev.countryfair.player.playlazlo.com.countryfair.azuregcm;


import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.NotificationCompat;
import com.microsoft.windowsazure.notifications.NotificationsHandler;
import android.text.TextUtils;
import android.util.Log;

import com.google.gson.Gson;
import java.util.Set;
import com.microsoft.windowsazure.notifications.NotificationsManager;
import dev.countryfair.player.playlazlo.com.countryfair.MainActivity;
import dev.countryfair.player.playlazlo.com.countryfair.R;
import dev.countryfair.player.playlazlo.com.countryfair.database.AppDb;
import dev.countryfair.player.playlazlo.com.countryfair.model.PushMessage;
import dev.countryfair.player.playlazlo.com.countryfair.model.PushMessageCommon;
import dev.countryfair.player.playlazlo.com.countryfair.model.PushMessageEntity;
import dev.countryfair.player.playlazlo.com.countryfair.model.PushMessageItem;
import dev.countryfair.player.playlazlo.com.countryfair.service.SendBleDataService;

public class MyHandler extends NotificationsHandler {
    public static final int NOTIFICATION_ID = 1;
    private static final String TAG = MyHandler.class.getSimpleName();
    private static final int OPERATION_TYPE_BLE_REQUEST = 65536;
    private NotificationManager mNotificationManager;
    NotificationCompat.Builder builder;
    Context ctx;
    boolean isValidMessage = false;

    @Override
    public void onReceive(Context context, Bundle bundle) {
        ctx = context;
        PushMessageItem item = retrieveMessage(bundle);
        if (item!=null) {
            String msg = new Gson().toJson(item, PushMessageItem.class);
            Log.d(TAG, "onReceive: "+msg);
            saveMessage(msg);
            if (isBleRequestMessage(item)) {
                SendBleDataService.startSend(context);
            } else {
                sendNotification(msg);
                if (isValidMessage) {
                    String entity = new Gson().toJson(item.getEntity());
                    startSilentDownload(entity);
                }
            }
        }
    }

    private boolean isBleRequestMessage(PushMessageItem item) {
        return item!=null&&item.getCommon()!=null&&item.getCommon().getOperationType()==OPERATION_TYPE_BLE_REQUEST;
    }

    private PushMessageItem retrieveMessage(Bundle bundle){
        isValidMessage = false;
        Set<String> keys = bundle.keySet();
        PushMessageItem item = new PushMessageItem();
        if (keys.contains("google.sent_time"))
            item.setGoogleSentTime(bundle.getLong("google.sent_time"));
        if (keys.contains("common"))
            item.setCommon(PushMessageCommon.from(bundle.getString("common")));
        if (keys.contains("entity"))
            item.setEntity(PushMessageEntity.from(bundle.getString("entity")));
        if (keys.contains("google.message_id"))
            item.setMessageId(bundle.getString("google.message_id"));
        isValidMessage = item.getEntity()!=null&&!TextUtils.isEmpty(item.getEntity().getSasUri());
        return item;
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
