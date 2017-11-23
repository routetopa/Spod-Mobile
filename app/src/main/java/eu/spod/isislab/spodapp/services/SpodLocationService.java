package eu.spod.isislab.spodapp.services;

import android.app.IntentService;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.*;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Looper;
import android.support.v4.content.ContextCompat;
import android.util.Log;
import android.widget.TextView;

import java.io.IOException;
import java.util.List;
import java.util.Locale;

public class SpodLocationService extends IntentService implements LocationListener
{
    private static Location location        = null;
    private LocationManager locationManager = null;
    private static final float MIN_DIST     = 20;
    private static final long MIN_PERIOD    = 3000;
    private static boolean gps_enabled;
    private static boolean network_enabled;

    public SpodLocationService() {
        super("SpodMobileLocationServices");
    }


    @Override
    protected void onHandleIntent(Intent intent)
    {
        if(ContextCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) ==  PackageManager.PERMISSION_GRANTED)
        {
            locationManager = (LocationManager) getSystemService(LOCATION_SERVICE);

            Location net_loc = null, gps_loc = null;
            gps_enabled     = locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER);
            network_enabled = locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER);

            if (gps_enabled)
                gps_loc = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
            if (network_enabled)
                net_loc = locationManager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER);

            if (gps_loc != null && net_loc != null) {
                //smaller the number more accurate result will
                if (gps_loc.getAccuracy() > net_loc.getAccuracy())
                    location = net_loc;
                else
                    location = gps_loc;
            } else {
                if (gps_loc != null) {
                    location = gps_loc;
                } else if (net_loc != null) {
                    location = net_loc;
                }
            }

            HandlerThread t = new HandlerThread("LocationServiceThread");
            t.start();
            if (locationManager.getAllProviders().contains(LocationManager.GPS_PROVIDER))
               locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, MIN_PERIOD, MIN_DIST, this, t.getLooper());
            if (locationManager.getAllProviders().contains(LocationManager.NETWORK_PROVIDER))
               locationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, MIN_PERIOD, MIN_DIST, this, t.getLooper());
        }

    }

    @Override
    public void onLocationChanged(Location location) {
        this.location = location;
        Log.e("LOCATION SERVICE", "Lat: " + location.getLatitude() + " Long: " + location.getLongitude());
    }

    @Override
    public void onStatusChanged(String provider, int status, Bundle extras) {

    }

    @Override
    public void onProviderEnabled(String provider) {

    }

    @Override
    public void onProviderDisabled(String provider) {

    }

    public static  Location getCurrentLocation(){
        return location;
    }

    public static boolean isAvailable(){

        return (gps_enabled || network_enabled);

    }
}