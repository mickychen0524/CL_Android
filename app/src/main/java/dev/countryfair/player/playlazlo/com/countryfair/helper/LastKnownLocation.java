package dev.countryfair.player.playlazlo.com.countryfair.helper;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationManager;
import android.support.v4.content.ContextCompat;

/**
 * Created by Berhan on 11.10.2017.
 */

public class LastKnownLocation {
    public static Location getLastKnownLocation(Context context){
        if (context!=null && ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION)== PackageManager.PERMISSION_GRANTED){
            LocationManager lm = (LocationManager)context.getSystemService(Context.LOCATION_SERVICE);
            Location locationGps = null;
            Location locationNetwork = null;
            if (lm.isProviderEnabled(LocationManager.GPS_PROVIDER)){
                locationGps = lm.getLastKnownLocation(LocationManager.GPS_PROVIDER);
            }
            if (lm.isProviderEnabled(LocationManager.NETWORK_PROVIDER)){
                locationNetwork = lm.getLastKnownLocation(LocationManager.NETWORK_PROVIDER);
            }
            if (locationGps==null)
                return locationNetwork;
            if (locationNetwork==null)
                return locationGps;
            if (locationNetwork.getTime() - locationGps.getTime()>2*60*1000)
                return locationNetwork;
            else
                return locationGps;
        }
        return null;
    }
}
