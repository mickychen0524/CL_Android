package lite.storeclerk.admin.playlazlo.com.storeclerklite;

import android.content.Context;
import android.support.multidex.MultiDex;
import android.support.multidex.MultiDexApplication;

import project.labs.avviotech.com.chatsdk.nearby.NearByUtil;

/**
 * Created by mymac on 4/3/17.
 */

public class App extends MultiDexApplication {
    @Override
    protected void attachBaseContext(Context base) {
        super.attachBaseContext(base);
        MultiDex.install(this);
    }

    @Override
    public void onCreate() {
        super.onCreate();
    }

    @Override
    public void onTerminate() {
        super.onTerminate();

    }
}
