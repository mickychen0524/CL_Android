package dev.countryfair.player.playlazlo.com.countryfair.helper;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.util.Log;

/**
 * Created by Android Developer on 05/08/17.
 */

public class DbManager {

    private static final String TAG = DbManager.class.getSimpleName();

    public static boolean hasExistingData(Context context, String dataType){

/*
        SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(context);
        if(sharedPref.getBoolean(dataType,false)){
            Log.d(TAG, "hasExistingData: "+dataType+" "+true);
            return true;
        }
        else{
            SharedPreferences.Editor editor = sharedPref.edit();
            editor.putBoolean(dataType, true);
            editor.apply();
            Log.d(TAG, "hasExistingData: "+dataType+" "+false);
            return false;
        }

        if(dataType.equals("cards")){
            // check existing cards data
        }
        else if(dataType.equals("games")){
            // check existing games data
        }
        else if(dataType.equals("brands")){
            // check existing brands data
        }
        return true;
*/
        return true;
    }

    public static void clearExistingDataFlags(Context context){
        SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(context);
        SharedPreferences.Editor editor = sharedPref.edit();
        editor.putBoolean("cards", false);
        editor.putBoolean("games", false);
        editor.putBoolean("brands", false);
        editor.apply();
    }
}
