package anddorua.shootdetector.handlers;

import anddorua.shootdetector.models.DisplacementItem;
import anddorua.shootdetector.utils.Calc;
import anddorua.shootdetector.utils.Calibrator;
import anddorua.shootdetector.views.TerminalView;

import java.util.List;

/**
 * Created by andrey on 28.01.16.
 */
public class DisplacementHandler {
    private TerminalView terminal;
    private BearingGauge bearingGauge;
    private double micDistanceCM;
    private Calibrator cal;
    private float sampleRate;
    private double soundVelocity;
    private int displacementError;

    public DisplacementHandler(TerminalView terminal, BearingGauge bearingGauge, double micDistanceCM, Calibrator cal, float sampleRate, double soundVelocity, int displacementError) {
        this.terminal = terminal;
        this.bearingGauge = bearingGauge;
        this.micDistanceCM = micDistanceCM;
        this.cal = cal;
        this.sampleRate = sampleRate;
        this.soundVelocity = soundVelocity;
        this.displacementError = displacementError;
    }

    private double dist (int displacement) {
        return (double)displacement / (double)sampleRate * soundVelocity;
    }

    public synchronized void handle(int displacement) {
        double measured = dist(displacement);
        double corrected = cal.correct(measured);
        terminal.println("dist measured: " + (measured * 100d) + "  corrected:" + (corrected * 100d));
        double bearing = Calc.angle(micDistanceCM / 100d, corrected);
        double lagError = ((double)displacementError) / sampleRate * soundVelocity;
        double bearingMin = Calc.angle(micDistanceCM / 100d, corrected - lagError);
        double bearingMax = Calc.angle(micDistanceCM / 100d, corrected + lagError);
        bearingGauge.setBearing(bearing, bearingMin, bearingMax);
    }
}
