package com.gritacademy.draftlifecycle.steps;

import android.content.Context;
import android.content.SharedPreferences;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

/** stegräkning + datum för senaste nollställning, sparat i SharedPreferences
 * (lagrat på enheten, inte i Firebase) */
public class StepStore {

    private static final String PREFS = "steps";
    private static final String KEY_COUNT = "count";
    private static final String KEY_LAST_RESET = "last_reset_date";

    private final SharedPreferences prefs;

    public StepStore(Context context) {
        // MODE_PRIVATE: bara den här appen kan läsa filen
        prefs = context.getSharedPreferences(PREFS, Context.MODE_PRIVATE);
    }

    public int getSteps() {
        return prefs.getInt(KEY_COUNT, 0);
    }

    public void setSteps(int steps) {
        prefs.edit().putInt(KEY_COUNT, steps).apply(); // apply() = skriv asynkront
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
