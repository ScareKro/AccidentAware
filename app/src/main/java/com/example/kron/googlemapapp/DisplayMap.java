package com.example.kron.googlemapapp;

import android.Manifest;
import android.app.Dialog;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.FragmentActivity;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GoogleApiAvailability;
import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapView;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Objects;
import java.util.Set;

/**
 * Created by Kron on 4/8/2016.
 */
public class DisplayMap extends FragmentActivity implements OnMapReadyCallback {
    private static final int ERRORDIALOG = 9001;
    private GoogleMap mMap;
    private MapView mMapView;
    private CameraUpdate update = null;
    private MarkerOptions[] marker;
    private String county, state;
    private final static String MESSAGE1 = "com.example.kron.googlemapapp.county",
            MESSAGE2 = "com.example.kron.googlemapapp.state";

    @Override
    protected void onCreate(Bundle SavedInstanceState) {
        super.onCreate(SavedInstanceState);
        setContentView(R.layout.mapview_trial);
        Intent myIntent = getIntent();
        county = myIntent.getStringExtra(MESSAGE1);
        state = myIntent.getStringExtra(MESSAGE2);

        if (servicesOK()) {
            mMapView = (MapView) findViewById(R.id.map);
            mMapView.onCreate(SavedInstanceState);
            mMapView.getMapAsync(this);
        }
        else{
            Toast.makeText(this, "Cannot construct map.", Toast.LENGTH_SHORT).show();
        }
    }


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
    public boolean servicesOK(){
//        int isAvailable = GooglePlayServicesUtil.isGooglePlayServicesAvailable(this):
//        The previous line is depreciated
        GoogleApiAvailability googleAPI = GoogleApiAvailability.getInstance();
        int isAvailable = googleAPI.isGooglePlayServicesAvailable(this);
//        The previous two lines make up the fix for that

        if(isAvailable == ConnectionResult.SUCCESS)
            return true;
        else if(googleAPI.isUserResolvableError(isAvailable)){
            Dialog dialog = googleAPI.getErrorDialog(this,isAvailable,ERRORDIALOG);
            dialog.show();
        }
        else{
            Toast.makeText(this,"Can't connect to Google Play Services.. sucks.",Toast.LENGTH_SHORT).show();
        }
        return false;
    }

    /**
     * onDestroy(), onLowMemory(), onPause(), onResume(), and onSaveInstanceState(Bundle outState).
     * These are from the MainActivity parent class FragmentActivity. They are mostly unchanged
     * however they are slightly modified in order to also include a case for the map view. These
     * changes are necessary in order to make map view work. Without it they are completely
     * redundant however. Remember map view is superior to fragment so now that they are fully
     * implemented there is no real need to switch back.. at least that I know of now.
     */
    @Override
     protected void onDestroy() {
        super.onDestroy();
        mMapView.onDestroy();
    }
    @Override
    public void onLowMemory() {
        super.onLowMemory();
        mMapView.onLowMemory();
    }
    @Override
    protected void onPause() {
        super.onPause();
        mMapView.onPause();
    }
    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        mMapView.onSaveInstanceState(outState);
    }

    /**
     * onResume() is a little special. It is changed just like the others to ensure that the mapview
     * works perfectly. However it is also edited in order to interract with the MapStateManager
     * which allows us to load our previous position from the mgr into our update value, allowing
     * us to hop the camera right back to where the user left it so they aren't inconvenienced.
     */
    @Override
    protected void onResume() {
        super.onResume();
        mMapView.onResume();
        MapStateManager mgr = new MapStateManager(this);
        CameraPosition position = mgr.getSavedCameraPosition();
        if (position != null) {
            update = CameraUpdateFactory.newCameraPosition(position);
        }
        MarkerOptions[] tmpMO = mgr.getSavedMarkers();
        if (tmpMO == null)
            Toast.makeText(this, "Error constructing marker.", Toast.LENGTH_SHORT).show();
        else {
            marker = tmpMO;
        }
    }

    /**
     * Manipulates the map once available.
     * This callback is triggered when the map is ready to be used.
     * This is where we can add markers or lines, add listeners or move the camera.
     * If Google Play services is not installed on the device, the user will be prompted to install
     * it inside the SupportMapFragment. This method will only be triggered once the user has
     * installed Google Play services and returned to the app.
     */
    @Override
    public void onMapReady(GoogleMap googleMap) {
        DatabaseManager DB = new DatabaseManager(this);
        Set<String> markerList = DB.getAccidents(county, state);
        if(markerList == null){
            Toast.makeText(this,"You done goofed, still not working.",Toast.LENGTH_SHORT).show();
        }
//        markerList = new HashSet<String>(Arrays.asList("44.997158,-92.762050",
//                        "44.992241,-92.760119",
//                        "44.991907,-92.763338"));

        mMap = googleMap;
        mMap.setMapType(GoogleMap.MAP_TYPE_TERRAIN);

        double avgLat = 0, avgLong = 0;

        if (update == null) {
            marker = new MarkerOptions[markerList.size()];
            int i = 0;
            for(String s : markerList){
                String[] tmpArray = s.split(",");
                if(tmpArray[0]!=""|| tmpArray[1]!="") {
                    double tmpLat = Double.parseDouble(tmpArray[0]),
                            tmpLong = Double.parseDouble(tmpArray[1]);
                    avgLat += tmpLat;
                    avgLong += tmpLong;
                    LatLng tmpLL = new LatLng(tmpLat, tmpLong);
                    marker[i] = new MarkerOptions().position(tmpLL).title("Accident #" + i);
                    mMap.addMarker(marker[i]);
                    i++;
                } else {
                    Toast.makeText(this,"Improper coordinate.. ignoring.",Toast.LENGTH_SHORT).show();
                }
            }
            LatLng startCamera = new LatLng(avgLat/marker.length, avgLong/marker.length);
            mMap.moveCamera(CameraUpdateFactory.newLatLng(startCamera));
            mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(startCamera, 14));
        }
        else{
            mMap.moveCamera(update);
            for(int i = 0; i < marker.length; i++){
                mMap.addMarker(marker[i]);
            }
        }
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            Toast.makeText(this,"Need proper permissions",Toast.LENGTH_SHORT).show();
            return;
        }
        mMap.setMyLocationEnabled(true);
    }

    /**
     * Here we are overriding what the program does when it is shut down either by the user
     * or system. What happens is that we will still do the super.onStop() like normal.
     * However we will then create a new oject of the MapStateManager class which I created.
     * This will allow us to pass in the context 'this' as well as the mMap map which will save
     * every facet of the current map into system memory so that when it is restored by the user
     * it will just pick up where they left off rather than resetting.
     */
    @Override
    protected void onStop() {
        super.onStop();
        MapStateManager mgr = new MapStateManager(this);
        mgr.saveMapState(mMap, marker);
    }
}
