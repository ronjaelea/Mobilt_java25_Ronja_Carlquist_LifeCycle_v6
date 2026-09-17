package com.gritacademy.draftlifecycle.steps;

/**
 * räknar steg ur råa accelerometervärden
 */
public class StepDetector {

    private static final float GRAVITY_SMOOTHING = 0.05f; // långsamt: följer ~9.81
    private static final float SIGNAL_SMOOTHING = 0.5f; // lätt: behåller topparna
    private static final float PEAK_THRESHOLD = 1.5f; // m/s² över baslinjen för ett steg
    private static final float RESET_THRESHOLD = 0.7f; // måste dala under denna först
    private static final long MIN_STEP_INTERVAL_MS = 300; // max ~3 steg/s

    private float gravity;
    private float signal;
    private boolean initialized;
    private boolean aboveThreshold;
    private long lastStepMs;

    /** mata in sample. returnerar true exakt när ett steg upptäcks */
    public boolean onSample(float x, float y, float z, long nowMs) {
        double magnitude = Math.sqrt(x * x + y * y + z * z);

        if (!initialized) {
            gravity = (float) magnitude;
            signal = 0f;
            initialized = true;
            return false;
        }

        gravity += GRAVITY_SMOOTHING * (magnitude - gravity);
        float linear = (float) magnitude - gravity;
        signal += SIGNAL_SMOOTHING * (linear - signal);

        if (!aboveThreshold && signal > PEAK_THRESHOLD) {
            aboveThreshold = true;
            if (nowMs - lastStepMs >= MIN_STEP_INTERVAL_MS) {
                lastStepMs = nowMs;
                return true;
            }
        } else if (aboveThreshold && signal < RESET_THRESHOLD) {
            aboveThreshold = false;
        }
        return false;
    }

    /** nollställ filtrets tillstånd (efter manuell-/midnight reset) */
    public void reset() {
        initialized = false;
        aboveThreshold = false;
        lastStepMs = 0;
    }
}
