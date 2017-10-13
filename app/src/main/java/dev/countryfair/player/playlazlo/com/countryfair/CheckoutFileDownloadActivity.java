package dev.countryfair.player.playlazlo.com.countryfair;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.os.Environment;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.widget.ListView;

import com.google.zxing.BinaryBitmap;
import com.google.zxing.LuminanceSource;
import com.google.zxing.MultiFormatReader;
import com.google.zxing.RGBLuminanceSource;
import com.google.zxing.Reader;
import com.google.zxing.Result;
import com.google.zxing.common.HybridBinarizer;
import org.json.JSONArray;
import org.json.JSONObject;
import java.io.File;
import java.io.FileInputStream;
import java.util.ArrayList;
import java.util.List;
import dev.countryfair.player.playlazlo.com.countryfair.adapter.CheckoutFileDownloadListAdapter;
import dev.countryfair.player.playlazlo.com.countryfair.helper.AppHelper;
import dev.countryfair.player.playlazlo.com.countryfair.helper.Constants;
import wseemann.media.FFmpegMediaMetadataRetriever;

/**
 * Created by mymac on 3/18/17.
 */

public class CheckoutFileDownloadActivity extends AppCompatActivity {

    private List<JSONObject> shoppingCartList = new ArrayList<JSONObject>();
    private List<JSONObject> downloadFileList = new ArrayList<JSONObject>();
    private List<JSONObject> ticketSuccessList = new ArrayList<JSONObject>();
    private List<JSONObject> ticketErrorList = new ArrayList<JSONObject>();

    private JSONArray shoppingCartArr = new JSONArray();
    private JSONArray downloadFileArr = new JSONArray();
    private JSONArray ticketSuccessArr = new JSONArray();
    private JSONArray ticketErrorArr = new JSONArray();

    private boolean b_downloadCompleteFlg = false;
    private CheckoutFileDownloadListAdapter mAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Intent intent = getIntent();
        try {
            shoppingCartArr = new JSONArray(intent.getStringExtra("shoppingCartArr"));
            downloadFileArr = new JSONArray(intent.getStringExtra("resDataArr"));
            ticketSuccessArr = new JSONArray(intent.getStringExtra("ticketSuccessArr"));
            ticketErrorArr = new JSONArray(intent.getStringExtra("ticketErrorArr"));

            shoppingCartList = AppHelper.parseFromJsonList(shoppingCartArr);
            downloadFileList = AppHelper.parseFromJsonList(downloadFileArr);
            ticketSuccessList = AppHelper.parseFromJsonList(ticketSuccessArr);
            ticketErrorList = AppHelper.parseFromJsonList(ticketErrorArr);
            for (int i = 0; i < downloadFileList.size(); i++) {
                downloadFileList.set(i, downloadFileList.get(i).put("downloaded", false));
            }
        } catch (Exception e) {
            Log.e("json_parsing-->", e.getMessage());
        }

        setContentView(R.layout.checkout_file_download_activity);
        ListView mListView = (ListView) findViewById(R.id.download_file_list);
        mAdapter = new CheckoutFileDownloadListAdapter(this, shoppingCartList, downloadFileList, ticketSuccessList, ticketErrorList);
        mListView.setAdapter(mAdapter);

        mAdapter.setTicketDownloadListener(new CheckoutFileDownloadListAdapter.OnTicketDownloadListener() {
            @Override
            public void onDataChanged(int position, boolean ticketStatus) {
                try {

                    downloadFileList.set(position, downloadFileList.get(position).put("downloaded", true));
                    for (JSONObject obj : downloadFileList) {
                        if (obj.getBoolean("downloaded")) {
                            b_downloadCompleteFlg = true;
                        } else {
                            b_downloadCompleteFlg = false;
                            break;
                        }
                    }
                    final String localFilePathForTicket = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS) + "/"+ Constants.DIR_ROOT+"/"+Constants.DIR_TICKETS+"/";
                    JSONObject downloadFileItem = downloadFileList.get(position);
                    String ticketRefId = downloadFileItem.getString("ticketRefId");
                    if (ticketStatus) {

                        for (JSONObject ticketSuccessItem : ticketSuccessList) {
                            if (ticketSuccessItem.getString("ticketRefId").equals(ticketRefId)) {

                                String fileName = ticketSuccessItem.getString("fileName");

                                if (fileName.contains("mp4")) { // video ticket file from local

                                    File sourceFile = new File(localFilePathForTicket + fileName);
                                    if (sourceFile.exists()) {

                                        FFmpegMediaMetadataRetriever mmr = new FFmpegMediaMetadataRetriever();
                                        mmr.setDataSource(localFilePathForTicket + fileName);
                                        String cyperStr = mmr.extractMetadata(FFmpegMediaMetadataRetriever.METADATA_KEY_COMMENT);

                                        ticketSuccessItem.put("licenseCypherText", cyperStr);
                                        Log.d("sample", cyperStr);
                                    }
                                } else {                    // image ticket file from local
                                    File bmpFile = new File(localFilePathForTicket + fileName);
                                    if (bmpFile.exists()) {
                                        BitmapFactory.Options options = new BitmapFactory.Options();
                                        options.inJustDecodeBounds = false;
                                        options.inPreferredConfig = Bitmap.Config.RGB_565;
                                        options.inSampleSize = 1;
                                        try {
                                            Bitmap ticketImage = BitmapFactory.decodeStream(new FileInputStream(bmpFile), null, options);
                                            String cyperStr = scanQRImage(ticketImage);
                                            ticketSuccessItem.put("licenseCypherText", cyperStr);
                                            Log.d("cyperTxxt", cyperStr);
                                        } catch (Exception e) {
                                            Log.e("input stream error -->", e.getMessage());
                                        }
                                    }
                                }

                                new AppHelper(CheckoutFileDownloadActivity.this).saveOneTicketDataToLocal(ticketSuccessItem);
                                break;
                            }
                        }
                    } else {
                        for (JSONObject ticketErrorItem : ticketErrorList) {
                            if (ticketErrorItem.getString("ticketRefId").equals(ticketRefId)) {
                                new AppHelper(CheckoutFileDownloadActivity.this).saveOneTicketErrorDataToLocal(ticketErrorItem);
                            }
                        }
                    }

                    if (b_downloadCompleteFlg) {

                        new AppHelper(CheckoutFileDownloadActivity.this).saveInitShoppingCart();

                        if (ticketErrorList.size() != 0) {
                            Intent i = new Intent(CheckoutFileDownloadActivity.this, TicketRefundActivity.class);
                            CheckoutFileDownloadActivity.this.startActivity(i);
                            finish();
                        } else {
                            Intent i = new Intent(CheckoutFileDownloadActivity.this, TicketListActivity.class);
                            CheckoutFileDownloadActivity.this.startActivity(i);
                            finish();
                        }

                    }
                } catch (Exception exc) {
                    Log.e("putting_err-->", exc.getMessage());
                }
            }
        });

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
