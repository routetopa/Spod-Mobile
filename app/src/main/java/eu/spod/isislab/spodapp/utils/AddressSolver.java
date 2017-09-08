package eu.spod.isislab.spodapp.utils;

import android.content.Context;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.os.AsyncTask;
import android.widget.TextView;

import java.io.IOException;
import java.util.List;
import java.util.Locale;

import eu.spod.isislab.spodapp.services.SpodLocationServices;

public class AddressSolver extends AsyncTask<Location, Void, String> {
    private Geocoder geocoder;
    private TextView targetView;
    private Location currentLocation;

    public AddressSolver(TextView targetView, Context context) {
        geocoder  = new Geocoder(context, Locale.getDefault());
        this.targetView = targetView;
    }

    @Override
    protected String doInBackground(Location... params)
    {
        Location pos=params[0];
        currentLocation = pos;
        double latitude = pos.getLatitude();
        double longitude = pos.getLongitude();

        List<Address> addresses = null;
        try
        {
            addresses = geocoder.getFromLocation(latitude, longitude, 5);
        }
        catch (IOException e)
        {
            e.printStackTrace();
        }
        if (addresses!=null)
        {
            if (addresses.isEmpty())
            {
                return SpodLocationServices.getCurrentLocation().getLatitude() + "," + SpodLocationServices.getCurrentLocation().getLongitude();
            }
            else {
                if (addresses.size() > 0)
                {
                    StringBuffer address=new StringBuffer();
                    Address tmp=addresses.get(0);
                    for (int y=0;y<tmp.getMaxAddressLineIndex();y++)
                        address.append(tmp.getAddressLine(y)+"\n");
                    return address.toString();
                }
            }
        }
        return SpodLocationServices.getCurrentLocation().getLatitude() + "," + SpodLocationServices.getCurrentLocation().getLongitude();
    }

    @Override
    protected void onPostExecute(String result)
    {
        if (result!=null)
            targetView.setText(result);
        else
            targetView.setText(currentLocation.getLatitude() + "," + currentLocation.getLongitude());

    }
}
