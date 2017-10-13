package dev.countryfair.player.playlazlo.com.countryfair.helper;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

import dev.countryfair.player.playlazlo.com.countryfair.R;


/**
 * Created by Mycom on 9/21/16.
 */

public class ActiveActivitiesTracker {
    private static int sActiveActivities = 0;

    public static void activityStarted(Context context)
    {
        if( sActiveActivities == 0 )
        {
            SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(context.getApplicationContext());
            SharedPreferences.Editor editor = sharedPref.edit();
            editor.putBoolean(context.getString(R.string.pause_state), false);
            editor.commit();
            // TODO: Here is presumably "application level" resume
        }
        sActiveActivities++;
    }

    public static void activityStopped(Context context)
    {
        sActiveActivities--;
        if( sActiveActivities == 0 )
        {

            SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(context.getApplicationContext());
            SharedPreferences.Editor editor = sharedPref.edit();
            editor.putBoolean(context.getString(R.string.pause_state), true);
            editor.commit();
            // TODO: Here is presumably "application level" pause
        }
    }
}
