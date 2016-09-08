package anddorua.shootdetector.utils;

/**
 * Created by andrey on 05.03.16.
 */
public class Calibrator {
    private double a;
    private double b;

    public Calibrator(double measuredPositiveCM, double measuredNegativeCM, double measuredMicDistanceCM) {
        recalibrate(measuredPositiveCM, measuredNegativeCM, measuredMicDistanceCM);
    }

    public synchronized void recalibrate(double measuredPositiveCM, double measuredNegativeCM, double measuredMicDistanceCM) {
        a = measuredMicDistanceCM * 2 / (measuredPositiveCM - measuredNegativeCM);
        b = (measuredMicDistanceCM - measuredPositiveCM * a) / 100d;
    }

    public synchronized double correct(double inputMeters) {
        return inputMeters * a + b;
    }
}
