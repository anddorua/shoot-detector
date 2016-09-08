/**
 * Created by andrey on 24.02.16.
 */
package anddorua.shootdetector.processors;


import be.tarsos.dsp.util.fft.FFT;
import anddorua.shootdetector.handlers.OnsetHandler;

/**
 * Created by andrey on 25.01.16.
 */
public class PercussionDetector {
    public static final double DEFAULT_THRESHOLD = 8;

    public static final double DEFAULT_SENSITIVITY = 90;

    private final FFT fft;

    private final float[] priorMagnitudes;
    private final float[] currentMagnitudes;

    private float dfMinus1, dfMinus2;

    private OnsetHandler handler;

    private final float sampleRate;//samples per second (Hz)
    private long processedSamples;//in samples

    public synchronized void setThreshold(double threshold) {
        this.threshold = threshold;
    }

    public synchronized void setSensitivity(double sensitivity) {

        this.sensitivity = sensitivity;
    }

    public synchronized double getThreshold() {
        return threshold;
    }

    public synchronized double getSensitivity() {

        return sensitivity;
    }

    /**
     * Sensitivity of peak detector applied to broadband detection function (%).
     * In [0-100].
     */
    private double sensitivity;

    /**
     * Energy rise within a frequency bin necessary to count toward broadband
     * total (dB). In [0-20].
     *
     */
    private double threshold;

    /**
     * Create a new percussion onset detector. With a default sensitivity and threshold.
     *
     * @param sampleRate
     *            The sample rate in Hz (used to calculate timestamps)
     * @param bufferSize
     *            The size of the buffer in samples.
     * @param bufferOverlap
     *            The overlap of buffers in samples.
     * @param handler
     *            An interface implementor to handle percussion onset events.
     */
    public PercussionDetector(float sampleRate, int bufferSize,
                              int bufferOverlap, OnsetHandler handler) {
        this(sampleRate, bufferSize, handler,
                DEFAULT_SENSITIVITY, DEFAULT_THRESHOLD);
    }

    /**
     * Create a new percussion onset detector.
     *
     * @param sampleRate
     *            The sample rate in Hz (used to calculate timestamps)
     * @param bufferSize
     *            The size of the buffer in samples.
     * @param handler
     *            An interface implementor to handle percussion onset events.
     * @param sensitivity
     *            Sensitivity of the peak detector applied to broadband
     *            detection function (%). In [0-100].
     * @param threshold
     *            Energy rise within a frequency bin necessary to count toward
     *            broadband total (dB). In [0-20].
     */
    public PercussionDetector(float sampleRate, int bufferSize, OnsetHandler handler, double sensitivity, double threshold) {
        fft = new FFT(bufferSize >> 1);
        this.setThreshold(threshold);
        this.setSensitivity(sensitivity);
        priorMagnitudes = new float[bufferSize >> 1];
        currentMagnitudes = new float[bufferSize >> 1];
        this.handler = handler;
        this.sampleRate = sampleRate;
    }

    public boolean process(float[] audioFloatBuffer) {
        this.processedSamples += audioFloatBuffer.length;

        fft.forwardTransform(audioFloatBuffer);
        fft.modulus(audioFloatBuffer, currentMagnitudes);
        int binsOverThreshold = 0;
        double threshold = this.getThreshold();
        for (int i = 0; i < currentMagnitudes.length; i++) {
            if (priorMagnitudes[i] > 0.f) {
                double diff = 10 * Math.log10(currentMagnitudes[i]
                        / priorMagnitudes[i]);
                if (diff >= threshold) {
                    binsOverThreshold++;
                }
            }
            priorMagnitudes[i] = currentMagnitudes[i];
        }

        if (dfMinus2 < dfMinus1
                && dfMinus1 >= binsOverThreshold
                && dfMinus1 > ((100 - this.getSensitivity()) * audioFloatBuffer.length) / 200) {
            float timeStamp = processedSamples / sampleRate;
            handler.handleOnset(timeStamp);
            //System.out.println("dfMinus2: " + dfMinus2 + ", dfMinus1: " + dfMinus1 + ", BOT: " + binsOverThreshold);
        }

        dfMinus2 = dfMinus1;
        dfMinus1 = binsOverThreshold;

        return true;
    }

    public void setHandler(OnsetHandler handler) {
        this.handler = handler;
    }
}
