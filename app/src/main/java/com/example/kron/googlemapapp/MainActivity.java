package com.example.kron.googlemapapp;

import android.Manifest;
import android.annotation.TargetApi;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.net.DhcpInfo;
import android.net.wifi.WifiManager;
import android.os.Build;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.FragmentActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.ActivityRecognition;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapFragment;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.common.api.GoogleApiClient.ConnectionCallbacks;
import com.google.android.gms.common.api.GoogleApiClient.OnConnectionFailedListener;
import com.google.android.gms.maps.model.Circle;
import com.google.android.gms.maps.model.CircleOptions;
import com.google.android.gms.maps.model.LatLng;

import java.io.IOException;
import java.net.InetAddress;
import java.net.Socket;
import java.util.Random;

// extends FragmentActivity implements OnMapReadyCallback
public class MainActivity extends FragmentActivity implements
        ConnectionCallbacks, OnConnectionFailedListener, OnMapReadyCallback {
    //OnMapReadyCallback, ConnectionCallbacks, OnConnectionFailedListener, LocationListener {

    private Spinner radiusSpinner;
    private Intent myIntent = null;
    private final static String MESSAGE1 = "com.example.kron.googlemapapp.county",
            MESSAGE2 = "com.example.kron.googlemapapp.state";
    Thread m_objThreadClient;
    int port = 3447;
    Socket clientSocket;
    public static final String TAG = "KRON_";
    protected LocationManager locationManager;
    protected Location lastLocation;
    Circle myRadius;
//    WifiManager wifi;
    /**
     * ATTENTION: This was auto-generated to implement the App Indexing API.
     * See https://g.co/AppIndexing/AndroidStudio for more information.
     */
    private GoogleApiClient client;
    protected GoogleMap myMap;

    /**
     * onCreate, this is the first method that will be run when our app is installed. It defines what
     * the start page of the app will be and then creates the first drop down menu on the start
     * screen. The final thing we do is define what method is called when an item on the first drop
     * down is selected.
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.start_page); //Load our front page.

//        wifi = (WifiManager) this.getSystemService(Context.WIFI_SERVICE);

        //Create the spinner the user can use to determine the radius around their location the accidents will be loaded.
        radiusSpinner = (Spinner) findViewById(R.id.DistSpinner);
        ArrayAdapter<String> radiusSpinnerAdapter = new ArrayAdapter<String>(this,
                android.R.layout.simple_spinner_dropdown_item,
                getResources().getStringArray(R.array.radiiList));
        radiusSpinnerAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        radiusSpinner.setAdapter(radiusSpinnerAdapter);
        radiusSpinner.setOnTouchListener(new View.OnTouchListener() {
            @TargetApi(Build.VERSION_CODES.LOLLIPOP)    //Wont work on older API's
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                switch (event.getAction()) {
                    case MotionEvent.ACTION_DOWN: {     //when touched.
                        radiusSpinner.setBackground(getDrawable(R.drawable.dropdownsmallclicked));
                        break;
                    }
                    case MotionEvent.ACTION_UP: {       //when released
                        radiusSpinner.setBackground(getDrawable(R.drawable.dropdownsmall));
                        break;
                    }
                }
                return false;                           //For some reason if this is not false the dropdown will stop working entirely.
            }
        });
        radiusSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                handlePermissionsAndGetLocation();
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent){}
        });
        // ATTENTION: This was auto-generated to implement the App Indexing API.
        // See https://g.co/AppIndexing/AndroidStudio for more information.

        //Wipe anything saved from before.
        DatabaseManager DB = new DatabaseManager(this);
        DB.clearPrefs();

        // Create an instance of GoogleAPIClient.
        if (client == null) {
            client = new GoogleApiClient.Builder(this)
                    .addConnectionCallbacks(this)
                    .addOnConnectionFailedListener(this)
                    .addApi(LocationServices.API)
                    .addApi(ActivityRecognition.API)
                    .build();
        }

        /**
         * bottomText basically refers to the helpful little driving tips that will be randomly
         * chosen each time the front page of the app is opened. It is not super important to the functionality
         * of the application, but it is a fun little thing to add a level of polish to the app.
         */
        TextView bottomText = (TextView) findViewById(R.id.SafeTips);
        String[] textOptions = getResources().getStringArray(R.array.safeList);
        Random r = new Random();
        int i1 = r.nextInt(10 - 0) + 1;
        bottomText.setText(textOptions[i1 - 1]);


//        locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
//        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
//            return;
//        }
//        locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 0, this);
        /**
         * Build the little map in the center of the screen.
         */
        MapFragment mapFragment = (MapFragment) getFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

        /**
         * Going to get location information so the map can be adjusted.
         */
        handlePermissionsAndGetLocation();

    }

    /**
     * We are jumping ship here. The create map function was called so we are bundling up the
     * countySelected and stateSelected values and shipping off to the DisplayMap class as our new
     * main.
     */
    public void onBuildMapButton() {
        myIntent = new Intent(this, DisplayMap.class);

        //replace the 3 with the radius selected.
        myIntent.putExtra(MESSAGE1, 3);
        //myIntent.putExtra(MESSAGE, DB.getAccidents(countySelected).toArray(new String[DB.getAccidents(countySelected).size()]));
        startActivity(myIntent);
    }

    /**
     * Necessary for internet functionality.
     */
//    private InetAddress getBroadcastAddress() throws IOException {
//        DhcpInfo dhcp = wifi.getDhcpInfo();
//        int broadcast = (dhcp.ipAddress & dhcp.netmask) | ~dhcp.netmask;
//        byte[] quads = new byte[4];
//        for (int k = 0; k < 4; k++)
//            quads[k] = (byte) ((broadcast >> k * 8) & 0xFF);
//        return InetAddress.getByAddress(quads);
//    }

    /**
     * When the application is opened from being minimized rather than shut down.
     */
    @Override
    public void onStart() {
        super.onStart();
        // ATTENTION: This was auto-generated to implement the App Indexing API.
        // See https://g.co/AppIndexing/AndroidStudio for more information.
        client.connect();
//        Action viewAction = Action.newAction(
//                Action.TYPE_VIEW, // TODO: choose an action type.
//                "Main Page", // TODO: Define a title for the content shown.
//                // TODO: If you have web page content that matches this app activity's content,
//                // make sure this auto-generated web page URL is correct.
//                // Otherwise, set the URL to null.
//                Uri.parse("http://host/path"),
//                // TODO: Make sure this auto-generated app deep link URI is correct.
//                Uri.parse("android-app://com.example.kron.googlemapapp/http/host/path")
//        );
//        AppIndex.AppIndexApi.start(client, viewAction);
    }

    /**
     * When the app is minimized.
     */
    @Override
    public void onStop() {
        super.onStop();
        // ATTENTION: This was auto-generated to implement the App Indexing API.
        // See https://g.co/AppIndexing/AndroidStudio for more information.
//        Action viewAction = Action.newAction(
//                Action.TYPE_VIEW, // TODO: choose an action type.
//                "Main Page", // TODO: Define a title for the content shown.
//                // TODO: If you have web page content that matches this app activity's content,
//                // make sure this auto-generated web page URL is correct.
//                // Otherwise, set the URL to null.
//                Uri.parse("http://host/path"),
//                // TODO: Make sure this auto-generated app deep link URI is correct.
//                Uri.parse("android-app://com.example.kron.googlemapapp/http/host/path")
//        );
//        AppIndex.AppIndexApi.end(client, viewAction);
        client.disconnect();
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
        myMap = googleMap;
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            return;
        }
        myMap.setMyLocationEnabled(true);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        switch (requestCode) {
            case 123:
                if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    //Accepted
                    getLocation();
                } else {
                    // Denied
                    Toast.makeText(MainActivity.this, "LOCATION Denied", Toast.LENGTH_SHORT)
                            .show();
                }
                break;
            default:
                super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        }
    }


    @TargetApi(Build.VERSION_CODES.M)
    private void handlePermissionsAndGetLocation() {
        Log.v(TAG, "handlePermissionsAndGetLocation");
        int hasWriteContactsPermission = ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION);
        if (hasWriteContactsPermission != PackageManager.PERMISSION_GRANTED) {
            requestPermissions(new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                    123);
            return;
        }
        getLocation();//if already has permission
    }

    @TargetApi(Build.VERSION_CODES.M)
    protected void getLocation() {
        Log.v(TAG, "GetLocation");
        int LOCATION_REFRESH_TIME = 300;
        int LOCATION_REFRESH_DISTANCE = 2;

        if (!(ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED)) {
            Log.v(TAG, "Has permission");
            locationManager = (LocationManager) getSystemService(LOCATION_SERVICE);
            locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, LOCATION_REFRESH_TIME,
                    LOCATION_REFRESH_DISTANCE, locationListener);
        } else {
            Log.v(TAG, "Does not have permission");
        }

    }

    /**
     * The location listener as the name may suggest listens to the location data sent by the Google Maps
     * API. We wait for the location to be changed by an amount set by the getLocation() method then
     * update the map's camera position and the position of the circle.
     */
    private final LocationListener locationListener = new LocationListener() {
        @Override
        public void onLocationChanged(Location location) {
            Log.v(TAG, "Location Change");
//            myRadius.remove();
            LatLng ll = new LatLng(lastLocation.getLatitude(), lastLocation.getLongitude());
            myMap.moveCamera(CameraUpdateFactory.newLatLngZoom(ll, 16));
            myRadius = drawCircle(ll);
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
    };

    /**
     * This is called automatically when the maps functionality is set up (well when connected, but neither here nor there).
     * We are using it to move the map's focus to the user's current position, zoom in on them, and build a circle
     * around them with a radius set by the drop-down menu.
     *
     * Due to the way it is currently set up, this method is also called (in a fashion) after the user selects a
     * new radius on the drop-down menu to get more immediate feedback.
     */
    @Override
    public void onConnected(Bundle bundle) {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            return;
        }
        lastLocation = LocationServices.FusedLocationApi.getLastLocation(client);
        if (lastLocation != null) {
            Log.v(TAG, "Location Set");
            LatLng ll = new LatLng(lastLocation.getLatitude(), lastLocation.getLongitude());
            myMap.moveCamera(CameraUpdateFactory.newLatLngZoom(ll, 16));
            myRadius = drawCircle(ll);
        }
    }

    /**
     * Method draws a circle around the user's current location.
     * TODO: set the radius of the circle equal to the range that pins will be fetched.
     * @param ll - LatLng of the current location.
     * @return returns the circle object.
     */
    private Circle drawCircle(LatLng ll){
        String tmpText = radiusSpinner.getSelectedItem().toString();
        Float selectedRadius = Float.parseFloat(tmpText.substring(0,3))*1000;
        CircleOptions options = new CircleOptions()
                .center(ll)
                .radius(selectedRadius)
                .fillColor(0x335e5e5e)
                .strokeColor(Color.GRAY)
                .strokeWidth(10);
        return myMap.addCircle(options);
    }

    @Override
    public void onConnectionSuspended(int i) {

    }
    @Override
    public void onConnectionFailed(ConnectionResult connectionResult) {

    }
}

