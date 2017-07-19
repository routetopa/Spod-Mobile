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

public class AddressSolver extends AsyncTask<Location, Void, String> {
    private Geocoder geocoder;
    private TextView targetView;

    public AddressSolver(TextView targetView, Context context) {
        geocoder  = new Geocoder(context, Locale.getDefault());
        this.targetView = targetView;
    }

    @Override
    protected String doInBackground(Location... params)
    {
        Location pos=params[0];
        double latitude = pos.getLatitude();
        double longitude = pos.getLongitude();

        List<Address> addresses = null;
        try
        {
            addresses = geocoder.getFromLocation(latitude, longitude, 1);
        }
        catch (IOException e)
        {

        }
        if (addresses!=null)
        {
            if (addresses.isEmpty())
            {
                return null;
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
        return null;
    }

    @Override
    protected void onPostExecute(String result)
    {
        if (result!=null)
            targetView.setText(result);
        else
            targetView.setText("No address to show");

    }
}
