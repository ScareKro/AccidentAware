package com.example.kron.googlemapapp;

import android.Manifest;
import android.annotation.TargetApi;
import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.DataSetObserver;
import android.net.DhcpInfo;
import android.net.Uri;
import android.net.wifi.WifiManager;
import android.os.Build;
import android.os.Handler;
import android.os.Message;
import android.provider.ContactsContract;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.FragmentActivity;
import android.os.Bundle;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.Spinner;
import android.widget.SpinnerAdapter;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.appindexing.Action;
import com.google.android.gms.appindexing.AppIndex;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GoogleApiAvailability;
import com.google.android.gms.common.GooglePlayServicesUtil;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapView;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

import java.io.IOException;
import java.io.ObjectOutputStream;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Random;
import java.util.Scanner;
import java.util.Set;
// extends FragmentActivity implements OnMapReadyCallback
public class MainActivity extends Activity{

    //    private boolean map = false;
//    private static final int ERRORDIALOG = 9001;
//    private GoogleMap mMap;
//    private MapView mMapView;
//    private CameraUpdate update = null;
//    private MarkerOptions[] marker;
    private Spinner radiusSpinner;
    private Intent myIntent = null;
    private final static String MESSAGE1 = "com.example.kron.googlemapapp.county",
            MESSAGE2 = "com.example.kron.googlemapapp.state";
    //private ArrayAdapter<String> stateSpinnerAdapter;
    Thread m_objThreadClient;
    int port = 3447;
    Socket clientSocket;
    WifiManager wifi;
    private GoogleMap mMap;
    private MapView mMapView;
    /**
     * ATTENTION: This was auto-generated to implement the App Indexing API.
     * See https://g.co/AppIndexing/AndroidStudio for more information.
     */
    private GoogleApiClient client;

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
//        final DatabaseManager DB = new DatabaseManager(this);
//        createAlphaDB();

        wifi = (WifiManager) this.getSystemService(Context.WIFI_SERVICE);

        //Create the spinner the user can use to determine the radius around their location the accidents will be loaded.
        radiusSpinner = (Spinner)findViewById(R.id.DistSpinner);
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
        // ATTENTION: This was auto-generated to implement the App Indexing API.
        // See https://g.co/AppIndexing/AndroidStudio for more information.
        client = new GoogleApiClient.Builder(this).addApi(AppIndex.API).build();

        //Wipe anything saved from before.
        DatabaseManager DB = new DatabaseManager(this);
        DB.clearPrefs();

        TextView bottomText = (TextView) findViewById(R.id.SafeTips);
        String[] textOptions = getResources().getStringArray(R.array.safeList);
        Random r = new Random();
        int i1 = r.nextInt(10 - 0) + 1;
        bottomText.setText(textOptions[i1-1]);


//        //Build the example map
//        MapStats tempMS = new MapStats();
//        int services = tempMS.servicesOK(this);
//        if(services == 1){
//            mMapView = (MapView) findViewById(R.id.map);
//            mMapView.onCreate(savedInstanceState);
//            mMapView.getMapAsync(this);
//        }
//        else{
//            if(services == 2) {
//                Toast.makeText(this, "Can't connect to Google Play Services.. sucks.", Toast.LENGTH_SHORT).show();
//            }
//            Toast.makeText(this, "Cannot construct map.", Toast.LENGTH_SHORT).show();
//        }
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

    private InetAddress getBroadcastAddress() throws IOException {
        DhcpInfo dhcp = wifi.getDhcpInfo();

        int broadcast = (dhcp.ipAddress & dhcp.netmask) | ~dhcp.netmask;
        byte[] quads = new byte[4];
        for (int k = 0; k < 4; k++)
            quads[k] = (byte) ((broadcast >> k * 8) & 0xFF);
        return InetAddress.getByAddress(quads);
    }

    @Override
    public void onStart() {
        super.onStart();

        // ATTENTION: This was auto-generated to implement the App Indexing API.
        // See https://g.co/AppIndexing/AndroidStudio for more information.
        client.connect();
        Action viewAction = Action.newAction(
                Action.TYPE_VIEW, // TODO: choose an action type.
                "Main Page", // TODO: Define a title for the content shown.
                // TODO: If you have web page content that matches this app activity's content,
                // make sure this auto-generated web page URL is correct.
                // Otherwise, set the URL to null.
                Uri.parse("http://host/path"),
                // TODO: Make sure this auto-generated app deep link URI is correct.
                Uri.parse("android-app://com.example.kron.googlemapapp/http/host/path")
        );
        AppIndex.AppIndexApi.start(client, viewAction);
    }

    @Override
    public void onStop() {
        super.onStop();

        // ATTENTION: This was auto-generated to implement the App Indexing API.
        // See https://g.co/AppIndexing/AndroidStudio for more information.
        Action viewAction = Action.newAction(
                Action.TYPE_VIEW, // TODO: choose an action type.
                "Main Page", // TODO: Define a title for the content shown.
                // TODO: If you have web page content that matches this app activity's content,
                // make sure this auto-generated web page URL is correct.
                // Otherwise, set the URL to null.
                Uri.parse("http://host/path"),
                // TODO: Make sure this auto-generated app deep link URI is correct.
                Uri.parse("android-app://com.example.kron.googlemapapp/http/host/path")
        );
        AppIndex.AppIndexApi.end(client, viewAction);
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
//    @Override
//    public void onMapReady(GoogleMap googleMap) {
//        mMap = googleMap;
//        mMap.setMapType(GoogleMap.MAP_TYPE_TERRAIN);
//
//        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
//            Toast.makeText(this,"Need proper permissions",Toast.LENGTH_SHORT).show();
//            return;
//        }
//        mMap.setMyLocationEnabled(true);
//    }
}

