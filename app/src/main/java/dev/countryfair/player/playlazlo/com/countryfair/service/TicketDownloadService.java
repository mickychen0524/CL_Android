package dev.countryfair.player.playlazlo.com.countryfair.service;

import android.app.Activity;
import android.app.IntentService;
import android.content.Intent;
import android.os.Bundle;
import android.os.Environment;
import android.os.ResultReceiver;
import android.util.Log;
import android.widget.Toast;

import com.koushikdutta.async.future.FutureCallback;
import com.koushikdutta.ion.Ion;
import com.koushikdutta.ion.ProgressCallback;

import org.json.JSONObject;

import java.io.File;
import java.util.List;

import dev.countryfair.player.playlazlo.com.countryfair.helper.Constants;

/**
 * Created by mymac on 3/18/17.
 */

public class TicketDownloadService extends IntentService {
    private static final String TAG = TicketDownloadService.class.getSimpleName();
    /**
     * A constructor is required, and must call the super IntentService(String)
     * constructor with a name for the worker thread.
     */

    public static List<JSONObject> shoppingCartData;
    public static List<JSONObject> ticketList;

    private JSONObject downloadObj = new JSONObject();

    public TicketDownloadService() {
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

        try {
            downloadObj = new JSONObject(intent.getStringExtra("fileItem"));
        } catch (Exception e) {
            stopSelf();
            Bundle bundle = new Bundle();
            bundle.putInt("resultCode", Constants.DOWNLOAD_EXCEPRION);
            bundle.putFloat("resultPercent", 0f);
            mResultReceiver.send(Activity.RESULT_OK, bundle);
        }
        mResultReceiver = intent.getParcelableExtra("receiver");
        ticketDownload();

        return super.onStartCommand(intent,flags,startId);
    }

    private void ticketDownload() {

        try {

            if (downloadObj.isNull("sasUri")) {

            } else {
                String sassUrl = downloadObj.getString("sasUri");
                String fileName = downloadObj.getString("fileName");

                String localFilePath = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS) + "/"+Constants.DIR_ROOT+"/"+Constants.DIR_TICKETS+"/";
                if (!(new File(localFilePath).exists())) {
                    new File(localFilePath).mkdir();
                }
                Ion.with(this)
                        .load(sassUrl)
                        .progress(new ProgressCallback() {
                            @Override
                            public void onProgress(long downloaded, long total) {
                                Bundle bundle = new Bundle();
                                bundle.putInt("resultCode", Constants.DOWNLOAD_PICK);
                                bundle.putFloat("resultPercent", 100 * downloaded / total);
                                mResultReceiver.send(Activity.RESULT_OK, bundle);
                                Toast.makeText(TicketDownloadService.this, "progress : " + String.valueOf(100 * downloaded / total), Toast.LENGTH_SHORT).show();
                                Log.d(TAG, "" + downloaded + " / " + total);
                            }
                        })
                        .write(new File(localFilePath + fileName))
                        .setCallback(new FutureCallback<File>() {
                            @Override
                            public void onCompleted(Exception e, File file) {

                                stopSelf();
                                Bundle bundle = new Bundle();
                                bundle.putInt("resultCode", Constants.DOWNLOAD_COMPLETE);
                                bundle.putFloat("resultPercent", 100f);
                                mResultReceiver.send(Activity.RESULT_OK, bundle);
                            }
                        });
            }
        } catch (Exception e) {
            stopSelf();
            Bundle bundle = new Bundle();
            bundle.putInt("resultCode", Constants.DOWNLOAD_EXCEPRION);
            bundle.putFloat("resultPercent", 0f);
            mResultReceiver.send(Activity.RESULT_OK, bundle);
        }

    }

}
