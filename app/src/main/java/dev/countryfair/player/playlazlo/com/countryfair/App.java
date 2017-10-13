package dev.countryfair.player.playlazlo.com.countryfair;

import android.app.Application;
import android.location.Location;
import android.util.Log;
import android.widget.Toast;

import mobi.windfall.receipt.InitializeCallback;
import mobi.windfall.receipt.ReceiptSdk;
import mobi.windfall.receipt.SdkNotInitializedException;

public class App extends Application {

    public static final String TAG = "App";

    private static App instance;

    private Location location;

    @Override
    public void onCreate() {
        super.onCreate();
        instance = this;

        ReceiptSdk.debug( true );

        ReceiptSdk.deepOcr( true );

        ReceiptSdk.sdkInitialize(getApplicationContext(), new InitializeCallback() {

            @Override
            public void onInitialized() {
                Log.d( TAG, "sdk initialized" );

                Toast.makeText( App.this, "Receipt Playground Initialized", Toast.LENGTH_SHORT ).show();
            }

            @Override
            public void onError(SdkNotInitializedException e) {
                Log.e( TAG, e.toString() );

                Toast.makeText( App.this, "Receipt Playground Exception:" + e.toString(), Toast.LENGTH_SHORT ).show();
            }

        } );
    }

    public static App getInstance() {
        return instance;
    }

    public Location getLocation() {
        return location;
    }

    public void setLocation(Location location) {
        this.location = location;
    }
}
