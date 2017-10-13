package dev.countryfair.player.playlazlo.com.countryfair.azuregcm;

import android.app.Service;
import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Environment;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.util.Log;

import com.google.zxing.BinaryBitmap;
import com.google.zxing.LuminanceSource;
import com.google.zxing.MultiFormatReader;
import com.google.zxing.RGBLuminanceSource;
import com.google.zxing.Reader;
import com.google.zxing.Result;
import com.google.zxing.common.HybridBinarizer;
import com.koushikdutta.async.future.FutureCallback;
import com.koushikdutta.ion.Ion;
import com.koushikdutta.ion.ProgressCallback;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;

import dev.countryfair.player.playlazlo.com.countryfair.CheckoutFileDownloadActivity;
import dev.countryfair.player.playlazlo.com.countryfair.event.CouponListInvalidateEvent;
import dev.countryfair.player.playlazlo.com.countryfair.event.EventBus;
import dev.countryfair.player.playlazlo.com.countryfair.helper.AppHelper;
import dev.countryfair.player.playlazlo.com.countryfair.helper.Constants;

public class SilentDownloadService extends Service {

    private static final String TAG = SilentDownloadService.class.getSimpleName();
    private JSONObject jsonMessage = new JSONObject();

    public SilentDownloadService() {
        super();

    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {

        if (intent != null) {
            String message = intent.getStringExtra("message");
            initiateSilentDownload(message);
        }

        return super.onStartCommand(intent, flags, startId);
    }

    private void initiateSilentDownload(String message){
        try {
            jsonMessage = new JSONObject(message);
            final String sasUrl = jsonMessage.getString("sasUri");
            final String fileName = jsonMessage.getString("fileName");
            final String localFilePathForCoupons = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS) + "/"+ Constants.DIR_ROOT+"/"+Constants.DIR_COUPONS+"/";
            final File localFileForCoupons = new File(localFilePathForCoupons);
            if (!localFileForCoupons.exists()) {
                localFileForCoupons.mkdir();
            }

            final File dataFile = new File(localFilePathForCoupons+fileName);


            Ion.with(SilentDownloadService.this.getApplicationContext()).load(sasUrl).setLogging("data",Log.VERBOSE).progress(new ProgressCallback() {
                @Override
                public void onProgress(long downloaded, long total) {
                    Log.d(TAG, "" + downloaded + " / " + total);
                }
            }).write(dataFile).setCallback(new FutureCallback<File>() {
                @Override
                public void onCompleted(Exception e, File result) {
                    new AppHelper(SilentDownloadService.this.getApplicationContext()).saveOneCouponItemToLocal(jsonMessage);
                    EventBus.getInstance().post(new CouponListInvalidateEvent());
                    stopSelf();
                }
            });
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    private String scanQRImage(Bitmap bMap) {
        String contents = null;

        int[] intArray = new int[bMap.getWidth()*bMap.getHeight()];
        //copy pixel data from the Bitmap into the 'intArray' array
        bMap.getPixels(intArray, 0, bMap.getWidth(), 0, 0, bMap.getWidth(), bMap.getHeight());

        LuminanceSource source = new RGBLuminanceSource(bMap.getWidth(), bMap.getHeight(), intArray);
        BinaryBitmap bitmap = new BinaryBitmap(new HybridBinarizer(source));

        Reader reader = new MultiFormatReader();
        try {
            Result result = reader.decode(bitmap);
            contents = result.getText();
        }
        catch (Exception e) {
            Log.e("QrTest", "Error decoding barcode", e);
        }
        return contents;
    }

}
