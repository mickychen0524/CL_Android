package lite.storeclerk.admin.playlazlo.com.storeclerklite.helper;

import android.app.Activity;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

import lite.storeclerk.admin.playlazlo.com.storeclerklite.BuildConfig;

/**
 * Created by mymac on 2/27/17.
 */

public class SetInitialVarsOnLocal {
    public SetInitialVarsOnLocal() {

    }
    public SetInitialVarsOnLocal(Activity activity) {
        SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(activity.getApplicationContext());
        SharedPreferences.Editor editor = sharedPref.edit();
        editor.putBoolean("startAppState", false);
        editor.putBoolean("cameraState", false);
        editor.putBoolean("locationState", false);
        editor.putBoolean("storageState", false);
        editor.putBoolean("loginState", false);
        editor.putBoolean("activateState", false);
        editor.putBoolean("registerState", false);
        editor.putString("serviceURL", BuildConfig.SERVICE_URL);
        editor.putString("token", BuildConfig.TOKEN);
        editor.putString("playerToken", BuildConfig.PLAYER_TOKEN);
        editor.apply();
    }

}
