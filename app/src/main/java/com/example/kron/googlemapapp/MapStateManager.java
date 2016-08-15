package com.example.kron.googlemapapp;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.res.Resources;

import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;

/**
 * Created by Kron on 4/7/2016.
 */
@SuppressWarnings("unused")
public class MapStateManager {
    private static final String
            LONGITUDE = "longitude",
            LATITUDE = "latitude",
            ZOOM = "zoom",
            BEARING = "bearing",
            TILT = "tilt",
            MAPTYPE = "MAPTYPE",
            PREFS_NAME = "mapCameraState",
            MARKERLONG = "marker longitude",
            MARKERLAT = "marker latitude",
            MARKERTITLE = "marker title",
            MARKERIN = "marker index";

    private SharedPreferences mapStatePrefs;

    public MapStateManager(Context context){
        mapStatePrefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
    }

    /**
     * This function is being called from the MainActivity class when either the user
     * or the system are closing the map object. This function will go through every facet
     * of the map object and save them so that they can be painlessly restored when the
     * user calls the function again.
     */
    public void saveMapState(GoogleMap map, MarkerOptions[] marker){
        SharedPreferences.Editor editor = mapStatePrefs.edit();
        CameraPosition position = map.getCameraPosition();

        editor.putFloat(LATITUDE, (float) position.target.latitude);
        editor.putFloat(LONGITUDE, (float) position.target.longitude);
        editor.putFloat(ZOOM, position.zoom);
        editor.putFloat(BEARING, position.bearing);
        editor.putFloat(TILT, position.tilt);
        editor.putInt(MAPTYPE, map.getMapType());
        for(int i = 0; i < marker.length; i++){
            editor.putFloat(MARKERLAT+i, (float) marker[i].getPosition().latitude);
            editor.putFloat(MARKERLONG+i, (float) marker[i].getPosition().longitude);
            editor.putString(MARKERTITLE+i, marker[i].getTitle());
        }
        editor.putInt(MARKERIN,marker.length);
        editor.commit();
    }

    /**
     * Here we have the opposite of the function above. This function is called on the
     * creation of the map and it checks the saved preferences in order to get the values
     * saved there. If there is nothing (which we check after fetching the latitude value)
     * then we return null. Otherwise we create a camera position object and return that
     * using the lat, long, bearing, zoom, and tilt of the previous camera position.
     */
    public CameraPosition getSavedCameraPosition(){
        double latitude = mapStatePrefs.getFloat(LATITUDE, 0);
        if(latitude == 0){
            return null;
        }
        double longitude = mapStatePrefs.getFloat(LONGITUDE, 0);
        LatLng target = new LatLng(latitude, longitude);

        float zoom = mapStatePrefs.getFloat(ZOOM, 0);
        float bearing = mapStatePrefs.getFloat(BEARING, 0);
        float tilt = mapStatePrefs.getFloat(TILT, 0);

        CameraPosition position = new CameraPosition(target, zoom, tilt, bearing);
        return position;
    }

    /**
     * getSavedMarkers() is called on onResume() in order to fish out any number of saved markers that
     * the map had prior to being closed by the user or system. This version is fetching the details
     * of the markers such as longitude and latitude and saving them to a markeroptions() object which will
     * go into the array to be returned.
     */
    public MarkerOptions[] getSavedMarkers(){
        MarkerOptions[] marker = new MarkerOptions[mapStatePrefs.getInt(MARKERIN,1)];
        for(int i = 0; i < mapStatePrefs.getInt(MARKERIN,0); i++){
            LatLng tmp = new LatLng(mapStatePrefs.getFloat(MARKERLAT+i,0),mapStatePrefs.getFloat(MARKERLONG+i,0));
            marker[i] = new MarkerOptions()
                    .position(tmp)
                    .title(mapStatePrefs.getString(MARKERTITLE+i,""));
        }
        return marker;
    }

    /**
     * Simply enough, this is what we will call when we are done with our current map. Say if the
     * user wants to load a new area's accident markers. This way when they build the new map it
     * wont reset their camera position to the old location.
     */
    public void clearSavedData(){
        SharedPreferences.Editor editor = mapStatePrefs.edit();
        editor.clear();
        editor.commit();
    }
}
