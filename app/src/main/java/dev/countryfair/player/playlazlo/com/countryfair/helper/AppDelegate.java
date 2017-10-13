package dev.countryfair.player.playlazlo.com.countryfair.helper;

import android.app.Application;
import android.location.Location;
import android.util.Log;
import android.widget.Toast;

import org.greenrobot.greendao.database.Database;

import dev.countryfair.player.playlazlo.com.countryfair.database.AppDb;
import dev.countryfair.player.playlazlo.com.countryfair.helper.db.DatabaseUpgradeHelper;
import dev.countryfair.player.playlazlo.com.countryfair.model.db.DaoMaster;
import dev.countryfair.player.playlazlo.com.countryfair.model.db.DaoSession;
import mobi.windfall.receipt.InitializeCallback;
import mobi.windfall.receipt.ReceiptSdk;
import mobi.windfall.receipt.SdkNotInitializedException;

/**
 * Created by Dev01 on 9/27/2017.
 */

public class AppDelegate extends Application {

    private AppDb mAppDb;

    public static final String TAG = "App";

    private static AppDelegate instance;

    private Location location;

    private DaoSession daoSession;
    private DatabaseUpgradeHelper helper;


    @Override
    public void onCreate() {
        super.onCreate();
        instance = this;

        init();

        ReceiptSdk.debug( true );

        ReceiptSdk.deepOcr( true );

        ReceiptSdk.sdkInitialize(getApplicationContext(), new InitializeCallback() {

            @Override
            public void onInitialized() {
                Log.d( TAG, "sdk initialized" );

                Toast.makeText( AppDelegate.this, "Receipt Playground Initialized", Toast.LENGTH_SHORT ).show();
            }

            @Override
            public void onError(SdkNotInitializedException e) {
                Log.e( TAG, e.toString() );

                Toast.makeText( AppDelegate.this, "Receipt Playground Exception:" + e.toString(), Toast.LENGTH_SHORT ).show();
            }

        } );

        initDao();
    }

    public static AppDelegate getInstance() {
        return instance;
    }

    public Location getLocation() {
        return location;
    }

    public void setLocation(Location location) {
        this.location = location;
    }

    private void init(){
        mAppDb = AppDb.getAppDatabase(getApplicationContext());
    }

    public AppDb getmAppDb() {
        return mAppDb;
    }

    @Override
    public void onTerminate() {
        super.onTerminate();
        if(mAppDb!=null){
            mAppDb.destroyInstance();
        }
    }

    /**
     * Init Database framework
     */
    private void initDao() {
        helper = new DatabaseUpgradeHelper(this, Constants.DB_NAME);
        Database db = helper.getWritableDb();
        daoSession = new DaoMaster(db).newSession();
    }

    /**
     * Get DAO session object
     * @return dao session object
     */
    public DaoSession getDaoSession() {
        return daoSession;
    }

    public String getDatabaseFileName(){
        return helper.getReadableDatabase().getPath();
    }


}
