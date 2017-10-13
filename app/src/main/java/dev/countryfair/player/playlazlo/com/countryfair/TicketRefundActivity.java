package dev.countryfair.player.playlazlo.com.countryfair;

import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v7.app.AppCompatActivity;
import android.telephony.TelephonyManager;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.zxing.BarcodeFormat;
import com.google.zxing.MultiFormatWriter;
import com.google.zxing.common.BitMatrix;

import org.json.JSONArray;
import org.json.JSONObject;

import java.text.DateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import cn.refactor.lib.colordialog.PromptDialog;
import dev.countryfair.player.playlazlo.com.countryfair.adapter.TicketRefundListAdapter;
import dev.countryfair.player.playlazlo.com.countryfair.helper.APIInterface;
import dev.countryfair.player.playlazlo.com.countryfair.helper.AdvancedHTTPClient;
import dev.countryfair.player.playlazlo.com.countryfair.helper.AndroidUtilities;
import dev.countryfair.player.playlazlo.com.countryfair.helper.AppHelper;
import dev.countryfair.player.playlazlo.com.countryfair.helper.Constants;

/**
 * Created by mymac on 2/25/17.
 */

public class TicketRefundActivity extends AppCompatActivity {

    private List<JSONObject> ticketErrorDataList = new ArrayList<>();
    private JSONArray ticketErrorDataArr = new JSONArray();
    private JSONObject receivedObj = new JSONObject();
    private TicketRefundListAdapter mAdapter;
    private ProgressDialog mProgressDialog;
    private ListView mListView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.ticket_refund_activity);
        mListView = (ListView) findViewById(R.id.ticket_refund_list_listview);
        mAdapter = new TicketRefundListAdapter(this, loadAllTicketErrorData());
        mListView.setAdapter(mAdapter);

        mAdapter.setOnTicketRefundListener(new TicketRefundListAdapter.OnTicketRefundListener() {
            @Override
            public void onTicketRefunded(int position) {
                refundTicketErrorData(position);
            }
        });

    }
    public void back(View view) {
        Intent i = new Intent(this, MainActivity.class);
        i.putExtra("backView", "back");
        startActivity(i);
        finish();
    }

    private boolean checkHelpOverlayState() {
        SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
        return sharedPref.getBoolean("refundTicketViewHelpOverlayShown", false);
    }

    // create help overlay view
    private void createHelpOverlayView () {
        final Dialog dialog = new Dialog(this);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        dialog.setContentView(R.layout.modal_help_ticketrefund_layout);

        TextView helpTxt = (TextView) dialog.findViewById(R.id.modal_help_overlay);
        helpTxt.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(TicketRefundActivity.this.getApplicationContext());
                SharedPreferences.Editor editor = sharedPref.edit();
                editor.putBoolean("refundTicketViewHelpOverlayShown", true);
                editor.apply();
                dialog.dismiss();
            }
        });

        dialog.show();
    }

    public void viewHelpOverlay(View view) {
        createHelpOverlayView();
    }

    private List<JSONObject> loadAllTicketErrorData() {
        ticketErrorDataArr = new AppHelper(this).getTicketErrorDataFromLocal();
        ticketErrorDataList = AppHelper.parseFromJsonList(ticketErrorDataArr);
        if (!checkHelpOverlayState()) {
            createHelpOverlayView();
        }
        return ticketErrorDataList;
    }

    private void refundTicketErrorData(int position) {

        final JSONObject ticketRefundItem = ticketErrorDataList.get(position);

        mProgressDialog = new ProgressDialog(this);
        mProgressDialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        mProgressDialog.setMessage("Refunding...");
        mProgressDialog.show();
        mProgressDialog.setCancelable(false);
        mProgressDialog.setCanceledOnTouchOutside(false);
        new Thread(new Runnable() {
            public void run() {
                try {
                    String uuid = AndroidUtilities.getUUID(TicketRefundActivity.this);
                    receivedObj = APIInterface.ticketRefund(ticketRefundItem,uuid);
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {

                            if (mProgressDialog.isShowing()) {
                                mProgressDialog.dismiss();
                            }

                            if (receivedObj != null) {
                                try {
                                    JSONObject jsonData = receivedObj.getJSONObject("data");
                                    createRefundDLG(jsonData, ticketRefundItem);

                                } catch (Exception e) {
                                    Log.d("json_e-->", e.getMessage());
                                }

                            } else {
                                new PromptDialog(TicketRefundActivity.this)
                                        .setDialogType(PromptDialog.DIALOG_TYPE_WRONG)
                                        .setAnimationEnable(true)
                                        .setTitleText(Constants.ERROR_TITLE)
                                        .setContentText(Constants.ERROR_MESSAGE)
                                        .setPositiveListener("Ok", new PromptDialog.OnPositiveListener() {
                                            @Override
                                            public void onClick(PromptDialog dialog) {
                                                dialog.dismiss();
                                            }
                                        }).show();
                            }

                        }
                    });
                } catch (final Exception e) {
                    Log.e("register error--->", e.getMessage());
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            if (mProgressDialog.isShowing()) {
                                mProgressDialog.dismiss();
                            }
                            new PromptDialog(TicketRefundActivity.this)
                                    .setDialogType(PromptDialog.DIALOG_TYPE_WRONG)
                                    .setAnimationEnable(true)
                                    .setTitleText(Constants.ERROR_TITLE)
                                    .setContentText(Constants.ERROR_MESSAGE)
                                    .setPositiveListener("Ok", new PromptDialog.OnPositiveListener() {
                                        @Override
                                        public void onClick(PromptDialog dialog) {
                                            dialog.dismiss();
                                        }
                                    }).show();
                        }
                    });

                }
            }
        }).start();

    }

    private void createRefundDLG(final JSONObject refundObj, final JSONObject ticketRefundItem) {

        final Dialog refundResDlg = new Dialog(this);
        refundResDlg.requestWindowFeature(Window.FEATURE_NO_TITLE);
        refundResDlg.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        refundResDlg.setContentView(R.layout.modal_ticket_refund_layout);

        ImageView qrcodeImg = (ImageView) refundResDlg.findViewById(R.id.ticket_refund_qrcode_img);
         MultiFormatWriter writer =new MultiFormatWriter();

        try {
            String finaldata = Uri.encode(refundObj.getString("qrCodeBase64"), "utf-8");

            BitMatrix bm = writer.encode(finaldata, BarcodeFormat.QR_CODE,350, 350);
            Bitmap ImageBitmap = Bitmap.createBitmap(350, 350, Bitmap.Config.ARGB_8888);

            for (int i = 0; i < 350; i++) {//width
                for (int j = 0; j < 350; j++) {//height
                    ImageBitmap.setPixel(i, j, bm.get(i, j) ? Color.BLACK: Color.WHITE);
                }
            }

            if (ImageBitmap != null) {
                qrcodeImg.setImageBitmap(ImageBitmap);
            } else {
                Toast.makeText(getApplicationContext(), "QRCode image generating error", Toast.LENGTH_SHORT).show();
            }

        } catch (Exception e) {
            Log.e("checkout_dlg_e-->", e.getMessage());
        }

        Button okBtn = (Button) refundResDlg.findViewById(R.id.ticket_refund_ok_btn);
        okBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                new AppHelper(TicketRefundActivity.this).deleteOneTicketErrorDataItem(ticketRefundItem);
                mAdapter = new TicketRefundListAdapter(TicketRefundActivity.this, loadAllTicketErrorData());
                mAdapter.notifyDataSetChanged();
                mListView.setAdapter(mAdapter);
                refundResDlg.dismiss();
            }
        });

        refundResDlg.show();
        AndroidUtilities.setDialogLayoutParams(TicketRefundActivity.this,refundResDlg);
    }
}
