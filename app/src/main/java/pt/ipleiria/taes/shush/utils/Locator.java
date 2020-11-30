package pt.ipleiria.taes.shush.utils;

import android.app.Activity;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Parcelable;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;

public class Locator{
    private static final String TAG = Locator.class.getSimpleName();

    // Location permissions
    private static final int PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION = 1;
    private boolean locationPermissionGranted;

    private final LatLng DEFAULT_LOCATION = new LatLng(-33.8523341, 151.2106085);

    private Location lastKnownLocation;
    // The entry point to the Fused Location Provider.
    private FusedLocationProviderClient fusedLocationProviderClient;

    private Activity activity;

    /**
     * Listener for LocationResult task
     */
    private class LocationResultListener implements OnCompleteListener<Location> {

        @Override
        public void onComplete(@NonNull Task<Location> task) {
            if (task.isSuccessful()) {
                // Set the current location of the device.
                lastKnownLocation = task.getResult();
                if(lastKnownLocation == null)
                {
                    Log.e(TAG, "Can't acquire location");
                } else {
                    Log.d(TAG, "Location acquired: " + lastKnownLocation.toString());
                }
            } else {
                Log.d(TAG, "Current location is null. Using defaults.");
                Log.e(TAG, "Exception: %s", task.getException());
            }
        }
    }

    public Locator(Activity activity)
    {
        this.activity = activity;
        fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(activity);
    }

    /**
     * Gets the current location of the device, and positions the map's camera.
     */
    public Task<Location> getDeviceLocation(OnCompleteListener<Location> onCompleteListener)
    {
        /*
         * Get the best and most recent location of the device, which may be null in rare
         * cases when a location is not available.
         */
        try {
            if (locationPermissionGranted) {
                Log.d(TAG, "Location granted. Getting location...");
                Task<Location> locationResult = fusedLocationProviderClient.getLastLocation();
                locationResult.addOnCompleteListener(new OnCompleteListener<Location>() {
                    @Override
                    public void onComplete(@NonNull Task<Location> task) {
                        new LocationResultListener().onComplete(locationResult);
                        if(onCompleteListener != null) onCompleteListener.onComplete(locationResult);
                    }
                });
                return locationResult;
            }
        } catch (SecurityException e)  {
            Log.e(TAG, "Error on getting device location: " + e.getMessage(), e);
        }

        return null;
    }

    public Task<Location> getDeviceLocation()
    {
        return getDeviceLocation(null);
    }

    /**
     * Prompts the user for permission to use the device location.
     */
    public void getLocationPermission()
    {
        /*
         * Request location permission, so that we can get the location of the
         * device. The result of the permission request is handled by a callback,
         * onRequestPermissionsResult.
         */
        if (ContextCompat.checkSelfPermission(activity.getApplicationContext(),
                android.Manifest.permission.ACCESS_FINE_LOCATION)
                == PackageManager.PERMISSION_GRANTED) {
            locationPermissionGranted = true;
        } else {
            ActivityCompat.requestPermissions(activity,
                    new String[]{android.Manifest.permission.ACCESS_FINE_LOCATION},
                    PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION);
        }
    }

    public Location getLastKnownLocation() {
        return lastKnownLocation;
    }
}
