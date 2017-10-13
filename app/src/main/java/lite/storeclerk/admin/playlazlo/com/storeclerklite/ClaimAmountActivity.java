package lite.storeclerk.admin.playlazlo.com.storeclerklite;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Vibrator;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.TextView;

import org.json.JSONObject;

import java.util.Locale;

import cn.refactor.lib.colordialog.PromptDialog;
import lite.storeclerk.admin.playlazlo.com.storeclerklite.helper.APIInterface;
import lite.storeclerk.admin.playlazlo.com.storeclerklite.helper.AndroidUtilities;

/**
 * Created by mymac on 3/28/17.
 */

public class ClaimAmountActivity extends AppCompatActivity {


    private ProgressDialog mProgressDialog;
    private JSONObject claimObj;
    private JSONObject receivedObj;
    private double amount;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.claim_amount_layout);

        try {
            claimObj = new JSONObject(getIntent().getStringExtra("claimObj"));
            amount = claimObj.getDouble("appliedAmount");
        } catch (Exception e) {

        }

        TextView amountTxt = (TextView) findViewById(R.id.claim_amount_txt);
        amountTxt.setText(String.format(Locale.US, "$%.2f", amount));

        Button completeBtn = (Button) findViewById(R.id.claim_complete_btn);
        Button cancelBtn = (Button) findViewById(R.id.claim_cancel_btn);

        completeBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                completeClaim();
            }
        });

        cancelBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent i = new Intent(ClaimAmountActivity.this, CheckoutActivity.class);
                startActivity(i);
                finish();
            }
        });
    }

    private void completeClaim() {

        mProgressDialog = new ProgressDialog(this);
        mProgressDialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        mProgressDialog.setMessage("Claim Completing...");
        mProgressDialog.show();
        mProgressDialog.setCancelable(false);
        mProgressDialog.setCanceledOnTouchOutside(false);
        new Thread(new Runnable() {
            public void run() {
                try {
                    String uuid = AndroidUtilities.getUUID(ClaimAmountActivity.this);
                    receivedObj = APIInterface.completeClaim(claimObj, uuid);
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {

                            if (mProgressDialog.isShowing()) {
                                mProgressDialog.dismiss();
                            }

                            if (receivedObj != null) {
                                try {
                                    Intent i = new Intent(ClaimAmountActivity.this, ClaimActivity.class);
                                    startActivity(i);
                                    finish();

                                } catch (Exception e) {
                                    Log.d("json_e-->", e.getMessage());
                                }

                            } else {
                                Vibrator v = (Vibrator) ClaimAmountActivity.this.getSystemService(Context.VIBRATOR_SERVICE);
                                v.vibrate(1000);
                                new PromptDialog(ClaimAmountActivity.this)
                                        .setDialogType(PromptDialog.DIALOG_TYPE_WRONG)
                                        .setAnimationEnable(true)
                                        .setTitleText("Claim Complete error")
                                        .setContentText("Oops Claim Complete failed. \n Please restart after exit.")
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
                            Vibrator v = (Vibrator) ClaimAmountActivity.this.getSystemService(Context.VIBRATOR_SERVICE);
                            v.vibrate(1000);
                            new PromptDialog(ClaimAmountActivity.this)
                                    .setDialogType(PromptDialog.DIALOG_TYPE_WRONG)
                                    .setAnimationEnable(true)
                                    .setTitleText("Claim Complete error")
                                    .setContentText("Oops Claim Complete failed. \n Please restart after exit.")
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
}