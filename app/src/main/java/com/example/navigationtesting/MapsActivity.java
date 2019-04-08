package com.example.navigationtesting;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.renderscript.Float3;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.FragmentActivity;
import android.os.Bundle;
import android.util.Log;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.lemmingapex.trilateration.NonLinearLeastSquaresSolver;
import com.lemmingapex.trilateration.TrilaterationFunction;


import org.apache.commons.math3.fitting.leastsquares.LeastSquaresOptimizer;
import org.apache.commons.math3.fitting.leastsquares.LevenbergMarquardtOptimizer;

import java.util.ArrayList;

public class MapsActivity extends FragmentActivity implements OnMapReadyCallback, Callback<ArrayList<GalileoSatelliteData>> {
    private ArrayList<GalileoSatelliteData> galileoSatellites;
    private GoogleMap mMap;
    private LocationManager locationManager;
    private LocationManager nmeaListener;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);


        /*locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);

        LocationListener locationListener = new GPSListener(this);
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            return;
        }
        locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 2000, 10, locationListener);


        nmeaListener = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        nmeaListener.addNmeaListener(new NmeaMessagesListener());*/

        new RawGnssListener(this, this);
    }


    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;

        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            return;
        }
        //updateMapFromLocation(locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER));
        // Add a marker in Sydney and move the camera
        /*LatLng sydney = new LatLng(-34, 151);
        mMap.addMarker(new MarkerOptions().position(sydney).title("Marker in Sydney"));
        mMap.moveCamera(CameraUpdateFactory.newLatLng(sydney));*/
    }


    /*@Override
    public void callBack(String name, Object in) {
        //Object is new location returned from GPSListener
        Log.d("Project", "Retrieved new positions");

        Location l = (Location) in;
        Log.d("Project", "Latitude: "+Double.toString(l.getLatitude()));
        Log.d("Project", "Longitude"+Double.toString(l.getLongitude()));

        ArrayList<GalileoSatelliteData> galileoSatellites = (ArrayList<GalileoSatelliteData>)in;
        if(previousSatelliteId == -1){
            previousSatelliteId = galileoSatellites.get(0).getSvid();
        }
        for(GalileoSatelliteData g : galileoSatellites){
            if(g.getSvid() == previousSatelliteId){
                Location l = new Location("Dummy provider");
                SatellitePositionData position = g.getPosition();
                l.setLatitude(position.getEllipsoidalCoords().x);
                l.setLongitude(position.getEllipsoidalCoords().y);
                updateMapFromLocation(l);
            }
        }

        //updateMapFromLocation(l);

    }*/

    private void updateMapFromLocation(Location l){
        if(l == null){
            Log.d("Project", "updateMapFromLocation Location is null");
        }else{
            //Log.d("Project", "updateMapFromLocation Latitude: "+Double.toString(l.getLatitude()));
            //Log.d("Project", "updateMapFromLocation Longitude"+Double.toString(l.getLongitude()));
        }

        LatLng newPosition = new LatLng(l.getLatitude(), l.getLongitude());
        mMap.addMarker(new MarkerOptions().position(newPosition).title("You are here"));
        //mMap.moveCamera(CameraUpdateFactory.newLatLng(newPosition));
    }

    private int previousSatelliteId = -1;
    @Override
    public void callBack(String name, ArrayList<GalileoSatelliteData> sSat) {
        galileoSatellites = sSat;
        runOnUiThread(new Runnable(){
            @Override
            public void run() {
                mMap.clear();
                if(galileoSatellites.size() > 0){
                    if(previousSatelliteId == -1){
                        previousSatelliteId = galileoSatellites.get(0).getSvid();
                    }
                    for(GalileoSatelliteData g : galileoSatellites){
                        if(g.getSvid() == previousSatelliteId){
                            Location l = new Location("Dummy provider");
                            SatellitePositionData position = g.getPosition();
                            l.setLatitude(position.getEllipsoidalCoords().x);
                            l.setLongitude(position.getEllipsoidalCoords().y);
                            updateMapFromLocation(l);
                            //Log.i("Project", g.getPosition().toString());
                        }
                    }

                    if(galileoSatellites.size() >= 4){
                        //calculateUsersPositionTrilateration(galileoSatellites);
                    }else{
                        //Log.i("Project", "Number of detected satellites: "+galileoSatellites.size());
                    }
                }else{
                    Log.i("Project", "Number of detected satellites: "+galileoSatellites.size());
                }
            }
        });

    }



    public void calculateUsersPositionTrilateration(ArrayList<GalileoSatelliteData> sats){
        double[][] positions = new double[sats.size()][1];
        double[] distance = new double[sats.size()];
        for(int i=0; i<sats.size(); i++){

            double[] pos = new double[] {sats.get(i).getPosition().getCartesianCoords().x, sats.get(i).getPosition().getCartesianCoords().y};
            positions[i] = pos;

            distance[i] = sats.get(i).getPseudorange();
        }



        NonLinearLeastSquaresSolver solver = new NonLinearLeastSquaresSolver(new TrilaterationFunction(positions, distance), new LevenbergMarquardtOptimizer());
        LeastSquaresOptimizer.Optimum optimum = solver.solve();

        // the answer
        double[] centroid = optimum.getPoint().toArray();

        Log.i("Project", "The answer: ");
        for(int i=0; i<centroid.length; i++){
            Log.i("Project", Double.toString(centroid[i]));
        }

    }



    private static final Float3 convertCartesianToEllipsoidalCoordinates(Float3 cartesian) {
        double latitude = Math.atan2(cartesian.z, Math.sqrt(Math.pow(cartesian.x, 2) + Math.pow(cartesian.y, 2)));
        double longitude = Math.atan2(cartesian.y, cartesian.x);

        Float3 ellipsoidalCoords = new Float3();
        ellipsoidalCoords.x = (float) (latitude * (180 / Math.PI));
        ellipsoidalCoords.y = (float) (longitude * (180 / Math.PI));

        return ellipsoidalCoords;
    }
}
