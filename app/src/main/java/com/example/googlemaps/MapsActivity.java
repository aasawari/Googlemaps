package com.example.googlemaps;

import android.app.Activity;
import android.app.LoaderManager;
import android.content.ContentValues;
import android.content.CursorLoader;
import android.content.Loader;
import android.database.Cursor;
import android.location.Location;
import android.location.LocationListener;
import android.net.Uri;
import android.os.AsyncTask;
import android.support.v4.app.FragmentActivity;
import android.os.Bundle;
import android.view.MotionEvent;
import android.view.View;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapFragment;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;

public class MapsActivity extends FragmentActivity implements LoaderManager.LoaderCallbacks<Cursor> {

    private GoogleMap mMap;
    private final LatLng LOCATION_UNIV = new LatLng(37.349642, -121.938987);
    private final LatLng LOCATION_BUILDING = new LatLng(37.348190, -121.937975);

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);
        mMap = ((MapFragment)getFragmentManager().findFragmentById(R.id.map)).getMap();
        mMap.addMarker(new MarkerOptions().position(LOCATION_BUILDING).title("Find me here!"));

        mMap.setOnMapClickListener(new GoogleMap.OnMapClickListener() {

            @Override
            public void onMapClick(LatLng point) {
                mMap.addMarker(new MarkerOptions().position(point));

                ContentValues contentValues = new ContentValues();
                contentValues.put(LocationsDB.FIELD_LAT, point.latitude);
                contentValues.put(LocationsDB.FIELD_LNG, point.longitude);
                contentValues.put(LocationsDB.FIELD_ZOOM, mMap.getCameraPosition().zoom);

                LocationInsertTask insertTask = new LocationInsertTask();
                insertTask.execute(contentValues);

            }
        });

        mMap.setOnMapLongClickListener(new GoogleMap.OnMapLongClickListener() {
            @Override
            public void onMapLongClick(LatLng point) {
                mMap.clear();
                LocationDeleteTask deleteTask = new LocationDeleteTask();
                deleteTask.execute();
            }
        });
        getLoaderManager().initLoader(0, null, this);
    }

    public void onClick_City(View v){
        mMap.setMapType(GoogleMap.MAP_TYPE_SATELLITE);
        CameraUpdate update = CameraUpdateFactory.newLatLngZoom(LOCATION_UNIV, 10);
        mMap.animateCamera(update);
    }

    public void onClick_University(View v){
        mMap.setMapType(GoogleMap.MAP_TYPE_TERRAIN);
        CameraUpdate update = CameraUpdateFactory.newLatLngZoom(LOCATION_UNIV, 13);
        mMap.animateCamera(update);
    }

    public void onClick_Building(View v){
        mMap.setMapType(GoogleMap.MAP_TYPE_NORMAL);
        CameraUpdate update = CameraUpdateFactory.newLatLngZoom(LOCATION_BUILDING, 17);
        mMap.animateCamera(update);
    }

    private class LocationInsertTask extends AsyncTask<ContentValues, Void, Void>{
        @Override
        protected Void doInBackground(ContentValues...contentValues){
            getContentResolver().insert(LocationsContentProvider.CONTENT_URI, contentValues[0]);
            return null;
        }
    }

    private class LocationDeleteTask extends AsyncTask<Void, Void, Void>{
        @Override
        protected Void doInBackground(Void...params){
            getContentResolver().delete(LocationsContentProvider.CONTENT_URI, null, null);
            return null;
        }
    }

    @Override
    public Loader<Cursor> onCreateLoader(int arg0, Bundle arg1){
        Loader<Cursor> c = null;
        Uri uri = LocationsContentProvider.CONTENT_URI;
        c = new CursorLoader(this, uri, null, null, null, null);;
        return c;
    }

    @Override
    public void onLoadFinished(Loader<Cursor> arg0, Cursor arg1){
        int locationCount = 0;
        double lat = 0;
        double lng = 0;
        float zoom = 0;

        //Number of locations available in the SQLite database table
        if(arg1 != null){
            locationCount = arg1.getCount();
            //Move the current record pointer to the first row of the table
            arg1.moveToFirst();
        }
        else{
            locationCount = 0;
        }
        for(int i=0;i<locationCount;i++){
            lat = arg1.getDouble(arg1.getColumnIndex(LocationsDB.FIELD_LAT));
            lng = arg1.getDouble(arg1.getColumnIndex(LocationsDB.FIELD_LNG));
            zoom = arg1.getFloat(arg1.getColumnIndex(LocationsDB.FIELD_ZOOM));

            LatLng place = new LatLng(lat, lng);

            mMap.addMarker(new MarkerOptions().position(place));

            arg1.moveToNext();
        }
        if(locationCount>0){
            mMap.moveCamera(CameraUpdateFactory.newLatLng(new LatLng(lat,lng)));
            mMap.animateCamera(CameraUpdateFactory.zoomTo(zoom));
        }
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader){

    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }
}
