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
import dev.countryfair.player.playlazlo.com.countryfair.adapter.GiftcardCheckoutFileDownloadListAdapter;
import dev.countryfair.player.playlazlo.com.countryfair.helper.AppHelper;
import wseemann.media.FFmpegMediaMetadataRetriever;

/**
 * Created by mymac on 3/18/17.
 */

public class GiftcardCheckoutFileDownloadActivity extends AppCompatActivity {

    private List<JSONObject> shoppingCartList = new ArrayList<JSONObject>();
    private List<JSONObject> downloadFileList = new ArrayList<JSONObject>();
    private List<JSONObject> ticketSuccessList = new ArrayList<JSONObject>();
    private List<JSONObject> ticketErrorList = new ArrayList<JSONObject>();

    private JSONArray shoppingCartArr = new JSONArray();
    private JSONArray downloadFileArr = new JSONArray();
    private JSONArray ticketSuccessArr = new JSONArray();
    private JSONArray ticketErrorArr = new JSONArray();

    private boolean b_downloadCompleteFlg = false;
    private GiftcardCheckoutFileDownloadListAdapter mAdapter;

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
        mAdapter = new GiftcardCheckoutFileDownloadListAdapter(this, shoppingCartList, downloadFileList, ticketSuccessList, ticketErrorList);
        mListView.setAdapter(mAdapter);

        mAdapter.setGiftcardDownloadListener(new GiftcardCheckoutFileDownloadListAdapter.OnGiftcardDownloadListener() {
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
                    if (ticketStatus) {

                        for (JSONObject ticketSuccessItem : ticketSuccessList) {
                            if (ticketSuccessItem.getBoolean("isGiftCard")) {
                                new AppHelper(GiftcardCheckoutFileDownloadActivity.this).saveOneRedeemCardItemToLocal(ticketSuccessItem);
                            } else {
                                new AppHelper(GiftcardCheckoutFileDownloadActivity.this).saveOneCouponItemToLocal(ticketSuccessItem);
                            }
                        }
                    } else {

                    }

                    if (b_downloadCompleteFlg) {

                        new AppHelper(getApplicationContext()).saveInitShoppingCartGift();


                        Intent i = new Intent(GiftcardCheckoutFileDownloadActivity.this, MainActivity.class);
                        GiftcardCheckoutFileDownloadActivity.this.startActivity(i);
                        finish();
                    }
                } catch (Exception exc) {
                    Log.e("putting_err-->", exc.getMessage());
                }
            }
        });

    }

}
