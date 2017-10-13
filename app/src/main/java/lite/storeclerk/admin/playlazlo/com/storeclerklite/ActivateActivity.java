package lite.storeclerk.admin.playlazlo.com.storeclerklite;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.PointF;
import android.os.Build;
import android.os.Bundle;
import android.os.Vibrator;
import android.preference.PreferenceManager;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.dlazaro66.qrcodereaderview.QRCodeReaderView;

import org.json.JSONObject;

import java.util.Locale;

import lite.storeclerk.admin.playlazlo.com.storeclerklite.helper.Constants;

/**
 * Created by mymac on 6/20/17.
 */

public class ActivateActivity extends AppCompatActivity implements QRCodeReaderView.OnQRCodeReadListener {

    private static final int PERMISSION_ALL = 1;
    String[] PERMISSIONS = {android.Manifest.permission.CAMERA};
    private QRCodeReaderView mydecoderview;

    private boolean b_alertStateFlg = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activate_layout);

        b_alertStateFlg = false;
        if(!hasPermissions(this, PERMISSIONS)){
            ActivityCompat.requestPermissions(this, PERMISSIONS, PERMISSION_ALL);
        } else {
            mydecoderview = (QRCodeReaderView) findViewById(R.id.activate_qrdecoderview);

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

    }

    public void onBackAction(View view) {
        Intent i = new Intent(this, MainActivity.class);
        i.putExtra("backView", "back");
        startActivity(i);
        finish();
    }

    public static boolean hasPermissions(Context context, String... permissions) {
        if (android.os.Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && context != null && permissions != null) {
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
            if (text.length() > 120) {
                SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(ActivateActivity.this.getApplicationContext());
                SharedPreferences.Editor editor = sharedPref.edit();
                editor.putString("playerToken",text);
                Constants.PLAYER_TOKEN = text;
                editor.putBoolean("activateState", true);
                editor.apply();

                Intent i = new Intent(ActivateActivity.this, MainActivity.class);
                startActivity(i);
                finish();

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
    @Override
    public void onStart() {
        super.onStart();

    }

    @Override
    public void onStop() {
        super.onStop();

    }
}
