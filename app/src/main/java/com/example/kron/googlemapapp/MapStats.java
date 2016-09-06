package com.example.kron.googlemapapp;

import android.app.Dialog;
import android.content.Context;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GoogleApiAvailability;

/**
 * Created by Kron on 9/6/2016.
 */
public class MapStats {
    /**
     * Creates a boolean which tracks whether or not the application can connect to the Google
     * Play Store, this is essential in order to use the Google Maps API on the device in
     * question. If the function can easily find it I return true, otherwise I check to see if
     * the error we found is something easily repairable (this is handled by the Google API so
     * I do not need to worry too much about it), and if it is I send them an error message
     * indicating what the problem they encountered is (e.g. missing update).
     * If, however, we do not know what the problem is, I simply print "Can't connect...."
     * and then return false, indicating that we cannot go through with creating the map object.
     */
    public int servicesOK(Context con){
//        int isAvailable = GooglePlayServicesUtil.isGooglePlayServicesAvailable(this):
//        The previous line is depreciated
        GoogleApiAvailability googleAPI = GoogleApiAvailability.getInstance();
        int isAvailable = googleAPI.isGooglePlayServicesAvailable(con);
//        The previous two lines make up the fix for that

        if(isAvailable == ConnectionResult.SUCCESS)
            return 1;
        else if(googleAPI.isUserResolvableError(isAvailable)){
            return 2;
        }
        else{
            return 0;
        }
    }
}
