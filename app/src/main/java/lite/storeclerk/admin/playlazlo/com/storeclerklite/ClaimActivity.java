package lite.storeclerk.admin.playlazlo.com.storeclerklite;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.graphics.PointF;
import android.graphics.drawable.GradientDrawable;
import android.os.Build;
import android.os.Bundle;
import android.os.Vibrator;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.dlazaro66.qrcodereaderview.QRCodeReaderView;

import org.json.JSONObject;

import cn.refactor.lib.colordialog.PromptDialog;
import lite.storeclerk.admin.playlazlo.com.storeclerklite.helper.APIInterface;
import lite.storeclerk.admin.playlazlo.com.storeclerklite.helper.AndroidUtilities;

/**
 * Created by mymac on 3/28/17.
 */

public class ClaimActivity extends AppCompatActivity implements QRCodeReaderView.OnQRCodeReadListener {

    private static final int PERMISSION_ALL = 1;
    String[] PERMISSIONS = {android.Manifest.permission.CAMERA};
    private QRCodeReaderView mydecoderview;

    private EditText codeTxt;
    private TextView errorTxt;
    private Button nextBtn;
    private ProgressDialog mProgressDialog;
    private JSONObject receivedObj;

    private boolean b_alertStateFlg = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.claim_layout);
        b_alertStateFlg = false;
        if(!hasPermissions(this, PERMISSIONS)){
            ActivityCompat.requestPermissions(this, PERMISSIONS, PERMISSION_ALL);
        } else {
            mydecoderview = (QRCodeReaderView) findViewById(R.id.claim_qrdecoderview);

            mydecoderview.setOnQRCodeReadListener(this);

            // Use this function to enable/disable decoding
            mydecoderview.setQRDecodingEnabled(true);

            // Use this function to change the autofocus interval (default is 5 secs)
            mydecoderview.setAutofocusInterval(2000L);

            // Use this function to enable/disable Torch
            mydecoderview.setTorchEnabled(true);

            // Use this function to set front camera preview
            //        mydecoderview.setFrontCamera();

            // Use this function to set back camera preview
            mydecoderview.setBackCamera();
            mydecoderview.startCamera();
        }

        codeTxt = (EditText) findViewById(R.id.claim_insert_code_edit);
        errorTxt = (TextView) findViewById(R.id.claim_error_txt);
        errorTxt.setVisibility(View.GONE);
        codeTxt.setBackgroundResource(R.drawable.background_light_blue_rect);
        GradientDrawable drawable = (GradientDrawable) codeTxt.getBackground();
        drawable.setStroke(1, Color.TRANSPARENT);
        nextBtn = (Button) findViewById(R.id.claim_next_btn);
        Button cancelBtn = (Button) findViewById(R.id.claim_cancel_btn);
        nextBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (codeTxt.getText().toString().equals("12345")) {
                    errorTxt.setVisibility(View.GONE);
                    codeTxt.setBackgroundResource(R.drawable.background_light_blue_rect);
                    GradientDrawable drawable = (GradientDrawable) codeTxt.getBackground();
                    drawable.setStroke(1, Color.TRANSPARENT);
                    claimWithCode(codeTxt.getText().toString());
                } else {
                    codeTxt.setBackgroundResource(R.drawable.background_light_blue_rect);
                    GradientDrawable drawable = (GradientDrawable) codeTxt.getBackground();
                    drawable.setStroke(1, Color.RED);
                    errorTxt.setVisibility(View.VISIBLE);
                    Vibrator v = (Vibrator) ClaimActivity.this.getSystemService(Context.VIBRATOR_SERVICE);
                    v.vibrate(1000);
                }
            }
        });

        codeTxt.addTextChangedListener(new TextWatcher() {
            @Override
            public void afterTextChanged(Editable s) {
                // TODO Auto-generated method stub
            }

            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                // TODO Auto-generated method stub
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                errorTxt.setVisibility(View.GONE);
                codeTxt.setBackgroundResource(R.drawable.background_light_blue_rect);
                GradientDrawable drawable = (GradientDrawable) codeTxt.getBackground();
                drawable.setStroke(1, Color.TRANSPARENT);
            }
        });
        codeTxt.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
//                if (hasFocus) {
//                    ClaimActivity.this.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_VISIBLE);
//                }
            }
        });
        cancelBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent i = new Intent(ClaimActivity.this, MainActivity.class);
                startActivity(i);
                finish();
            }
        });

        codeTxt.setFocusableInTouchMode(true);
        codeTxt.setFocusable(true);
        codeTxt.requestFocus();

    }

    @Override
    protected void onResume() {
        super.onResume();
        if (mydecoderview != null){
            mydecoderview.startCamera();
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (mydecoderview != null){
            mydecoderview.stopCamera();
        }

    }

    private void claimWithCode(final String code) {

        mProgressDialog = new ProgressDialog(this);
        mProgressDialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        mProgressDialog.setMessage("Claiming...");
        mProgressDialog.show();
        mProgressDialog.setCancelable(false);
        mProgressDialog.setCanceledOnTouchOutside(false);
        new Thread(new Runnable() {
            public void run() {
                try {
                    String uuid = AndroidUtilities.getUUID(ClaimActivity.this);
                    receivedObj = APIInterface.claimWithCode(code);
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {

                            if (mProgressDialog.isShowing()) {
                                mProgressDialog.dismiss();
                            }

                            if (receivedObj != null) {
                                Log.i("devoloTest", "");
                                try {
                                    JSONObject jsonData = receivedObj.getJSONObject("data");

                                    Intent i = new Intent(ClaimActivity.this, ClaimAmountActivity.class);
                                    i.putExtra("claimObj", jsonData.toString());
                                    startActivity(i);
                                    finish();

                                } catch (Exception e) {
                                    Log.d("json_e-->", e.getMessage());
                                }

                            } else {
                                Vibrator v = (Vibrator) ClaimActivity.this.getSystemService(Context.VIBRATOR_SERVICE);
                                v.vibrate(1000);
                                new PromptDialog(ClaimActivity.this)
                                        .setDialogType(PromptDialog.DIALOG_TYPE_WRONG)
                                        .setAnimationEnable(true)
                                        .setTitleText("Claim error")
                                        .setContentText("Oops Claim failed. \n Please restart after exit.")
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
                            Vibrator v = (Vibrator) ClaimActivity.this.getSystemService(Context.VIBRATOR_SERVICE);
                            v.vibrate(1000);
                            new PromptDialog(ClaimActivity.this)
                                    .setDialogType(PromptDialog.DIALOG_TYPE_WRONG)
                                    .setAnimationEnable(true)
                                    .setTitleText("Claim error")
                                    .setContentText("Oops Claim failed. \n Please restart after exit.")
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

    public static boolean hasPermissions(Context context, String... permissions) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && context != null && permissions != null) {
            for (String permission : permissions) {
                if (ActivityCompat.checkSelfPermission(context, permission) != PackageManager.PERMISSION_GRANTED) {
                    return false;
                }
            }
        }
        return true;
    }
    @Override
    public void onQRCodeRead(String text, PointF[] points) {
        // show the scanner result into dialog box.

        if (!b_alertStateFlg) {
            b_alertStateFlg = true;
            if (text.contains("[CC]")) {
                claimWithCode(text);
            } else {
                b_alertStateFlg = false;
                Toast toast = Toast.makeText(getApplicationContext(), "Scan Result Error : " + text, Toast.LENGTH_SHORT);
                toast.show();

                Vibrator v = (Vibrator) this.getSystemService(Context.VIBRATOR_SERVICE);
                // Vibrate for 500 milliseconds
                v.vibrate(500);
            }
        }
    }
}