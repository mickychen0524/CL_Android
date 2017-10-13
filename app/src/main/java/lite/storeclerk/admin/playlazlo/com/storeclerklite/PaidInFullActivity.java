package lite.storeclerk.admin.playlazlo.com.storeclerklite;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Vibrator;
import android.support.v7.app.AppCompatActivity;
import android.telephony.TelephonyManager;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.TextView;

import org.json.JSONObject;

import java.text.DateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.concurrent.Exchanger;

import cn.refactor.lib.colordialog.PromptDialog;
import lite.storeclerk.admin.playlazlo.com.storeclerklite.helper.APIInterface;

import lite.storeclerk.admin.playlazlo.com.storeclerklite.helper.AndroidUtilities;
import lite.storeclerk.admin.playlazlo.com.storeclerklite.helper.Constants;

/**
 * Created by mymac on 3/28/17.
 */

public class PaidInFullActivity extends AppCompatActivity {


    private ProgressDialog mProgressDialog;
    private JSONObject checkoutObj;
    private JSONObject receivedObj;
    private double amount;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.paid_in_full_layout);

        try {
            checkoutObj = new JSONObject(getIntent().getStringExtra("checkoutObj"));
            amount = checkoutObj.getDouble("amount");
        } catch (Exception e) {

        }

        TextView amountTxt = (TextView) findViewById(R.id.paidview_amount_txt);
        amountTxt.setText(String.format(Locale.US, "$%.2f", amount));

        Button paidInFullbtn = (Button) findViewById(R.id.paidview_paid_btn);
        Button refuseBtn = (Button) findViewById(R.id.paidview_refuse_btn);
        Button cancelBtn = (Button) findViewById(R.id.paidview_cancel_btn);

        paidInFullbtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                paidInFullWithLicenseCode(checkoutObj);
            }
        });

        refuseBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent i = new Intent(PaidInFullActivity.this, CheckoutActivity.class);
                startActivity(i);
                finish();
            }
        });

        cancelBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent i = new Intent(PaidInFullActivity.this, CheckoutActivity.class);
                startActivity(i);
                finish();
            }
        });
    }

    private void paidInFullWithLicenseCode(final JSONObject obj) {

        mProgressDialog = new ProgressDialog(this);
        mProgressDialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        mProgressDialog.setMessage("Checkout completing...");
        mProgressDialog.show();
        mProgressDialog.setCancelable(false);
        mProgressDialog.setCanceledOnTouchOutside(false);
        new Thread(new Runnable() {
            public void run() {
                try {
                    String uuid = AndroidUtilities.getUUID(PaidInFullActivity.this);
                    receivedObj = APIInterface.paidFullWithLicenseCode(obj,uuid);
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {

                            if (mProgressDialog.isShowing()) {
                                mProgressDialog.dismiss();
                            }

                            if (receivedObj != null) {
                                try {
                                    double jsonData = receivedObj.getDouble("data");

                                    Intent i = new Intent(PaidInFullActivity.this, CheckoutActivity.class);
                                    startActivity(i);
                                    finish();

                                } catch (Exception e) {
                                    Log.d("json_e-->", e.getMessage());
                                }

                            } else {
                                Vibrator v = (Vibrator) PaidInFullActivity.this.getSystemService(Context.VIBRATOR_SERVICE);
                                v.vibrate(1000);
                                new PromptDialog(PaidInFullActivity.this)
                                        .setDialogType(PromptDialog.DIALOG_TYPE_WRONG)
                                        .setAnimationEnable(true)
                                        .setTitleText("Checkout error")
                                        .setContentText("Oops Checkout failed. \n Please restart after exit.")
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
                            Vibrator v = (Vibrator) PaidInFullActivity.this.getSystemService(Context.VIBRATOR_SERVICE);
                            v.vibrate(1000);
                            new PromptDialog(PaidInFullActivity.this)
                                    .setDialogType(PromptDialog.DIALOG_TYPE_WRONG)
                                    .setAnimationEnable(true)
                                    .setTitleText("Checkout error")
                                    .setContentText("Oops Checkout failed. \n Please restart after exit.")
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

    private void refuseWithLicenseCode(JSONObject obj) {

    }
}