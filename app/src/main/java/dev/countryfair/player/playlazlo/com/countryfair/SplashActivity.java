package dev.countryfair.player.playlazlo.com.countryfair;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;

/**
 * Created by Dev01 on 10/6/2017.
 */

public class SplashActivity extends AppCompatActivity {

    private static final String TAG = "SplashActivity";

    private Handler mHandler = new Handler();
    private Runnable mRunnable = new Runnable() {
        @Override
        public void run() {
            Log.d(TAG, "run() called");
            if(!isFinishing()){
                Log.d(TAG, "run() called and moved out");
                startActivity(new Intent(SplashActivity.this,PermissionActivity.class));
                finish();
            }
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);

        Log.d(TAG, "onCreate: ");
        mHandler.postDelayed(mRunnable,2000);
    }
}
