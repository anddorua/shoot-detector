/**
 * Created by andrey on 24.02.16.
 */
package anddorua.shootdetector.app;


import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.TargetDataLine;
import javax.swing.*;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import java.io.ByteArrayOutputStream;
import java.nio.ByteBuffer;

import anddorua.settings.xml.XmlSettingsStorage;
import anddorua.shootdetector.handlers.BearingGauge;
import anddorua.shootdetector.models.DisplacementItem;
import anddorua.shootdetector.utils.*;
import be.tarsos.dsp.util.fft.FFT;
import anddorua.shootdetector.handlers.DisplacementHandler;
import anddorua.shootdetector.handlers.OnsetHandler;
import anddorua.shootdetector.processors.DisplacementFinder;
import anddorua.shootdetector.processors.PercussionDetector;
import anddorua.shootdetector.views.SoundGauge;
import anddorua.shootdetector.views.TerminalView;

import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.Line;
import javax.sound.sampled.TargetDataLine;
import javax.swing.*;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.text.DecimalFormat;
import java.util.List;

/**
 * Created by andrey on 23.01.16.
 */
public class LineConsumer extends Thread {
    private TargetDataLine line;
    private LineInputStorage lis;
    private AudioFormat format;
    private SoundGauge gauge;
    private OnsetHandler onsetHandler;
    private int sampleRateDivider;
    private ChangeListener sensitivityListener;
    private ChangeListener thresholdListener;
    private double sensitivity;
    private double threshold;
    private PercussionDetector detector = null;
    private volatile boolean stopRequire = false;


    public ChangeListener getSensitivityListener() {
        if (sensitivityListener == null) {
            sensitivityListener = new ChangeListener() {
                public void stateChanged(ChangeEvent e) {
                    detector.setSensitivity(((BoundedRangeModel)e.getSource()).getValue());
                }
            };
        }
        return sensitivityListener;
    }

    public ChangeListener getThresholdListener() {
        if (thresholdListener == null) {
            thresholdListener = new ChangeListener() {
                public void stateChanged(ChangeEvent e) {
                    detector.setThreshold(((BoundedRangeModel)e.getSource()).getValue());
                }
            };
        }
        return thresholdListener;
    }

    public LineConsumer(TargetDataLine line, LineInputStorage lis, double sensitivity, double threshold, int sampleRateDivider, SoundGauge gauge, OnsetHandler onsetHandler) {
        this.line = line;
        this.lis = lis;
        this.gauge = gauge;
        this.onsetHandler = onsetHandler;
        this.stopRequire = false;
        this.format = line.getFormat();
        this.sampleRateDivider = sampleRateDivider;
        this.sensitivity = sensitivity;
        this.threshold = threshold;
    }

    synchronized public void requestStop() {
        stopRequire = true;
    }

    @Override
    public void run() {
        float[] floatBuffer = new float[lis.testBufferLength / sampleRateDivider]; // используется для FFT-анализа наличия ударного звука
        detector = new PercussionDetector(format.getFrameRate() / sampleRateDivider, floatBuffer.length, onsetHandler, sensitivity, threshold);
        line.start();
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        byte[] buffer = new byte[lis.testBufferLength * format.getFrameSize()];
        lis.sampleBuffer.reset();
        lis.searchBuffer.reset();
        while (!stopRequire) {
            long nanoStart = System.nanoTime();
            int count = line.read(buffer, 0, buffer.length) / format.getFrameSize();
            long nanoRead = System.nanoTime();
            ByteBuffer bb = ByteBuffer.wrap(buffer);
            long powerL = 0;
            long powerR = 0;
            int pos = 0;
            int divStep = sampleRateDivider;
            while(bb.hasRemaining()) {
                short v = bb.getShort();
                lis.sampleBuffer.put(v);
                if (--divStep == 0) {
                    divStep = sampleRateDivider;
                    floatBuffer[pos++] = v;
                }
                powerL += v * v;
                v = bb.getShort();
                lis.searchBuffer.put(v);
                powerR += v * v;
            }
            double rmsL = Math.sqrt(powerL / count) / Short.MAX_VALUE;
            double rmsR = Math.sqrt(powerR / count) / Short.MAX_VALUE;
            long nanoCount = System.nanoTime();
            detector.process(floatBuffer);
            long nanoFFT =  System.nanoTime();
            //System.out.println("Frames count: " + count + ", read: " + (nanoRead - nanoStart) + ", counting: " + (nanoCount - nanoRead) + ", fft: " + (nanoFFT - nanoCount));
            gauge.setLeftChannelGauge((float)rmsL * 100);
            gauge.setRightChannelGauge((float)rmsR * 100);
            //System.out.println("L:" + rmsL + ", R:" + rmsR);
        }
        line.stop();
        line.flush();
        line.close();
        line = null;
    }
}
