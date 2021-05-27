package com.racing.newracingapp;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.FragmentActivity;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.drawable.Drawable;
import android.location.Location;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.provider.Settings;
import android.util.Log;
import android.widget.Toast;

import com.firebase.geofire.GeoFire;
import com.firebase.geofire.GeoLocation;
import com.firebase.geofire.GeoQuery;
import com.firebase.geofire.GeoQueryEventListener;
import com.firebase.geofire.LocationCallback;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptor;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.maps.model.PolylineOptions;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.maps.DirectionsApiRequest;
import com.google.maps.GeoApiContext;
import com.google.maps.PendingResult;
import com.google.maps.internal.PolylineEncoding;
import com.google.maps.model.DirectionsResult;
import com.google.maps.model.DirectionsRoute;

import java.util.ArrayList;
import java.util.List;

public class MapsActivity extends FragmentActivity implements OnMapReadyCallback, GoogleMap.OnPolylineClickListener {

    private static final String TAG = " hgvh";
    private GoogleMap mMap;
    private LatLng currentLatLng;
    private Polyline polyline1;
    private DatabaseReference dbRefDest;
    private Location destLoc;
    Drawable drawable;
    double destLat= 0, destLong = 0;

    private List<LatLng> polylinePoints = new ArrayList<>();
    private Marker mCurrLocationMarker;
    private ArrayList markerPoints= new ArrayList();
    private GeoApiContext mGeoApiContext;
    private ArrayList<PolylineData> mPolylinesData = new ArrayList<>();
    private DatabaseReference dbRefWin;
    private String winner="none";
    private String rceId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        rceId = getIntent().getStringExtra("raceId");
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
        dbRefDest = FirebaseDatabase.getInstance().getReference().child("myRacingApp").child(rceId).child("destination");
        dbRefWin = FirebaseDatabase.getInstance().getReference().child("myRacingApp").child(rceId).child("members");
        mMap = googleMap;
        drawable = getResources().getDrawable(R.drawable.ic_marker);

        // Add a marker  and move the camera
        polyline1 = mMap.addPolyline(new PolylineOptions().addAll(polylinePoints));
        fetchLocationUpdates();

        if(mGeoApiContext == null){
            mGeoApiContext = new GeoApiContext.Builder()
                    .apiKey(getString(R.string.channel_id))
                    .build();
        }
        mMap.setOnPolylineClickListener(this);
        mMap.setOnMapClickListener(new GoogleMap.OnMapClickListener() {
            @Override
            public void onMapClick(LatLng latLng2) {

                if (markerPoints.size() < 2) {

                // Adding new item to the ArrayList
                markerPoints.add(latLng2);

                // Creating MarkerOptions
                MarkerOptions options = new MarkerOptions();

                // Setting the position of the marker
                options.position(latLng2);

                if (markerPoints.size() == 1) {
                    options.icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_GREEN));
                } else if (markerPoints.size() == 2) {
                    options.icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_RED));
                    dbRefDest.child("lat").setValue(latLng2.latitude);
                    dbRefDest.child("long").setValue(latLng2.longitude);
                    dbRefDest.child("winner").setValue("none");
                    destLat = latLng2.latitude;
                    destLong = latLng2.longitude;
                    winner = "none";
                }

                // Add new marker to the Google Map Android API V2
                mMap.addMarker(options);

                // Checks, whether start and end locations are captured
                if (markerPoints.size() >= 2) {
                    LatLng origin = (LatLng) markerPoints.get(0);
                    LatLng dest = (LatLng) markerPoints.get(1);

                    calculateDirections(origin,dest);
                }
            }
            }
        });

    }

    private void fetchLocationUpdates() {
        final FirebaseDatabase database = FirebaseDatabase.getInstance();
        String id = Settings.System.getString(MapsActivity.this.getContentResolver(), Settings.Secure.ANDROID_ID);
        DatabaseReference ref = database.getReference().child("myRacingApp").child(rceId).child("members");
        GeoFire geoFire=new GeoFire(ref);
        geoFire.getLocation(id, new LocationCallback() {
            @Override
            public void onLocationResult(String key, GeoLocation location) {
                if (location != null) {

                    double latitude = 0, longitude = 0;
                    longitude = location.longitude;
                    latitude = location.latitude;
                    currentLatLng = new LatLng(latitude, longitude);
                    getUsersAround();

                    if(mMap!=null) {
                        checkWin();
                        polylinePoints.add(currentLatLng);
                        polyline1.setPoints(polylinePoints);
                        Log.w("tag", "Key:" + currentLatLng);
                        if(mCurrLocationMarker!=null){
                            mCurrLocationMarker.setPosition(currentLatLng);
                        }else{
                            mCurrLocationMarker = mMap.addMarker(new MarkerOptions()
                                    .position(currentLatLng)
                                    .icon(getMarkerIconFromDrawable(drawable))
                                    .title("Crnt"));
                        }
                        mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(currentLatLng, 16));
                    }
                } else {
                    System.out.println(String.format("There is no location for key %s in GeoFire", key));
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                System.err.println("There was an error getting the GeoFire location: " + databaseError);
            }
        });
    }

    private void checkWin() {
        dbRefDest.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {

                Iterable<DataSnapshot> data = snapshot.getChildren();
                for(DataSnapshot d: data){
                    if(d.getKey().equals("lat")){
                        destLat = (Double) d.getValue();
                    }else if(d.getKey().equals("long")){
                        destLong = (Double) d.getValue();
                    }
                    else if(d.getKey().equals("winner")){
                        winner = d.getValue(String.class);
                    }
                }
                destLoc = new Location("");
                destLoc.setLatitude(destLat);
                destLoc.setLongitude(destLong);
            }
            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

        DatabaseReference membersLocation = FirebaseDatabase.getInstance().getReference().child("myRacingApp").child(rceId).child("members");

        GeoFire geoFire = new GeoFire(membersLocation);
        GeoQuery geoQuery = geoFire.queryAtLocation(new GeoLocation(currentLatLng.longitude, currentLatLng.latitude), 999999999);

        geoQuery.addGeoQueryEventListener(new GeoQueryEventListener() {
            @Override
            public void onKeyEntered(String key, GeoLocation location) {
                Location crntLoc = new Location("");
                crntLoc.setLatitude(location.latitude);
                crntLoc.setLongitude(location.longitude);
                float distnce = crntLoc.distanceTo(destLoc);
                if (distnce < 50 && winner.equals("none")){
                    dbRefDest.child("winner").setValue(key);
                    winner = key;
                    Toast.makeText(MapsActivity.this, key +" won this Competition", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onKeyExited(String key) {

            }

            @Override
            public void onKeyMoved(String key, GeoLocation location) {
                Location crntLoc = new Location("");
                crntLoc.setLatitude(location.latitude);
                crntLoc.setLongitude(location.longitude);
                float distnce = crntLoc.distanceTo(destLoc);
                if (distnce < 50 && winner.equals("none")){
                    dbRefDest.child("winner").setValue(key);
                    winner = key;
                    Toast.makeText(MapsActivity.this, key +" won this Competition", Toast.LENGTH_SHORT).show();
                }

            }

            @Override
            public void onGeoQueryReady() {
            }

            @Override
            public void onGeoQueryError(DatabaseError error) {

            }
        });
    }

    private BitmapDescriptor getMarkerIconFromDrawable(Drawable drawable) {
        Canvas canvas = new Canvas();
        Bitmap bitmap = Bitmap.createBitmap(drawable.getIntrinsicWidth(), drawable.getIntrinsicHeight(), Bitmap.Config.ARGB_8888);
        canvas.setBitmap(bitmap);
        drawable.setBounds(0, 0, drawable.getIntrinsicWidth(), drawable.getIntrinsicHeight());
        drawable.draw(canvas);
        return BitmapDescriptorFactory.fromBitmap(bitmap);
    }

    private void calculateDirections(LatLng origin,LatLng dest){

        com.google.maps.model.LatLng destination = new com.google.maps.model.LatLng(
                dest.latitude,
                dest.longitude
        );
        DirectionsApiRequest directions = new DirectionsApiRequest(mGeoApiContext);

        directions.alternatives(true);
        directions.origin(
                new com.google.maps.model.LatLng(
                        origin.latitude,
                        origin.longitude
                )
        );
        directions.destination(destination).setCallback(new PendingResult.Callback<DirectionsResult>() {
            @Override
            public void onResult(DirectionsResult result) {
                addPolylinesToMap(result);
            }

            @Override
            public void onFailure(Throwable e) {
                Log.e(TAG, "onFailure: " + e.getMessage() );

            }
        });
    }
    private void addPolylinesToMap(final DirectionsResult result){
        new Handler(Looper.getMainLooper()).post(new Runnable() {
            @Override
            public void run() {
                Log.d(TAG, "run: result routes: " + result.routes.length);
                if(mPolylinesData.size() > 0){
                    for (PolylineData polylineData:mPolylinesData){
                        polylineData.getPolyline().remove();
                    }
                    mPolylinesData.clear();
                    mPolylinesData = new ArrayList<>();
                }

                for(DirectionsRoute route: result.routes){
                    Log.d(TAG, "run: leg: " + route.legs[0].toString());
                    List<com.google.maps.model.LatLng> decodedPath = PolylineEncoding.decode(route.overviewPolyline.getEncodedPath());

                    List<LatLng> newDecodedPath = new ArrayList<>();

                    // This loops through all the LatLng coordinates of ONE polyline.
                    for(com.google.maps.model.LatLng latLng: decodedPath){

//                        Log.d(TAG, "run: latlng: " + latLng.toString());

                        newDecodedPath.add(new LatLng(
                                latLng.lat,
                                latLng.lng
                        ));
                    }
                    Polyline polyline = mMap.addPolyline(new PolylineOptions().addAll(newDecodedPath));
                    polyline.setColor(ContextCompat.getColor(MapsActivity.this, R.color.pipeline));
                    polyline.setClickable(true);
                    mPolylinesData.add(new PolylineData(polyline, route.legs[0]));


                }
            }
        });
    }

    @Override
    public void onPolylineClick(@NonNull Polyline polyline) {
        for (PolylineData polylineData: mPolylinesData){
            if (polyline.getId().equals(polylineData.getPolyline().getId())){
                polylineData.getPolyline().setColor(ContextCompat.getColor(this, R.color.teal_700));
                polylineData.getPolyline().setZIndex(1);
            }
            else
            {
                polylineData.getPolyline().setColor(ContextCompat.getColor(this, R.color.pipeline));
                polylineData.getPolyline().setZIndex(0);
            }
        }

    }
    List<Marker> markers = new ArrayList<Marker>();
    private void getUsersAround(){

        DatabaseReference membersLocation = FirebaseDatabase.getInstance().getReference().child("myRacingApp").child(rceId).child("members");

        GeoFire geoFire = new GeoFire(membersLocation);
        GeoQuery geoQuery = geoFire.queryAtLocation(new GeoLocation(currentLatLng.longitude, currentLatLng.latitude), 999999999);

        geoQuery.addGeoQueryEventListener(new GeoQueryEventListener() {
            @Override
            public void onKeyEntered(String key, GeoLocation location) {

                for(Marker markerIt : markers){
                    if(markerIt.getTag().equals(key))
                        return;
                }

                LatLng driverLocation = new LatLng(location.latitude, location.longitude);

                Marker mDriverMarker = mMap.addMarker(new MarkerOptions().position(driverLocation).title(key).icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_ORANGE)));
                mDriverMarker.setTag(key);
                mDriverMarker.setTitle(key);

                markers.add(mDriverMarker);
            }

            @Override
            public void onKeyExited(String key) {
                for(Marker markerIt : markers){
                    if(markerIt.getTag().equals(key)){
                        markerIt.remove();
                    }
                }
            }

            @Override
            public void onKeyMoved(String key, GeoLocation location) {
                for(Marker markerIt : markers){
                    if(markerIt.getTag().equals(key)){
                        markerIt.setPosition(new LatLng(location.latitude, location.longitude));
                    }
                }
            }

            @Override
            public void onGeoQueryReady() {
            }

            @Override
            public void onGeoQueryError(DatabaseError error) {

            }
        });
    }
}