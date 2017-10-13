package dev.countryfair.player.playlazlo.com.countryfair.helper;

import android.app.Activity;

import java.io.IOException;
import java.io.InputStream;

/**
 * Created by Nyam on 2/17/17.
 */

public class GettingJSONFromLocal {
    public static String loadJSONFromAsset(Activity activity, String fileName) {
        String json = null;
        try {
            InputStream is = activity.getAssets().open(fileName);
            int size = is.available();
            byte[] buffer = new byte[size];
            is.read(buffer);
            is.close();
            json = new String(buffer, "UTF-8");
        } catch (IOException ex) {
            ex.printStackTrace();
            return null;
        }
        return json;
    }

}
