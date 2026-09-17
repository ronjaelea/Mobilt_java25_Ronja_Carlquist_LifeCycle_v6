package com.gritacademy.draftlifecycle.steps;

import android.content.Context;
import android.content.SharedPreferences;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

/** stegräkning + datum för senaste nollställning, sparat i SharedPreferences
 * (lagrat på enheten, inte i Firebase) */
public class StepStore {

    public static final String PREFS = "steps";
    public static final String KEY_COUNT = "count";
    private static final String KEY_LAST_RESET = "last_reset_date";

    private final SharedPreferences prefs;

    /** samma SharedPrefs-fil som konstruktorn — så aktiviteten kan lyssna på ändringar. */
    public static SharedPreferences prefs(Context context) {
        // MODE_PRIVATE så att bara den här appen kan läsa filen
        return context.getSharedPreferences(PREFS, Context.MODE_PRIVATE);
    }

    public StepStore(Context context) {
        prefs = prefs(context);
    }

    public int getSteps() {
        return prefs.getInt(KEY_COUNT, 0);
    }

    public void setSteps(int steps) {
        prefs.edit().putInt(KEY_COUNT, steps).apply();
    }

    /**
     * nollställ om dagens datum != senast sparade
     * returnerar true om nollställning skedde
     */
    public boolean resetIfNewDay() {
        String today = today();
        if (!today.equals(prefs.getString(KEY_LAST_RESET, null))) {
            prefs.edit()
                    .putInt(KEY_COUNT, 0)
                    .putString(KEY_LAST_RESET, today)
                    .apply();
            return true;
        }
        return false;
    }

    /** manuell nollställning. stämplar datum så midnight-check inte triggar direkt efteråt */
    public void resetNow() {
        prefs.edit()
                .putInt(KEY_COUNT, 0)
                .putString(KEY_LAST_RESET, today())
                .apply();
    }

    private String today() {
        return new SimpleDateFormat("yyyy-MM-dd", Locale.US).format(new Date());
    }
}
