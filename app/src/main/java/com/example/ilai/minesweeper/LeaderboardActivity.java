package com.example.ilai.minesweeper;

import android.content.Context;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Color;
import android.hardware.GeomagneticField;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.location.Address;
import android.location.Criteria;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationManager;
import android.os.Bundle;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;

import com.example.ilai.minesweeper.Logic.Level;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class LeaderboardActivity extends AppCompatActivity
        implements LeaderboardFragment.OnGameSelectedListener, OnMapReadyCallback, SensorEventListener, LocationListener{

    private Button mEasyLabel;
    private Button mMediumLabel;
    private Button mHardLabel;
    private Button mChosenLabel; //easy, medium or hard

    private GoogleMap mMap;
    private ArrayList<Marker> mMarkers;

    private SensorManager mSensorManager;
    private Sensor mRotationVectorSensor;
    private float[] mRotationMatrix = new float[16];
    private float mDeclination;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_leaderboards);

        // Initializing the chosen label:
        mChosenLabel = (Button) findViewById(R.id.easy_label);
        mChosenLabel.setBackgroundColor(Color.GRAY);

        // Setting up difficulty labels:
        setupEasyLabel();
        setupMediumLabel();
        setupHardLabel();

        // Setting up leaderboard fragment:
        setupLeaderboardFragment();

        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

        // Setting up the rotation vector sensor:
        sutupRotationVectorSensor();

    }


    private void sutupRotationVectorSensor() {

        mSensorManager = (SensorManager)getSystemService(Context.SENSOR_SERVICE);

        List<Sensor> sensorList = mSensorManager.getSensorList(Sensor.TYPE_ROTATION_VECTOR);

        if(sensorList.get(0) != null) {
            Log.i("Sensor Activity","Rotation Vector Sensor Aquired");
            mRotationVectorSensor = sensorList.get(0);
        } else {
            Log.e("Sensor Activity","No Rotation Vector Sensor Available");
        }

    }


    @Override
    protected void onResume() {
        super.onResume();
        // Register listener for sensor:
        mSensorManager.registerListener(this, mRotationVectorSensor, SensorManager.SENSOR_DELAY_NORMAL);
    }

    @Override
    protected void onPause() {
        super.onPause();
        // Unregister listener for sensor:
        mSensorManager.unregisterListener(this, mRotationVectorSensor);
    }


    @Override
    public void onLocationChanged(Location location) {

        GeomagneticField field = new GeomagneticField(
                (float)location.getLatitude(),
                (float)location.getLongitude(),
                (float)location.getAltitude(),
                System.currentTimeMillis()
        );

        // getDeclination returns degrees
        mDeclination = field.getDeclination();

    }


    @Override
    public void onSensorChanged(SensorEvent event) {
        if(event.sensor.getType() == Sensor.TYPE_ROTATION_VECTOR) {
            SensorManager.getRotationMatrixFromVector(mRotationMatrix, event.values);
            float[] orientation = new float[3];
            SensorManager.getOrientation(mRotationMatrix, orientation);
            float bearing = (float)Math.toDegrees(orientation[0]) + mDeclination;
            updateCamera(bearing);
        }
    }

    private void updateCamera(float bearing) {
        // If map exists, give it the right position and the right bearing:
        if(mMap != null){
            CameraPosition oldPos = mMap.getCameraPosition();
            CameraPosition pos = CameraPosition.builder(oldPos).bearing(bearing).build();
            mMap.animateCamera(CameraUpdateFactory.newCameraPosition(pos), 400, null);
        }
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {}


    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMarkers = new ArrayList<Marker>();
        if ( ContextCompat.checkSelfPermission( this, android.Manifest.permission.ACCESS_COARSE_LOCATION ) !=
                PackageManager.PERMISSION_GRANTED ) {
            ActivityCompat.requestPermissions( this, new String[] {  android.Manifest.permission.ACCESS_COARSE_LOCATION  },
                    Integer.parseInt(android.Manifest.permission.ACCESS_COARSE_LOCATION));
        }
        googleMap.setMyLocationEnabled(true);
        LocationManager locationManager = (LocationManager) getSystemService(LOCATION_SERVICE);
        Criteria criteria = new Criteria();
        String provider = locationManager.getBestProvider(criteria, true);
        Location myLocation = locationManager.getLastKnownLocation(provider);
        if(myLocation!=null){
            LatLng latLng = new LatLng(myLocation.getLatitude(), myLocation.getLongitude());
            googleMap.moveCamera(CameraUpdateFactory.newLatLng(latLng));
        }
        googleMap.setMapType(GoogleMap.MAP_TYPE_NORMAL);
        googleMap.moveCamera(CameraUpdateFactory.zoomTo(10));
        mMap = googleMap;
        setupMap(Level.EASY);
    }


    private void setupMap(Level mode) {

        mMap.clear();

        if(mMarkers != null && mMarkers.size() > 0)
            mMarkers.removeAll(mMarkers);

        GameDbHelper mDbHelper = new GameDbHelper(this);
        Cursor cursor = mDbHelper.getGamesSortedByGT(mode.toString());

        Geocoder geocoder = new Geocoder(this);

        for(int i = 0; i < cursor.getCount(); i++) {

            cursor.moveToPosition(i);

            String title = cursor.getString(2);
            String snippet = "NO. " + i+1 + " – " + cursor.getString(0) + " (" + cursor.getString(1) + ")";

            List<Address> addressList = null;

            try {
                addressList = geocoder.getFromLocationName(cursor.getString(2), 1);
            } catch (IOException e) {
                e.printStackTrace();
            }

            if(addressList != null){
                Address address = addressList.get(0);
                LatLng latLng = new LatLng(address.getLatitude(), address.getLongitude());
                MarkerOptions marker = new MarkerOptions().position(latLng).title(title).snippet(snippet);
                mMarkers.add(mMap.addMarker(marker));
            }

        }

        mMap.setOnMarkerClickListener(new GoogleMap.OnMarkerClickListener () {
            @Override
            public boolean onMarkerClick(Marker marker) {
                mMap.moveCamera(CameraUpdateFactory.newLatLng(marker.getPosition()));
                for(int i = 0; i < mMarkers.size(); i++) {

                    if(marker.equals(mMarkers.get(i))){
                        LeaderboardFragment leaderboardFragment = (LeaderboardFragment)
                                getSupportFragmentManager().findFragmentByTag(LeaderboardFragment.LEADERBOARD_FRAGMENT_TAG);
                        leaderboardFragment.updateSelectedGame(i+1);
                        marker.showInfoWindow();
                        return true;
                    }
                }
                return false;
            }
        });
    }


    private void setupEasyLabel(){

        mEasyLabel = (Button) findViewById(R.id.easy_label);

        mEasyLabel.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                if(!mEasyLabel.equals(mChosenLabel)){
                    mChosenLabel.setBackgroundColor(Color.LTGRAY);
                    mChosenLabel = mEasyLabel;
                    mChosenLabel.setBackgroundColor(Color.GRAY);
                    LeaderboardFragment leaderboardFragment = (LeaderboardFragment)
                            getSupportFragmentManager().findFragmentByTag(LeaderboardFragment.LEADERBOARD_FRAGMENT_TAG);
                    leaderboardFragment.updateMode(Level.EASY);
                    setupMap(Level.EASY);
                }

            }
        });

    }


    private void setupMediumLabel(){

        mMediumLabel = (Button) findViewById(R.id.medium_label);

        mMediumLabel.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {

                if(!mMediumLabel.equals(mChosenLabel)){
                    mChosenLabel.setBackgroundColor(Color.LTGRAY);
                    mChosenLabel = mMediumLabel;
                    mChosenLabel.setBackgroundColor(Color.GRAY);
                    LeaderboardFragment leaderboardFragment = (LeaderboardFragment)
                            getSupportFragmentManager().findFragmentByTag(LeaderboardFragment.LEADERBOARD_FRAGMENT_TAG);
                    leaderboardFragment.updateMode(Level.MEDIUM);
                    setupMap(Level.MEDIUM);

                }

            }
        });

    }


    private void setupHardLabel(){

        mHardLabel = (Button) findViewById(R.id.hard_label);

        mHardLabel.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {

                if(!mHardLabel.equals(mChosenLabel)){
                    mChosenLabel.setBackgroundColor(Color.LTGRAY);
                    mChosenLabel = mHardLabel;
                    mChosenLabel.setBackgroundColor(Color.GRAY);
                    LeaderboardFragment leaderboardFragment = (LeaderboardFragment)
                            getSupportFragmentManager().findFragmentByTag(LeaderboardFragment.LEADERBOARD_FRAGMENT_TAG);
                    leaderboardFragment.updateMode(Level.HARD);
                    setupMap(Level.HARD);

                }

            }
        });

    }


    private void setupLeaderboardFragment() {

        LeaderboardFragment leaderboardFragment = (LeaderboardFragment)getSupportFragmentManager().
                findFragmentByTag(LeaderboardFragment.LEADERBOARD_FRAGMENT_TAG);

        if (leaderboardFragment == null) {
            getSupportFragmentManager()
                    .beginTransaction()
                    .add(R.id.leaderboard_fragment_container, LeaderboardFragment.newInstance(),
                            LeaderboardFragment.LEADERBOARD_FRAGMENT_TAG)
                    .commit();
        }

    }

    @Override
    public void onGameSelected(int position) {

        if(position < mMarkers.size()) {
            mMap.moveCamera(CameraUpdateFactory.newLatLng(mMarkers.get(position).getPosition()));
            mMarkers.get(position).showInfoWindow();
        }
        else{
            for(int i = 0; i < mMarkers.size(); i++){
                if(mMarkers.get(i).isInfoWindowShown()) {
                    mMarkers.get(i).hideInfoWindow();
                    return;
                }
            }
        }

    }


}

