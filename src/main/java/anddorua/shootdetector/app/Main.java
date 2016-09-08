package anddorua.shootdetector.app;

import anddorua.settings.xml.XmlSettingsStorage;
import anddorua.shootdetector.handlers.DisplacementHandler;
import anddorua.shootdetector.handlers.OnsetHandler;
import anddorua.shootdetector.ui.LineInputWindow;
import anddorua.shootdetector.utils.Calibrator;
import anddorua.shootdetector.utils.Defaults;
import anddorua.shootdetector.utils.LineInputStorage;
import anddorua.shootdetector.utils.SettTag;
import org.xml.sax.SAXException;

import javax.sound.sampled.*;
import javax.xml.parsers.ParserConfigurationException;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Created by andrey on 23.02.16.
 */
public class Main {
    public static final Logger LOG = Logger.getLogger("ShootDetector");
    private static LineInputWindow win;
    private static LineConsumer monitor = null;
    private static XmlSettingsStorage settingStorage;

    public static void main(String[] args) {
        try {
            settingStorage = new XmlSettingsStorage("settings.xml");
            win = new LineInputWindow(settingStorage);
            win.setButtonText("Start");
            win.addButtonListener(new ActionListener() {
                public void actionPerformed(ActionEvent e) {
                    if (monitor == null) {
                        try {
                            startMonitor();
                            win.setButtonText("Stop");
                        } catch(LineUnavailableException eLine){
                            win.addTextMessage("Error line open:" + eLine.getMessage());
                        } catch(IllegalArgumentException eArg){
                            win.addTextMessage("Error argument:" + eArg.getMessage());
                        }
                    } else {
                        stopMonitor();
                        win.setButtonText("Start");
                    }
                }
            });
        } catch (ParserConfigurationException e) {
            LOG.log(Level.ALL, "ParserConfigurationException:", e);
        } catch (SAXException e) {
            LOG.log(Level.ALL, "SAXException:", e);
        } catch (IOException e) {
            LOG.log(Level.ALL, "IOException:", e);
        }
    }

    private static void startMonitor() throws LineUnavailableException{
        TargetDataLine line = openLine(
                getFormat(
                        settingStorage.getValueInt(SettTag.SAMPLING_RATE, Defaults.SAMPLING_RATE),
                        settingStorage.getValueInt(SettTag.BITS_PER_SAMPLE, Defaults.BITS_PER_SAMPLE)),
                settingStorage.getValueInt(SettTag.AUDIO_BUFFER_SIZE, Defaults.AUDIO_BUFFER_SIZE));
        win.addTextMessage(line.getFormat().toString());
        win.addTextMessage("Buffer size:" + line.getBufferSize());

        double soundVelocity = settingStorage.getValueDouble(SettTag.SOUND_VELOCITY, Defaults.SOUND_VELOCITY);
        int testBufferLength = settingStorage.getValueInt(SettTag.DETECTOR_BUFFER_SIZE, Defaults.DETECTOR_BUFFER_SIZE);
        LineInputStorage lis = new LineInputStorage(
                testBufferLength,
                testBufferLength / 2,
                (int)line.getFormat().getSampleRate(),
                settingStorage.getValueDouble(SettTag.MICROPHONE_DISTANCE, Defaults.MICROPHONE_DISTANCE),
                settingStorage.getValueDouble(SettTag.MEASUREMENT_ERROR, Defaults.MEASUREMENT_ERROR),
                soundVelocity
        );

        DisplacementHandler dh = new DisplacementHandler(
                win,
                win,
                settingStorage.getValueDouble(SettTag.MICROPHONE_DISTANCE, 0),
                makeCalibrator(settingStorage),
                line.getFormat().getSampleRate(),
                soundVelocity,
                1);

        monitor = new LineConsumer(
                line,
                lis,
                settingStorage.getValueDouble(SettTag.SENSITIVITY, Defaults.SENSITIVITY),
                settingStorage.getValueDouble(SettTag.THRESHOLD, Defaults.THRESHOLD),
                settingStorage.getValueInt(SettTag.RATE_DIVIDER, Defaults.RATE_DIVIDER),
                win,
                new OnsetHandler(lis, dh));
        win.getSensitivityModel().addChangeListener(monitor.getSensitivityListener());
        win.getThresholdModel().addChangeListener(monitor.getThresholdListener());
        monitor.start();
    }

    private static void stopMonitor() {
        monitor.requestStop();
        while (monitor.isAlive()) {
            try {
                Thread.sleep(100);                 //1000 milliseconds is one second.
            } catch(InterruptedException ex) {
                Thread.currentThread().interrupt();
            }
        }
        win.getSensitivityModel().removeChangeListener(monitor.getSensitivityListener());
        win.getThresholdModel().removeChangeListener(monitor.getThresholdListener());
        monitor = null;
    }

    private static Calibrator makeCalibrator(XmlSettingsStorage sett) {
        // setting calibrator
        double mPos = sett.getValueDouble(SettTag.MEASURED_POSITIVE, 0);
        double mNeg = sett.getValueDouble(SettTag.MEASURED_NEGATIVE, 0);
        double micDistanceCM = sett.getValueDouble(SettTag.MICROPHONE_DISTANCE, 0);
        return new Calibrator(mPos == 0d ? micDistanceCM : mPos, mNeg == 0d ? -micDistanceCM : mNeg, micDistanceCM);
    }

    private static AudioFormat getFormat(float sampleRate, int sampleSizeInBits) {
        int channels = 2;          //Стерео звук
        boolean signed = true;     //Флаг указывает на то, используются ли числа со знаком или без
        boolean bigEndian = true;  //Флаг указывает на то, следует ли использовать обратный (big-endian) или прямой (little-endian) порядок байтов
        return new AudioFormat(sampleRate, sampleSizeInBits, channels, signed, bigEndian);
    }

    private static TargetDataLine openLine(AudioFormat format, int bufferCapacityMSec) throws LineUnavailableException, IllegalArgumentException {
        DataLine.Info info = new DataLine.Info(TargetDataLine.class, format);
        TargetDataLine line = (TargetDataLine) AudioSystem.getLine(info);
        AudioFormat actualFormat = line.getFormat();
        int bufferSize = ((int)(actualFormat.getSampleRate() * bufferCapacityMSec / 1000)) * actualFormat.getFrameSize();
        line.open(format, bufferSize);
        return line;
    }


}
