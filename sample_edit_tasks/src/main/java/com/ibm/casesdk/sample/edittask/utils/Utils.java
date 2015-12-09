package com.ibm.casesdk.sample.edittask.utils;

import android.app.Activity;
import android.support.annotation.NonNull;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;

import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * Created by stelian on 26/10/2015.
 */
public class Utils {

    public static View getContentView(Activity activity) {
        return activity.findViewById(android.R.id.content);
    }

    public static void showKeyboard(@NonNull Window window) {
        window.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_VISIBLE);
    }

    public static String formatDate(Date time) {
        SimpleDateFormat formatter = new SimpleDateFormat("EEE, d MMM yyyy HH:mm Z");
        return formatter.format(time);
    }

    /**
     * Parse a {@link String} that contains a valid date with time zone.
     *
     * @param dateTime
     * @return
     */
    public static Date parseTimeZone(@NonNull String dateTime) {
        final SimpleDateFormat timeZoneParser = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSZ");
        Date parsed = null;

        try {
            parsed = timeZoneParser.parse(dateTime);
        } catch (Exception e) {
            e.printStackTrace();
        }

        return parsed;
    }

    public static String formatDateWithTimeZone(@NonNull Date dateTime) {
        final SimpleDateFormat timeZoneParser = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSZ");
        return timeZoneParser.format(dateTime);
    }
}
