package pt.ipleiria.taes.shush;

import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.navigation.Navigation;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.Toast;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;

import org.json.JSONException;

import java.io.IOException;
import java.text.ParseException;
import java.util.List;

import pt.ipleiria.taes.shush.utils.LocalMeasurements;
import pt.ipleiria.taes.shush.utils.Locator;
import pt.ipleiria.taes.shush.utils.Measurement;
import pt.ipleiria.taes.shush.utils.MeasurementPopup;


public class MapsFragment extends Fragment implements OnMapReadyCallback {
    // Fragment Tag for Logging
    private static final String TAG = MapsFragment.class.getSimpleName();

    // Location permissions
    private static final int PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION = 1;
    private boolean locationPermissionGranted;

    // Map defaults if no GPS location acquired
    private static final int DEFAULT_ZOOM = 15;

    // Google Maps client instance
    private GoogleMap map;
    // Maps container camera position
    private CameraPosition cameraPosition;
    private static final String KEY_CAMERA_POSITION = "camera_position";

    private static final String KEY_LOCATION = "location";

    private Locator locator;
    private LocalMeasurements localMeasurements;
    private List<Measurement> measurements;

    /**
     * Listener for LocationResult task
     */
    private class LocationResultListener implements OnCompleteListener<Location> {

        @Override
        public void onComplete(@NonNull Task<Location> task) {
            if (task.isSuccessful()) {
                Location loc = locator.getLastKnownLocation();
                if (loc != null) {
                    map.moveCamera(CameraUpdateFactory.newLatLngZoom(
                            new LatLng(loc.getLatitude(),
                                    loc.getLongitude()), DEFAULT_ZOOM));
                }
            } else {
                Log.d(TAG, "Current location is null. Using defaults.");
                Log.e(TAG, "Exception: %s", task.getException());
                map.moveCamera(CameraUpdateFactory
                        .newLatLngZoom(Locator.DEFAULT_LOCATION, DEFAULT_ZOOM));
                map.getUiSettings().setMyLocationButtonEnabled(false);
            }
        }
    }

    private class CameraMoveListener implements GoogleMap.OnCameraIdleListener {
        @Override
        public void onCameraIdle() {
            LatLngBounds bounds = map.getProjection().getVisibleRegion().latLngBounds;
            loadLocalMeasurements();

            if(measurements == null) return;

            for (Measurement measurement: measurements) {
                LatLng loc = new LatLng(measurement.getLatitude(), measurement.getLongitude());
                if (bounds.contains(loc)) {
                    // Add the marker.
                    Log.d(TAG, "marker here");
                    map.addMarker(new MarkerOptions()
                            .position(loc))
                            .setTag(measurement);
                }
            }
        }
    }

    private class MarkerListener implements GoogleMap.OnMarkerClickListener {
        @Override
        public boolean onMarkerClick(final Marker marker) {

            // Retrieve the data from the marker.
            Measurement markerTag = (Measurement) marker.getTag();
            new MeasurementPopup(markerTag).showPopupWindow(getView());

            // Return false to indicate that we have not consumed the event and that we wish
            // for the default behavior to occur (which is for the camera to move such that the
            // marker is centered and for the marker's info window to open, if it has one).
            return false;
        }
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState)
    {
        return inflater.inflate(R.layout.fragment_map, container, false);
    }

    public void loadLocalMeasurements()
    {
        try {
            if(localMeasurements == null)
                localMeasurements = new LocalMeasurements(getContext());

            measurements = localMeasurements.getMeasurements();
        } catch(IOException | JSONException | ParseException ex)
        {
            Toast.makeText(getContext(), "Can't load measurements!", Toast.LENGTH_LONG).show();
        }
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState)
    {
        super.onActivityCreated(savedInstanceState);

        locator = new Locator(getActivity());

        // Retrieve location and camera position from saved instance state.
        if (savedInstanceState != null) {
            locator.setLastKnownLocation(savedInstanceState.getParcelable(KEY_LOCATION));
            cameraPosition = savedInstanceState.getParcelable(KEY_CAMERA_POSITION);
        }

        // Build the map container. This will obtain the SupportMapFragment and get notified when
        // the map is ready to be used.
        SupportMapFragment supportMapFragment = (SupportMapFragment) getChildFragmentManager().findFragmentById(R.id.map);
        supportMapFragment.getMapAsync(this);
    }

    /**
     * Saves the state of the map when the activity is paused.
     */
    @Override
    public void onSaveInstanceState(Bundle outState)
    {
        if (map != null) {
            outState.putParcelable(KEY_CAMERA_POSITION, map.getCameraPosition());
            outState.putParcelable(KEY_LOCATION, locator.getLastKnownLocation());
        }
        super.onSaveInstanceState(outState);
    }

    @Override
    public void onMapReady(GoogleMap map)
    {
        this.map = map;
        map.setOnCameraIdleListener(new CameraMoveListener());
        map.setOnMarkerClickListener(new MarkerListener());

        // Prompt the user for permission.
        locator.getLocationPermission();

        // Turn on the My Location layer and the related control on the map.
        updateLocationUI();

        // Get the current location of the device and set the position of the map.
        getDeviceLocation();
    }

    private void getDeviceLocation()
    {
        locator.getDeviceLocation(new LocationResultListener());
    }

    /**
     * Handles the result of the request for location permissions.
     */
    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           @NonNull String[] permissions,
                                           @NonNull int[] grantResults)
    {
        switch (requestCode) {
            case PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION: {
                // If request is cancelled, the result arrays are empty.
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    locator.setLocationPermissionGranted(true);
                }
            }
        }
        updateLocationUI();
    }

    /**
     * Updates the map's UI settings based on whether the user has granted location permission.
     */
    private void updateLocationUI() {
        if (map == null) {
            return;
        }
        try {
            if (locator.isLocationPermissionGranted()) {
                map.setMyLocationEnabled(true);
                map.getUiSettings().setMyLocationButtonEnabled(true);
            } else {
                map.setMyLocationEnabled(false);
                map.getUiSettings().setMyLocationButtonEnabled(false);
                locator.setLastKnownLocation(null);
                locator.getLocationPermission();
            }
        } catch (SecurityException e)  {
            Log.e(TAG, "Error on updating location UI: " + e.getMessage(), e);
        }
    }
}