package com.ibm.casesdk.sample.nearbytasks.utils;

import android.app.Activity;
import android.content.Context;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.support.annotation.NonNull;
import android.text.TextUtils;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;

import com.ibm.casemanagersdk.sdk.interfaces.ICMTask;

import java.io.IOException;
import java.util.List;
import java.util.Locale;

/**
 * Created by stelian on 20/10/2015.
 */
public class Utils {

    private static final int TWO_MINUTES = 1000 * 60 * 2;

    public static View getContentView(Activity activity) {
        return activity.findViewById(android.R.id.content);
    }

    public static void showKeyboard(@NonNull Window window) {
        window.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_VISIBLE);
    }


    /**
     * Determines whether one Location reading is better than the current Location fix
     *
     * @param location            The new Location that you want to evaluate
     * @param currentBestLocation The current Location fix, to which you want to compare the new one
     */
    public static boolean isBetterLocation(Location location, Location currentBestLocation) {
        if (currentBestLocation == null) {
            // A new location is always better than no location
            return true;
        }

        // Check if the new location is  different from the old one
        double latDelta = location.getLatitude() - currentBestLocation.getLatitude();
        double longDelta = location.getLongitude() - currentBestLocation.getLongitude();
        boolean isDifferent = latDelta > 0.0 || longDelta > 0.0;

        if (!isDifferent) {
            return false;
        }

        // Check whether the new location fix is newer or older
        long timeDelta = location.getTime() - currentBestLocation.getTime();
        boolean isSignificantlyNewer = timeDelta > TWO_MINUTES;
        boolean isSignificantlyOlder = timeDelta < -TWO_MINUTES;
        boolean isNewer = timeDelta > 0;

        // If it's been more than two minutes since the current location, use the new location
        // because the user has likely moved
        if (isSignificantlyNewer) {
            return true;
            // If the new location is more than two minutes older, it must be worse
        } else if (isSignificantlyOlder) {
            return false;
        }

        // Check whether the new location fix is more or less accurate
        int accuracyDelta = (int) (location.getAccuracy() - currentBestLocation.getAccuracy());
        boolean isLessAccurate = accuracyDelta > 0;
        boolean isMoreAccurate = accuracyDelta < 0;
        boolean isSignificantlyLessAccurate = accuracyDelta > 200;

        // Check if the old and new location are from the same provider
        boolean isFromSameProvider = isSameProvider(location.getProvider(),
                currentBestLocation.getProvider());

        // Determine location quality using a combination of timeliness and accuracy
        if (isMoreAccurate) {
            return true;
        } /*else if (isNewer && !isLessAccurate) {
            return true;
        }*/ else if (isNewer && !isSignificantlyLessAccurate && isFromSameProvider) {
            return true;
        }
        return false;
    }

    /**
     * Checks whether two providers are the same
     */
    private static boolean isSameProvider(String provider1, String provider2) {
        if (provider1 == null) {
            return provider2 == null;
        }
        return provider1.equals(provider2);
    }

    /**
     * Check to see if the given {@link ICMTask} has latitude and longitude defined.
     *
     * @param task
     * @return
     */
    public static boolean hasLocation(@NonNull final ICMTask task) {
        return task.getLatitude() != 0.0f && task.getLongitude() != 0.0f;
    }

    public static String getAddressFromLocation(@NonNull Context context, @NonNull Location location) {
        final Geocoder geocoder = new Geocoder(context, Locale.getDefault());
        final StringBuilder builder = new StringBuilder();
        try {
            final List<Address> addresses = geocoder.getFromLocation(location.getLatitude(),
                    location.getLongitude(), 1); // Here 1 represent max location result to returned, by documents it recommended 1 to 5

            // If any additional address line present than only, check with max available address lines by getMaxAddressLineIndex()
            final String address = addresses.get(0).getAddressLine(0);
            if (addIfNotEmpty(builder, address)) {
                builder.append(", ");
            }

            final String city = addresses.get(0).getLocality();
            if (addIfNotEmpty(builder, city)) {
                builder.append(", ");
            }

            final String postalCode = addresses.get(0).getPostalCode();
            if (addIfNotEmpty(builder, postalCode)) {
                builder.append(", ");
            }

            final String state = addresses.get(0).getAdminArea();
            if (addIfNotEmpty(builder, state)) {
                builder.append(", ");
            }

            final String country = addresses.get(0).getCountryName();
            if (addIfNotEmpty(builder, country)) {
                builder.append(", ");
            }

//            final String knownName = addresses.get(0).getFeatureName();
//            addIfNotEmpty(builder, knownName);
        } catch (IOException e) {
            e.printStackTrace();
        }

        return builder.toString();
    }

    private static boolean addIfNotEmpty(StringBuilder builder, String stringToCheck) {
        if (!TextUtils.isEmpty(stringToCheck)) {
            builder.append(stringToCheck);
            return true;
        }

        return false;
    }
}
