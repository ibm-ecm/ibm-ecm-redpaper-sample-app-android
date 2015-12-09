package com.ibm.casesdk.sample.nearbytasks.views;

import android.Manifest;
import android.annotation.TargetApi;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.Snackbar;
import android.util.Log;
import android.view.View;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.ibm.casesdk.sample.nearbytasks.R;
import com.ibm.casesdk.sample.nearbytasks.utils.Utils;

/**
 * Base activity that can display a map, get user's location and display it on the map and also
 * register for location updates using the Google Fused Location API.
 * <p> For {@link android.os.Build.VERSION_CODES.M} , the class includes methods to request
 * location permission and handle the result.</p>
 * <p/>
 * Created by stelian on 20/10/2015.
 */
public abstract class BaseMapLocationActivity extends BaseActivity implements OnMapReadyCallback, GoogleApiClient.ConnectionCallbacks,
        GoogleApiClient.OnConnectionFailedListener, LocationListener {

    private static final String LOG_TAG = BaseMapLocationActivity.class.getSimpleName();
    private static final int PERMISSIONS_REQUEST_CODE = 657;
    protected GoogleMap mMap;
    protected GoogleApiClient mGoogleApiClient;
    protected Location mLastLocation;
    protected Marker mLastLocationMarker;
    protected boolean mReceivingUpdates = false;

    protected abstract void onMarkerInfoClicked(Marker marker);

    @Override
    protected void onStart() {
        super.onStart();
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            // check permission and request it if necessary
            checkAppPermission(Manifest.permission.ACCESS_FINE_LOCATION);
        } else {
            mGoogleApiClient.connect();
        }
    }

    @Override
    protected void onResume() {
        super.onResume();

        // register for receiving location updates
        if (!mReceivingUpdates) {
            registerForLocationUpdates();
        }
    }

    @Override
    protected void onPause() {
        super.onPause();

        // stop getting location updates
        try {

            // this will throw an error if the Google API client is not connected
            LocationServices.FusedLocationApi.removeLocationUpdates(mGoogleApiClient, this);
            mReceivingUpdates = false;
        } catch (Exception e) {
            // nothing we can really do
            mReceivingUpdates = false;
        }
    }

    /**
     * Manipulates the map once available.
     * This callback is triggered when the map is ready to be used.
     * This is where we setup the map options.
     * If Google Play services is not installed on the device, the user will be prompted to install
     * it inside the SupportMapFragment. This method will only be triggered once the user has
     * installed Google Play services and returned to the app.
     */
    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
        mMap.getUiSettings().setZoomControlsEnabled(true);
        mMap.getUiSettings().setCompassEnabled(true);
        mMap.getUiSettings().setMyLocationButtonEnabled(true);

        mMap.getUiSettings().setRotateGesturesEnabled(true);
        mMap.getUiSettings().setScrollGesturesEnabled(true);
        mMap.getUiSettings().setTiltGesturesEnabled(false);
        mMap.getUiSettings().setZoomGesturesEnabled(true);
        //or mMap.getUiSettings().setAllGesturesEnabled(true);

        // set info window click listener
        mMap.setOnInfoWindowClickListener(new GoogleMap.OnInfoWindowClickListener() {
            @Override
            public void onInfoWindowClick(Marker marker) {
                onMarkerInfoClicked(marker);
            }
        });
    }

    @Override
    public void onConnected(Bundle bundle) {
        Log.d(LOG_TAG, "onConnected:" + bundle);
        mLastLocation = LocationServices.FusedLocationApi.getLastLocation(mGoogleApiClient);
        if (mLastLocation != null) {
            // Add a marker to current location and move the camera
            showMyLocationOnMap(15);
        } else {
            Snackbar.make(Utils.getContentView(this), getString(R.string.err_no_last_location),
                    Snackbar.LENGTH_SHORT).show();

        }

        // register for receiving location updates
        registerForLocationUpdates();
    }

    @Override
    public void onConnectionSuspended(int i) {
    }

    @Override
    public void onConnectionFailed(ConnectionResult connectionResult) {
        // Could not connect to Google Play Services.  The user needs to select an account,
        // grant permissions or resolve an error in order to sign in. Refer to the javadoc for
        // ConnectionResult to see possible error codes.
        Log.d(LOG_TAG, "onConnectionFailed:" + connectionResult);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if (requestCode == PERMISSIONS_REQUEST_CODE) {
            if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                mGoogleApiClient.connect();
            } else {
                String message = getString(R.string.err_no_location_permission);

                Snackbar.make(Utils.getContentView(this),
                        message,
                        Snackbar.LENGTH_SHORT)
                        .setAction(getString(R.string.action_ok), new View.OnClickListener() {
                            @Override
                            public void onClick(View view) {
                                // request the permission again
                                checkAppPermission(Manifest.permission.ACCESS_FINE_LOCATION);
                            }
                        })
                        .setAction(getString(R.string.action_not_now), null)
                        .show();
            }

        } else {
            super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        }
    }

    protected void setupMap() {
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);
    }

    protected synchronized void setupGoogleApiClient() {
        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .addApi(LocationServices.API)
                .build();
    }

    protected void registerForLocationUpdates() {
        if (mGoogleApiClient != null && mGoogleApiClient.isConnected()) {
            LocationRequest mLocationRequest = new LocationRequest();
            mLocationRequest.setInterval(10000);
            mLocationRequest.setFastestInterval(5000);
            mLocationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);

            LocationServices.FusedLocationApi.requestLocationUpdates(mGoogleApiClient,
                    mLocationRequest, this);
            mReceivingUpdates = true;
        }
    }

    protected void showMyLocationOnMap(@NonNull int zoomLevel) {
        if (mLastLocation != null) {
            final LatLng pos = new LatLng(this.mLastLocation.getLatitude(), this.mLastLocation.getLongitude());

            mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(pos, zoomLevel));

            mLastLocationMarker = mMap.addMarker(new MarkerOptions().position(pos)
                    .title(getString(R.string.msg_last_known_location))
                    .snippet(Utils.getAddressFromLocation(this, mLastLocation)));
            mMap.moveCamera(CameraUpdateFactory.newLatLng(pos));
        }
    }

    @TargetApi(23)
    protected void checkAppPermission(@NonNull String permission) {
        if (checkSelfPermission(permission)
                != PackageManager.PERMISSION_GRANTED) {

            // Should we show an explanation?
            if (shouldShowRequestPermissionRationale(permission)) {
                // Explain to the user why we need the location
            }

            requestPermissions(new String[]{permission},
                    PERMISSIONS_REQUEST_CODE);
        }
    }
}
