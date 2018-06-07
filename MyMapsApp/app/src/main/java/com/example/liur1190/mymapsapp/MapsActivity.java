package com.example.liur1190.mymapsapp;

import android.Manifest;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.location.Address;
import android.location.Criteria;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.location.LocationProvider;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.FragmentActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.Circle;
import com.google.android.gms.maps.model.CircleOptions;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;

import java.io.IOException;
import java.util.List;
import java.util.Locale;

public class MapsActivity extends FragmentActivity implements OnMapReadyCallback {

    private GoogleMap mMap;
    private EditText locationSearch;
    private Location myLocation;
    private LocationManager locationManager;
    private boolean isGPSEnabled = false;
    private boolean isNetworkEnabled = false;

    private boolean notTrackingMyLocation = true;
    private boolean gotMyLocationOneTime;
    private double latitude, longitude;

    private static final long MIN_TIME_BTWN_UPDATES = 1000 * 5; //updates in msec
    private static final float MIN_DISTANCE_CHANGE_FOR_UPDATES = 0.0f;
    private static final int MY_LOC_ZOOM_FACTOR = 17;

    public static final int REQUEST_FINE = 2;
    public static final int REQUEST_COARSE = 2;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);
    }


    /**
     * Manipulates the map once available.
     * This callback is triggered when the map is ready to be used.
     * This is where we can add markers or lines, add listeners or move the camera. In this case,
     * we just add a marker near Sydney, Australia.
     * If Google Play services is not installed on the device, the user will be prompted to install
     * it inside the SupportMapFragment. This method will only be triggered once the user has
     * installed Google Play services and returned to the app.
     */
    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
        locationSearch = (EditText) findViewById(R.id.editText_addr);

        // Add a marker in Sydney and move the camera
        //LatLng sydney = new LatLng(-34, 151);
        //mMap.addMarker(new MarkerOptions().position(sydney).title("Marker in Sydney"));
        //mMap.moveCamera(CameraUpdateFactory.newLatLng(sydney));

        //Add a marker at your place of birth and move the camera to it
        //When the marker is tapped, display "Born here"
        LatLng newHaven = new LatLng(41.3, -72.9);
        mMap.addMarker(new MarkerOptions().position(newHaven).title("Born here"));
        mMap.moveCamera(CameraUpdateFactory.newLatLng(newHaven));

        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            Log.d("MyMapsApp", "Faied FINE Permission Check");
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, 2);

        }

        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            Log.d("MyMapsApp", "Faied COARSE Permission Check");
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_COARSE_LOCATION}, 2);

        }

        if ((ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) ||
                (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION)) == PackageManager.PERMISSION_GRANTED) {
            mMap.setMyLocationEnabled(true);

        }
    }


    //Add a view button and method to switch between satellite and map views
    public void changeView(View view) {
        Log.d("MyMapsApp", "changeView: View button clicked");
        if (mMap.getMapType() == 1) {
            mMap.setMapType(2);
        } else if (mMap.getMapType() == 2) {
            mMap.setMapType(1);
        }
    }


    //add an onSearch() method that allows you to put a point of interest query
    public void onSearch(View view) {
        String location = locationSearch.getText().toString();
        List<Address> addressList = null;

        //Use LocationManager for user location info
        LocationManager service = (LocationManager) getSystemService(LOCATION_SERVICE);
        Criteria criteria = new Criteria();
        String provider = service.getBestProvider(criteria, false);

        Log.d("MyMapsApp", "onSearch: location = " + location);
        Log.d("MyMapsApp", "onSearch: provider = " + provider);

        LatLng userLocation = null;

        try {

            //Check the last known location, need to specifically list the providers (network or gps)
            if (locationManager != null) {
                if ((myLocation = locationManager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER)) != null) {
                    userLocation = new LatLng(myLocation.getLatitude(), myLocation.getLongitude());
                    Log.d("MyMapsApp", "onSearch: using NETWORK_PROVIDER userLocation is: " + myLocation.getLatitude() + " " + myLocation.getLongitude());
                    Toast.makeText(this, "Userloc: " + myLocation.getLatitude() + " " + myLocation.getLongitude(), Toast.LENGTH_SHORT);
                } else if ((locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER) != null)) {
                    userLocation = new LatLng(myLocation.getLatitude(), myLocation.getLongitude());
                    Log.d("MyMapsApp", "onSearch: using GPS_PROVIDER userLocation is: " + myLocation.getLatitude() + " " + myLocation.getLongitude());
                    Toast.makeText(this, "Userloc: " + myLocation.getLatitude() + " " + myLocation.getLongitude(), Toast.LENGTH_SHORT).show();
                } else {
                    Log.d("MyMapsApp", "onSearch: myLocation is null");
                }
            }
        } catch (SecurityException | IllegalArgumentException e) {
            Log.d("MyMapsApp", "Exception on getLastKnownLocation");
            Toast.makeText(this, "Exception on getLastKnownLocation", Toast.LENGTH_SHORT).show();
        }

        if (!location.matches("")) {
            //Create Geocoder
            Geocoder geocoder = new Geocoder(this, Locale.US);

            try {
                //Get a list of Addresses
                addressList = geocoder.getFromLocationName(location, 100,
                        userLocation.latitude - (5.0 / 60.0),
                        userLocation.longitude - (5.0 / 60.0),
                        userLocation.latitude + (5.0 / 60.0),
                        userLocation.longitude + (5.0 / 60.0));

                Log.d("MyMapsApp", "created addressList");

            } catch (IOException e) {
                e.printStackTrace();
            }

            if (!addressList.isEmpty()) {
                Log.d("MyMapsApp", "Address list size: " + addressList.size());

                for (int i = 0; i < addressList.size(); i++) {
                    Address address = addressList.get(i);
                    LatLng latLng = new LatLng(address.getLatitude(), address.getLongitude());
                    mMap.addMarker(new MarkerOptions().position(latLng).title(i + ": " + address.getSubThoroughfare()));
                    mMap.animateCamera(CameraUpdateFactory.newLatLng(latLng));
                    Log.d("MyMapsApp", "added Marker and updated animateCamera");
                }
            }
        }
    }

    //method getLocation to plae a marker at current location
    public void getLocation(View view) {
        try {
            locationManager = (LocationManager) getSystemService(LOCATION_SERVICE);

            //get GPS status
            //isProviderEnabled returns true if user has enabled gps on phone
            isGPSEnabled = locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER);
            if (isGPSEnabled) {
                Log.d("MyMapsApp", "getLocation: GPS is enabled");
            }

            //get Network status
            isNetworkEnabled = locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER);
            if (isNetworkEnabled) {
                Log.d("MyMapsApp", "getLocation: Network is enabled");
            }

            if (!isGPSEnabled && !isNetworkEnabled) {
                Log.d("MyMapsApp", "getLocation: no provider is enabled");
            } else {
                if (isNetworkEnabled) {
                    if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED
                            && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                        return;
                    }
                    locationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER,
                            MIN_TIME_BTWN_UPDATES, MIN_DISTANCE_CHANGE_FOR_UPDATES, locationListenerNetwork);
                }
                if (isGPSEnabled) {
                    if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED
                            && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                        return;
                    }
                    locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER,
                            MIN_TIME_BTWN_UPDATES, MIN_DISTANCE_CHANGE_FOR_UPDATES, locationListenerGPS);
                    //launch locationListenerGps
                    //Code here
                }
            }

        } catch (Exception e) {
            Log.d("MyMapsApp", "getLocation: Caught exception");
            e.printStackTrace();

        }
    }

    public void dropAMarker(String provider) {
        LatLng userLocation;
        if (locationManager != null) {
            Log.d("MyMapsApp", "dropAMarker: locationManager != null");
            if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED
                    && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                Log.d("MyMapsApp", "dropAMarker: permissions failed");
                return;
            }
            myLocation = new Location(provider);
        }

        if (myLocation == null) {
            //display a message in Log.d and/or Toast


            //if(locationManager!=null)
            //if(checkSelfPermission fails)
            //return
            //myLocation = locationManager.getLastKnownLocation(provider)
            //LatLng userLocation = null;
            //if(myLocation == null) print log or toast message)
            //else
            //userLocation = new LatLng(myLocation.getLatitude(),myLocation.getLongitude()
            //CameraUpdate update = CameraUpdateFactory.newLatLngZoom(userLocation, MY_LOC_ZOOM_FACTOR)
            //if(provider==LocationManager.GPS_PROVIDER)
            //add circle for the market
            //else add circle for the marker
            //mMap.animateCamera(update)


            Log.d("MyMapsApp", "dropAMarker: Location is null");
            Toast.makeText(getApplicationContext(), "myLocation is invalid", Toast.LENGTH_SHORT).show();
        } else {
            //Add a shape for your marker
            if (provider.equals("Network")) {
                myLocation = locationManager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER);
                userLocation = new LatLng(myLocation.getLatitude(), myLocation.getLongitude());

                CameraUpdate update = CameraUpdateFactory.newLatLngZoom(userLocation, MY_LOC_ZOOM_FACTOR);

                Circle circle = mMap.addCircle(new CircleOptions()
                        .center(userLocation)
                        .radius(1.5)
                        .strokeColor(Color.GREEN)
                        .strokeWidth(2)
                        .fillColor(Color.GREEN));
                Log.d("MyMapsApp", "dropAMarker: Network Marker added!");
            } else {
                myLocation = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
                userLocation = new LatLng(myLocation.getLatitude(), myLocation.getLongitude());

                Circle circle = mMap.addCircle(new CircleOptions()
                        .center(userLocation)
                        .radius(1.5)
                        .strokeColor(Color.BLUE)
                        .fillColor(Color.BLUE));
                Log.d("MyMapsApp", "dropAMarker: GPS Marker added!");
            }

            CameraUpdate update = CameraUpdateFactory.newLatLngZoom(userLocation, MY_LOC_ZOOM_FACTOR);

            mMap.animateCamera(update);
        }
    }
    public void clear(View v) {
        mMap.clear();
        Toast.makeText(getApplicationContext(), "Cleared All Markers", Toast.LENGTH_SHORT).show();
    }

    public void trackMyLocation(View view) {
        //kick off the location tracker using getLocation to start the LocationListener
        //if(notTrackingMyLocation) getLocation(); notTrackingMyLocation = false;
//        getLocation(view);
//        Log.d("MyMapsActivity", "trackMyLocation: calling getLocation");
//        if (notTrackingMyLocation){
//            getLocation(view);
//            notTrackingMyLocation = false;
//        }else{
//            locationManager.removeUpdates(locationListenerGPS);
//            locationManager.removeUpdates(locationListenerNetwork);
//            notTrackingMyLocation = true;
//        }
        //kick off the location tracker using getLocation to start the LocationListeners
        //if (notTrackingMyLocation) {getLocation()}; notTrackingMyLocation = false;
        //else {removeUpdates for both network and gps; notTrackingMyLocation = true)
        getLocation(view);
        Log.d("MyMapsApp", "trackMyLocation: calling getLocation");
        if (notTrackingMyLocation){
            getLocation(view);
            notTrackingMyLocation = false;
        }else{
            locationManager.removeUpdates(locationListenerGPS);
            locationManager.removeUpdates(locationListenerNetwork);
            notTrackingMyLocation = true;
        }

    }



    //LocationListener to setup callbacks for requestLocationUpdates
    LocationListener locationListenerGPS = new LocationListener() {
        @Override
        public void onLocationChanged(Location location) {
            //output a message in Log.d and Toast
            Log.d("MyMapsApp", "locationListenerGPS: Location has changed! GPS is running");
            Toast.makeText(getApplicationContext(), "Location has changed! GPS is running", Toast.LENGTH_SHORT);

            //drop a marker on the map (create a method called dropAmarker)
            dropAMarker("GPS");

            //disable network updates (see LocationManager to remove updates)
            locationManager.removeUpdates(locationListenerNetwork);

        }

        @Override
        public void onStatusChanged(String provider, int status, Bundle extras) {
            //output a message in Log.d and Toast

            //setup a switch statement on status
            //case: LocationProvider.AVAILABLE --> output a message to Log.d and/or Toast
            //case: LocationProvider.OUT_OF_SERVICE --> request updates from NETWORK_PROVIDER
            //case: LocationProvider.TEMPORARILY_UNAVAILABLE --> request updates from NETWORK_PROVIDER
            //case: default --> request updates from NETWORK_PROVIDER
            //AVAILABLE==2, TEMPORARILY_UNAVAILABLE==1, OUT_OF_SERVICE==0

            if (ActivityCompat.checkSelfPermission(getApplicationContext(), Manifest.permission.ACCESS_FINE_LOCATION)
                    != PackageManager.PERMISSION_GRANTED) {
                Log.d("MyMapsApp", "Failed Fine Permission Check");
                Log.d("MyMapsApp", Integer.toString(ActivityCompat.checkSelfPermission(getApplicationContext(),
                        Manifest.permission.ACCESS_FINE_LOCATION)));
                ActivityCompat.requestPermissions(getParent(),
                        new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, REQUEST_FINE);
            }

            if (ActivityCompat.checkSelfPermission(getApplicationContext(), Manifest.permission.ACCESS_COARSE_LOCATION)
                    != PackageManager.PERMISSION_GRANTED) {
                Log.d("MyMapsApp", "Failed Coarse Permission Check");
                Log.d("MyMapsApp", Integer.toString(ActivityCompat.checkSelfPermission(getApplicationContext(),
                        Manifest.permission.ACCESS_COARSE_LOCATION)));
                ActivityCompat.requestPermissions(getParent(),
                        new String[]{Manifest.permission.ACCESS_COARSE_LOCATION}, REQUEST_COARSE);
            }

            String statusString;
            switch (status) {
                case LocationProvider.OUT_OF_SERVICE:
                    statusString = "OUT_OF_SERVICE";
                    Log.d("MyMapsApp", "GPS: onStatusChanged: status = " + statusString);
                    locationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER,
                            MIN_TIME_BTWN_UPDATES, MIN_DISTANCE_CHANGE_FOR_UPDATES, locationListenerNetwork);
                    break;
                case LocationProvider.TEMPORARILY_UNAVAILABLE:
                    statusString = "TEMPORARILY_UNAVAILABLE";
                    Log.d("MyMapsApp", "GPS: onStatusChanged: status = " + statusString);
                    locationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER,
                            MIN_TIME_BTWN_UPDATES, MIN_DISTANCE_CHANGE_FOR_UPDATES, locationListenerNetwork);
                    break;
                case LocationProvider.AVAILABLE:
                    statusString = "AVAILABLE";
                    Log.d("MyMapsApp", "GPS: onStatusChanged: status = " + statusString + ", location is updating");
                    Toast.makeText(getApplicationContext(), "Location Status = " + statusString + ", updating", Toast.LENGTH_SHORT).show();
                    break;
                default:
                    statusString = "DEFAULT";
                    Log.d("MyMapsApp", "GPS: onStatusChanged: status = " + statusString);
                    locationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER,
                            MIN_TIME_BTWN_UPDATES, MIN_DISTANCE_CHANGE_FOR_UPDATES, locationListenerNetwork);
                    break;
            }

        }

        @Override
        public void onProviderEnabled(String provider) {}

        @Override
        public void onProviderDisabled(String provider) {}
    };

    LocationListener locationListenerNetwork = new LocationListener() {
        @Override
        public void onLocationChanged(Location location) {
            if (ActivityCompat.checkSelfPermission(getApplicationContext(), Manifest.permission.ACCESS_FINE_LOCATION)
                    != PackageManager.PERMISSION_GRANTED)
            {
                Log.d("MyMapsApp", "Failed Fine Permission Check");
                Log.d("MyMapsApp", Integer.toString(ActivityCompat.checkSelfPermission(getApplicationContext(),
                        Manifest.permission.ACCESS_FINE_LOCATION)));
                ActivityCompat.requestPermissions(getParent(),
                        new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, REQUEST_FINE);
            }

            if (ActivityCompat.checkSelfPermission(getApplicationContext(), Manifest.permission.ACCESS_COARSE_LOCATION)
                    != PackageManager.PERMISSION_GRANTED)
            {
                Log.d("MyMapsApp", "Failed Coarse Permission Check");
                Log.d("MyMapsApp", Integer.toString(ActivityCompat.checkSelfPermission(getApplicationContext(),
                        Manifest.permission.ACCESS_COARSE_LOCATION)));
                ActivityCompat.requestPermissions(getParent(),
                        new String[]{Manifest.permission.ACCESS_COARSE_LOCATION}, REQUEST_COARSE);
            }

            //output a message in Log.d and Toast
            Log.d("MyMapsApp", "locationListenerNetwork: Location has changed! Network is running");
            Toast.makeText(getApplicationContext(), "Location has changed! Network is running", Toast.LENGTH_SHORT).show();

            //drop a marker on the map (create a method called dropAMarker)
            dropAMarker("Network");

            //relaunch request for network location updates (requestLocationUpdates(NETWORK_PROVIDER))
            locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER,
                    MIN_TIME_BTWN_UPDATES, MIN_DISTANCE_CHANGE_FOR_UPDATES, locationListenerGPS);
        }

        @Override
        public void onStatusChanged(String provider, int status, Bundle extras) {
            //output a message in Log.d and/or Toast
            Log.d("MyMapsApp", "locationListenerNetwork: status has changed");

            if (ActivityCompat.checkSelfPermission(getApplicationContext(), Manifest.permission.ACCESS_FINE_LOCATION)
                    != PackageManager.PERMISSION_GRANTED)
            {
                Log.d("MyMapsApp", "Failed Fine Permission Check");
                Log.d("MyMapsApp", Integer.toString(ActivityCompat.checkSelfPermission(getApplicationContext(),
                        Manifest.permission.ACCESS_FINE_LOCATION)));
                ActivityCompat.requestPermissions(getParent(),
                        new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, REQUEST_FINE);
            }

            if (ActivityCompat.checkSelfPermission(getApplicationContext(), Manifest.permission.ACCESS_COARSE_LOCATION)
                    != PackageManager.PERMISSION_GRANTED)
            {
                Log.d("MyMapsApp", "Failed Coarse Permission Check");
                Log.d("MyMapsApp", Integer.toString(ActivityCompat.checkSelfPermission(getApplicationContext(),
                        Manifest.permission.ACCESS_COARSE_LOCATION)));
                ActivityCompat.requestPermissions(getParent(),
                        new String[]{Manifest.permission.ACCESS_COARSE_LOCATION}, REQUEST_COARSE);
            }

            String statusString;
            switch (status) {
                case LocationProvider.OUT_OF_SERVICE:
                    statusString = "OUT_OF_SERVICE";
                    Log.d("MyMapsApp", "GPS: onStatusChanged: status = " + statusString);
                    locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER,
                            MIN_TIME_BTWN_UPDATES, MIN_DISTANCE_CHANGE_FOR_UPDATES, locationListenerGPS);
                    break;
                case LocationProvider.TEMPORARILY_UNAVAILABLE:
                    statusString = "TEMPORARILY_UNAVAILABLE";
                    Log.d("MyMapsApp","GPS: onStatusChanged: status = "+statusString);
                    locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER,
                            MIN_TIME_BTWN_UPDATES, MIN_DISTANCE_CHANGE_FOR_UPDATES, locationListenerGPS);
                    break;
                case LocationProvider.AVAILABLE:
                    statusString = "AVAILABLE";
                    Log.d("MyMapsApp","GPS: onStatusChanged: status = "+statusString+", location is updating");
                    Toast.makeText(getApplicationContext(),"Location Status = "+statusString+", updating", Toast.LENGTH_SHORT).show();
                    break;
                default:
                    statusString = "DEFAULT";
                    Log.d("MyMapsApp","GPS: onStatusChanged: status = "+statusString);
                    locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER,
                            MIN_TIME_BTWN_UPDATES, MIN_DISTANCE_CHANGE_FOR_UPDATES, locationListenerGPS);
                    break;
            }
        }

        @Override
        public void onProviderEnabled(String provider) {}

        @Override
        public void onProviderDisabled(String provider) {}
    };
}







