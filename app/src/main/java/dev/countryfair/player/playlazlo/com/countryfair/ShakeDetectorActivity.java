package dev.countryfair.player.playlazlo.com.countryfair;

import android.content.Intent;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.os.PersistableBundle;
import android.support.annotation.Nullable;
import android.support.v4.app.FragmentActivity;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.widget.Toast;

import com.squareup.seismic.ShakeDetector;

/**
 * Created by Dev01 on 10/3/2017.
 */

public class ShakeDetectorActivity extends AppCompatActivity implements ShakeDetector.Listener {

    private static final String TAG = "ShakeDetectorActivity";

    private SensorManager sensorManager;
    private ShakeDetector shakeDetector;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        sensorManager = (SensorManager) getSystemService(SENSOR_SERVICE);
        shakeDetector = new ShakeDetector(this);
    }

    @Override
    protected void onResume() {
        super.onResume();
        shakeDetector.start(sensorManager);
    }


    @Override
    protected void onPause() {
        super.onPause();
        shakeDetector.stop();
    }

    @Override
    public void hearShake() {
        Log.d(TAG, "hearShake: ");
        startActivity(new Intent(this,SocialScanActivity.class));
    }
}
