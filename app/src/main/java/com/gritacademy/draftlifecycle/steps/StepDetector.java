package com.gritacademy.draftlifecycle.steps;

/**
 * räknar steg ur råa accelerometervärden. ren Java, inga Android-beroenden,
 * så den går att enhetstesta
 *
 * algoritm:
 *   1. magnitud = |(x, y, z)|            (~9.81 i vila)
 *   2. långsamt filter spårar baslinjen (tyngdkraften)
 *   3. rörelse = magnitud - baslinje, lätt utjämnad
 *   4. ett steg = rörelsen korsar PEAK_THRESHOLD uppåt, måste sedan dala under
 *      RESET_THRESHOLD innan nästa (hysteres), och minst MIN_STEP_INTERVAL_MS
 *      sedan förra steget (debounce)
 *
 * justering om räkningen blir fel:
 *   - räknar för FÅ steg  -> sänk PEAK_THRESHOLD (t.ex. 1.5f)
 *   - räknar för MÅNGA    -> höj PEAK_THRESHOLD och/eller MIN_STEP_INTERVAL_MS
 */
public class StepDetector {

    private static final float GRAVITY_SMOOTHING = 0.05f;  // långsamt: följer ~9.81
    private static final float SIGNAL_SMOOTHING = 0.5f;     // lätt: behåller topparna
    private static final float PEAK_THRESHOLD = 2.0f;       // m/s² över baslinjen för ett steg
    private static final float RESET_THRESHOLD = 0.7f;      // måste dala under denna först
    private static final long MIN_STEP_INTERVAL_MS = 300;   // max ~3 steg/s

    private float gravity;
    private float signal;
    private boolean initialized;
    private boolean aboveThreshold;
    private long lastStepMs;

    /** mata in sample. returnerar true exakt när ett steg upptäcks */
    public boolean onSample(float x, float y, float z, long nowMs) {
        double magnitude = Math.sqrt(x * x + y * y + z * z);

        if (!initialized) {
            gravity = (float) magnitude; // starta baslinjen på första värdet
            signal = 0f;
            initialized = true;
            return false;
        }

        gravity += GRAVITY_SMOOTHING * (magnitude - gravity);
        float linear = (float) magnitude - gravity;          // rörelsen kring 0
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

    /** nollställ filtrets tillstånd (t.ex. efter en manuell/midnatts-nollställning) */
    public void reset() {
        initialized = false;
        aboveThreshold = false;
        lastStepMs = 0;
    }
}
