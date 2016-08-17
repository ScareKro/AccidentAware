package com.example.kron.googlemapapp;

import android.annotation.TargetApi;
import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.database.DataSetObserver;
import android.net.DhcpInfo;
import android.net.Uri;
import android.net.wifi.WifiManager;
import android.os.Build;
import android.os.Handler;
import android.os.Message;
import android.provider.ContactsContract;
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
import java.util.Scanner;
import java.util.Set;

public class MainActivity extends Activity {

    //    private boolean map = false;
//    private static final int ERRORDIALOG = 9001;
//    private GoogleMap mMap;
//    private MapView mMapView;
//    private CameraUpdate update = null;
//    private MarkerOptions[] marker;
    private Spinner stateSpinner, countySpinner, distSpinner;
    private String countySelected, stateSelected;
    private Intent myIntent = null;
    private final static String MESSAGE1 = "com.example.kron.googlemapapp.county",
            MESSAGE2 = "com.example.kron.googlemapapp.state";
    //private ArrayAdapter<String> stateSpinnerAdapter;
    Thread m_objThreadClient;
    int port = 3447;
    Socket clientSocket;
    WifiManager wifi;
    /**
     * ATTENTION: This was auto-generated to implement the App Indexing API.
     * See https://g.co/AppIndexing/AndroidStudio for more information.
     */
    private GoogleApiClient client;

    /**
     * Everything below until the line is only temporary for the alpha build, it will be replaced by
     * a more dynamic database entry system done from a computer on the same local network.
     */
//    private Set<String>
//            alphaStateDatabase =
//                    new HashSet<String>(Arrays.asList("Wisconsin", "Minnesota", "Oregon", "New York")),
//            alphaWiCountyDatabase =
//                    new HashSet<String>(Arrays.asList("St. Croix", "Polk", "Pierce")),
//            alphaStCroixDatabase =
//                    new HashSet<String>(Arrays.asList("44.997158,-92.762050",
//                            "44.992241,-92.760119",
//                            "44.991907,-92.763338"));

//    public MainActivity() {
//    }

//    private void createAlphaDB(){
//        DatabaseManager DB = new DatabaseManager(this);
//        DB.createStateList(alphaStateDatabase);
//        DB.createCountyList(alphaWiCountyDatabase, "Wisconsin");
//        DB.createAccidentDB(alphaStCroixDatabase, "St. Croix", "Wisconsin");
//    }
    //----------------------------------------------------------------------------------------------

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
        //Set up the drop down menu for the user to select the State.
        stateSpinner = (Spinner) findViewById(R.id.StateSpinner);

        //Set the click animation.
        stateSpinner.setOnTouchListener(new View.OnTouchListener() {
            @TargetApi(Build.VERSION_CODES.LOLLIPOP)    //Wont work on older API's
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                switch (event.getAction()) {
                    case MotionEvent.ACTION_DOWN: {     //when touched.
                        stateSpinner.setBackground(getDrawable(R.drawable.dropdownclicked));
                        break;
                    }
                    case MotionEvent.ACTION_UP: {       //when released
                        stateSpinner.setBackground(getDrawable(R.drawable.dropdown));
                        break;
                    }
                }
                return false;                           //For some reason if this is not false the dropdown will stop working entirely.
            }
        });

        ArrayAdapter<String> stateSpinnerAdapter = new ArrayAdapter<String>(this,
                android.R.layout.simple_spinner_dropdown_item,
                getResources().getStringArray(R.array.stateList)); //Get the list of states from the
        //State.xml document.
        stateSpinnerAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        stateSpinner.setAdapter(stateSpinnerAdapter);
        stateSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                onSelectState(parent.getItemAtPosition(position).toString());
                //This is the method called whenever an item is selected.
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {}
        });
        //-----------------------------------------------------------
        distSpinner = (Spinner)findViewById(R.id.DistSpinner);
        ArrayAdapter<String> distSpinnerAdapter = new ArrayAdapter<String>(this,
                android.R.layout.simple_spinner_dropdown_item,
                getResources().getStringArray(R.array.distList));
        distSpinnerAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        distSpinner.setAdapter(distSpinnerAdapter);
        distSpinner.setOnTouchListener(new View.OnTouchListener() {
            @TargetApi(Build.VERSION_CODES.LOLLIPOP)    //Wont work on older API's
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                switch (event.getAction()) {
                    case MotionEvent.ACTION_DOWN: {     //when touched.
                        distSpinner.setBackground(getDrawable(R.drawable.dropdownsmallclicked));
                        break;
                    }
                    case MotionEvent.ACTION_UP: {       //when released
                        distSpinner.setBackground(getDrawable(R.drawable.dropdownsmall));
                        break;
                    }
                }
                return false;                           //For some reason if this is not false the dropdown will stop working entirely.
            }
        });
        // ATTENTION: This was auto-generated to implement the App Indexing API.
        // See https://g.co/AppIndexing/AndroidStudio for more information.
        client = new GoogleApiClient.Builder(this).addApi(AppIndex.API).build();
    }

    /**
     * onSelectState is called whenever an item is selected from the drop-down menu at the top of the
     * front page. It takes a string argument which is the item that the user clicked on [e.g.
     * "Alabama" or "Wisconsin"]. Then we define the parameters of the second spinner and trim down
     * the state name into the name of the database containing its counties. The final thing again is
     * setting the method to be called when an item is selected from *this* spinner.
     */
    public void onSelectState(final String str) {
        countySpinner = (Spinner) findViewById(R.id.CountySpinner);
        //Set click animation.
        countySpinner.setOnTouchListener(new View.OnTouchListener() {
            @TargetApi(Build.VERSION_CODES.LOLLIPOP)    //Wont work on older API's
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                switch (event.getAction()) {
                    case MotionEvent.ACTION_DOWN: {     //when touched.
                        countySpinner.setBackground(getDrawable(R.drawable.dropdownclicked));
                        break;
                    }
                    case MotionEvent.ACTION_UP: {       //when released
                        countySpinner.setBackground(getDrawable(R.drawable.dropdown));
                        break;
                    }
                }
                return false;                           //For some reason if this is not false the dropdown will stop working entirely.
            }
        });
        //The 2nd spinner on the start page.
        String tmpStr = str.replace(" ", "");
        //This changes the state name into the name of the database containing its counties [e.g.
        //"New York" becomes "NewYork"].
        ArrayAdapter<String> countySpinnerAdapter = new ArrayAdapter<String>(this,
                android.R.layout.simple_spinner_dropdown_item,
                getResources().getStringArray(getResources().getIdentifier(tmpStr, "array", "com.example.kron.googlemapapp")));
        //The list used to populate the spinner is defined dynamically using the getIdentifier()
        //call in the inner parentheses. The tmpStr is the string we created earlier.
        countySpinnerAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        countySpinner.setAdapter(countySpinnerAdapter);
        //Toast.makeText(this,getResources().getIdentifier("Alabama", "array", "com.example.kron.googlemapapp"),Toast.LENGTH_SHORT).show();
        countySpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                onSelectCounty(parent.getItemAtPosition(position).toString(), str);
                //We send the next method the county name and the state name.
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
            }
        });
    }

    /**
     * Called by onSelectState this method takes in the county as well as the state the user selected
     * from the drop down menus on the front page. What we do is make a call to the DatabaseManager
     * object in order to check to see if we have a database for the state/county combination selected.
     * If we do not we put out a Toast and the user is stuck here until they select a valid combination.
     * Otherwise we enable the button for creating the map and define the onBuildMapButton function
     * as the function to call when the button is clicked.
     * <p/>
     * We also enable the update button from here regardless of whether or not we already have values
     * for the county in question. When that button is clicked we create and UpdateDB object with the
     * county and state and call its start function
     */
    public void onSelectCounty(String county, String state) {
        DatabaseManager DB = new DatabaseManager(this);
        Set<String> tmpSet = DB.getAccidents(county, state);
        //List of accidents.
        countySelected = county;
        stateSelected = state;
        //Set global values.
        if (tmpSet != null) {
            Button makeMap;
            makeMap = (Button) findViewById(R.id.MakeMapButton);
            makeMap.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    onBuildMapButton();
                    //function called when button is clicked.
                }
            });
        } else {
            Toast.makeText(this, "Cannot construct map of this county, pick another or press 'Update'.", Toast.LENGTH_SHORT).show();
            //User done goofed!
        }
        Button updateDB;
        updateDB = (Button) findViewById(R.id.updateButton);
        updateDB.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onUpdateMapButton();
            }
        });
        Button deleteDB;
        deleteDB = (Button) findViewById(R.id.deleteButton);
        deleteDB.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onDeleteMapButton();
            }
        });
    }

    /**
     * We are jumping ship here. The create map function was called so we are bundling up the
     * countySelected and stateSelected values and shipping off to the DisplayMap class as our new
     * main.
     */
    public void onBuildMapButton() {
        myIntent = new Intent(this, DisplayMap.class);
        myIntent.putExtra(MESSAGE1, countySelected);
        myIntent.putExtra(MESSAGE2, stateSelected);
        //myIntent.putExtra(MESSAGE, DB.getAccidents(countySelected).toArray(new String[DB.getAccidents(countySelected).size()]));
        startActivity(myIntent);
    }

    public void onDeleteMapButton() {
        DatabaseManager DB = new DatabaseManager(this);
        DB.deleteCurrent(stateSelected, countySelected);
        Toast.makeText(this, "Accident list for " + countySelected + " county deleted.", Toast.LENGTH_SHORT).show();
    }

    /**
     * Called when the update button is pressed.
     */
    public void onUpdateMapButton() {
        m_objThreadClient = new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    DatagramSocket udpPing = new DatagramSocket();
                    udpPing.setBroadcast(true);
                    String outstr = "Client Req";
                    byte outblock[] = outstr.getBytes();
                    DatagramPacket outpacket = new DatagramPacket(outblock, outblock.length, getBroadcastAddress(), port);
                    udpPing.send(outpacket);

                    byte inblock[] = new byte[256];
                    DatagramPacket inpacket = new DatagramPacket(inblock, inblock.length);
                    udpPing.receive(inpacket);
                    String instr = new String(inpacket.getData(), 0, inpacket.getLength());
                    udpPing.close();

                    if (instr.equals("Server Ack")) {
                        clientSocket = new Socket(inpacket.getAddress(), port);
                        clientSocket.setReuseAddress(true);
                        ObjectOutputStream oos = new ObjectOutputStream(clientSocket.getOutputStream());
                        oos.writeObject("Send now.");

                        String frmSrvr = "";
                        Scanner sc = new Scanner(clientSocket.getInputStream());
                        ArrayList<String> CroixDB = new ArrayList<String>();
                        frmSrvr = sc.nextLine();
                        while (frmSrvr.compareTo("Done") != 0) {
                            CroixDB.add(frmSrvr);
                            frmSrvr = sc.nextLine();
                        }
                        Set<String> AccidentSet = new HashSet<String>(CroixDB);
                        Message clientMessage = Message.obtain();
                        clientMessage.obj = AccidentSet;
                        mHandler.sendMessage(clientMessage);

                        sc.close();
                        clientSocket.close();
                    }
                } catch (IOException e) {
                }
            }
        });
        m_objThreadClient.start();
    }

    private InetAddress getBroadcastAddress() throws IOException {
        DhcpInfo dhcp = wifi.getDhcpInfo();

        int broadcast = (dhcp.ipAddress & dhcp.netmask) | ~dhcp.netmask;
        byte[] quads = new byte[4];
        for (int k = 0; k < 4; k++)
            quads[k] = (byte) ((broadcast >> k * 8) & 0xFF);
        return InetAddress.getByAddress(quads);
    }

    Handler mHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            toDatabase((Set<String>) msg.obj);
        }
    };

    /**
     * Called by the mHandler so that the accident list can be passed out of the new thread.
     */
    public void toDatabase(Set<String> serverMsg) {
        DatabaseManager DB = new DatabaseManager(this);
        DB.createAccidentDB(serverMsg, countySelected, stateSelected);
        for (String s : serverMsg) {
            Toast.makeText(this, s, Toast.LENGTH_SHORT).show();
        }
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
}

