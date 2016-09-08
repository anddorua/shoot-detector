package anddorua.shootdetector.ui;

import anddorua.settings.xml.XmlSettingsStorage;
import anddorua.shootdetector.utils.Defaults;
import anddorua.shootdetector.utils.SettTag;

import javax.swing.*;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import java.awt.event.*;

public class SystemParameters extends JDialog {
    private JPanel contentPane;
    private JButton buttonOK;
    private JButton buttonCancel;
    private JTextField samplingRate;
    private JTextField rateDivider;
    private JTextField detectorBufferSize;
    private JTextField microphoneDistance;
    private JTextField measurementError;
    private JTextField soundVelocity;
    private JTextPane valueHint;
    private JTextField bitsPerSample;
    private JTextField audioBufferSize;
    private JTextField measuredLagMax;
    private JTextField measuredLagMin;
    private JTabbedPane optionsTabbedPane;
    private JTextField samplesDir;
    private JButton samplesDirBtn;
    private XmlSettingsStorage storage;

    public SystemParameters(XmlSettingsStorage storage) {
        this.storage = storage;
        fetchValuesFromStorage();
        setContentPane(contentPane);
        setModal(true);
        getRootPane().setDefaultButton(buttonOK);

        buttonOK.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                onOK();
            }
        });

        buttonCancel.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                onCancel();
            }
        });

        samplesDirBtn.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                onSamplesDirChoose();
            }
        });

// call onCancel() when cross is clicked
        setDefaultCloseOperation(DO_NOTHING_ON_CLOSE);
        addWindowListener(new WindowAdapter() {
            public void windowClosing(WindowEvent e) {
                onCancel();
            }
        });

// call onCancel() on ESCAPE
        contentPane.registerKeyboardAction(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                onCancel();
            }
        }, KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0), JComponent.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT);
    }

    private void fetchValuesFromStorage() {
        samplingRate.setText(Integer.toString(storage.getValueInt(SettTag.SAMPLING_RATE, Defaults.SAMPLING_RATE)));
        bitsPerSample.setText(Integer.toString(storage.getValueInt(SettTag.BITS_PER_SAMPLE, Defaults.BITS_PER_SAMPLE)));
        rateDivider.setText(Integer.toString(storage.getValueInt(SettTag.RATE_DIVIDER, Defaults.RATE_DIVIDER)));
        detectorBufferSize.setText(Integer.toString(storage.getValueInt(SettTag.DETECTOR_BUFFER_SIZE, Defaults.DETECTOR_BUFFER_SIZE)));
        microphoneDistance.setText(Double.toString(storage.getValueDouble(SettTag.MICROPHONE_DISTANCE, Defaults.MICROPHONE_DISTANCE)));
        measurementError.setText(Double.toString(storage.getValueDouble(SettTag.MEASUREMENT_ERROR, Defaults.MEASUREMENT_ERROR)));
        soundVelocity.setText(Double.toString(storage.getValueDouble(SettTag.SOUND_VELOCITY, Defaults.SOUND_VELOCITY)));
        audioBufferSize.setText(Integer.toString(storage.getValueInt(SettTag.AUDIO_BUFFER_SIZE, Defaults.AUDIO_BUFFER_SIZE)));
        measuredLagMax.setText(Double.toString(storage.getValueDouble(SettTag.MEASURED_POSITIVE, 0d)));
        measuredLagMin.setText(Double.toString(storage.getValueDouble(SettTag.MEASURED_NEGATIVE, 0d)));
        samplesDir.setText(storage.getValueString(SettTag.SAMPLES_DIR, ""));
    }

    private void putValuesToStorage() {
        storage.setValue(SettTag.SAMPLING_RATE, Integer.valueOf(samplingRate.getText()));
        storage.setValue(SettTag.BITS_PER_SAMPLE, Integer.valueOf(bitsPerSample.getText()));
        storage.setValue(SettTag.RATE_DIVIDER, Integer.valueOf(rateDivider.getText()));
        storage.setValue(SettTag.DETECTOR_BUFFER_SIZE, Integer.valueOf(detectorBufferSize.getText()));
        storage.setValue(SettTag.MICROPHONE_DISTANCE, Double.valueOf(microphoneDistance.getText()));
        storage.setValue(SettTag.MEASUREMENT_ERROR, Double.valueOf(measurementError.getText()));
        storage.setValue(SettTag.SOUND_VELOCITY, Double.valueOf(soundVelocity.getText()));
        storage.setValue(SettTag.AUDIO_BUFFER_SIZE, Integer.valueOf(audioBufferSize.getText()));
        storage.setValue(SettTag.MEASURED_POSITIVE, Double.valueOf(measuredLagMax.getText()));
        storage.setValue(SettTag.MEASURED_NEGATIVE, Double.valueOf(measuredLagMin.getText()));
        storage.setValue(SettTag.SAMPLES_DIR, samplesDir.getText());
    }

    private void onOK() {
        putValuesToStorage();
        try {
            storage.save();
            dispose();
        } catch (TransformerException ex) {
            JOptionPane.showMessageDialog(this, ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void onCancel() {
        dispose();
    }

    private void onSamplesDirChoose() {
        JFileChooser fileChooser = new JFileChooser();
        fileChooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
        fileChooser.setAcceptAllFileFilterUsed(false);

        int rVal = fileChooser.showOpenDialog(null);
        if (rVal == JFileChooser.APPROVE_OPTION) {
            samplesDir.setText(fileChooser.getSelectedFile().toString());
        }
    }
}
