package com.example.kron.googlemapapp;

import android.content.Context;
import android.content.SharedPreferences;

import java.util.Set;

/**
 * Created by Kron on 4/7/2016.
 */
public class DatabaseManager {
    private static final String PREFS_NAME = "DatabasePrefrences",
            STATE = "DB-STATE";
    private SharedPreferences dataPref;

    /**
     * Our constructor which need only define the starting parameters of the sharedPreferences the
     * application will use.
     */
    public DatabaseManager(Context context){
        dataPref = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
    }

    /**
     * Used to be more complicated but now we will take a String array of accidents from the UpdateDB
     * class and store it in sharedPrefs using the state and county names with a dash in between as
     * the access key.
     */
    public void addAccidentToDB(Set<String> accident, String longe, String lat){
        SharedPreferences.Editor editor = dataPref.edit();
        String location = longe+"-"+lat;
        if(dataPref.getString(location, null) != null){
            editor.remove(location);
            editor.commit();
        }
        editor.putStringSet(location,accident);
    }
//    public void createAccidentDB(Set<String> accidentDB, String county, String state){
//        SharedPreferences.Editor editor = dataPref.edit();
//        editor.putStringSet(state+"-"+county, accidentDB);
//        editor.commit();
//    }

    /**
     * After selecing the county and state in the drop down menus the two string values are sent here
     * This is where we reference the sharedpreferences to acquire the actual list of accidents from
     * the county.
     */
    public Set<String> getAccident(String longe, String lat){
        return dataPref.getStringSet(longe+"-"+lat,null);
    }
//    public Set<String> getAccidents(String county, String state){
//        return dataPref.getStringSet(state+"-"+county, null);
//    }

    /**
     * Function to delete certain key value pairs in the sharedpreferences.
     */
    public void clearPrefs(){
        SharedPreferences.Editor editor = dataPref.edit();
        editor.clear();
        editor.commit();
    }
//    public void deleteCurrent(String county, String state){
//        SharedPreferences.Editor editor = dataPref.edit();
//        editor.remove(state+"-"+county);
//        editor.commit();
//    }
}
