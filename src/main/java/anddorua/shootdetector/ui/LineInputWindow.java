package anddorua.shootdetector.ui;

import anddorua.settings.xml.XmlSettingsStorage;
import anddorua.shootdetector.handlers.OnsetHandler;
import anddorua.shootdetector.ui.components.BearingGauge;
import anddorua.shootdetector.utils.Defaults;
import anddorua.shootdetector.utils.SettTag;
import anddorua.shootdetector.views.*;

import javax.swing.*;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.xml.transform.TransformerException;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Created by andrey on 23.01.16.
 */
public class LineInputWindow extends JFrame implements SoundGauge, TerminalView, anddorua.shootdetector.handlers.BearingGauge {
    private JPanel panel1;
    private JButton button1;
    private JTextField sampleRateField;
    private JTextField sampleSizeField;
    private JTextField bufferMSLengthField;
    private JProgressBar leftBar;
    private JProgressBar rightBar;
    private JTextArea textArea1;
    private JSlider thresholdSlider;
    private JSlider sensitivitySlider;
    private JTextField sampleDividerField;
    private JScrollPane messageScrollPane;
    private JPanel actionPanel;
    private JPanel soundValuePanel;
    private JPanel controlPanel;
    private JLabel statusText;
    private JPanel displayPanel;
    private JPanel bearingPanel;
    private JCheckBox saveSampleCB;
    private BearingGauge bearingGauge;
    private XmlSettingsStorage settingsStorage;
    private ChangeListener setingsChangeListener;
    private final Logger LOG = Logger.getLogger("LineInputWindow");

    public LineInputWindow(XmlSettingsStorage settingsStorage) throws HeadlessException {
        super("Sound value");
        JMenuBar mb = new JMenuBar();
        mb.add(makeFileMenu());
        setJMenuBar(mb);
        bearingGauge = makeBearingGauge();
        bearingPanel.add(bearingGauge, BorderLayout.CENTER);
        this.settingsStorage = settingsStorage;
        fetchSettings();
        setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
        setContentPane(panel1);
        new SmartScroller(messageScrollPane);
        setSettingsChangeListeners();
        addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                getThresholdModel().removeChangeListener(setingsChangeListener);
                getSensitivityModel().removeChangeListener(setingsChangeListener);
                super.windowClosing(e);
            }
        });
        outStatusLine();
        pack();
        setVisible(true);
    }

    private JMenu makeFileMenu() {
        JMenu menu = new JMenu("File");
        menu.add(new OptionsAction("Options ..."));
        menu.addSeparator();
        menu.add(new ExitAction("Exit"));
        return menu;
    }

    private BearingGauge makeBearingGauge() {
        final int h = 200;
        BearingGauge bg = new BearingGauge();
        bg.setPreferredSize(new Dimension(h * 2 + 1 + 8, h + 8));
        return bg;
    }

    class ExitAction extends AbstractAction {
        public ExitAction(String name) {
            super(name);
        }

        public void actionPerformed(ActionEvent e) {
            System.exit(0);
        }
    }

    class OptionsAction extends AbstractAction {
        public OptionsAction(String name) {
            super(name);
        }

        public void actionPerformed(ActionEvent e) {
            SystemParameters dialog = new SystemParameters(settingsStorage);
            dialog.pack();
            dialog.setVisible(true);
            outStatusLine();
        }
    }

    private void fetchSettings() {
        getThresholdModel().setValue(settingsStorage.getValueInt(SettTag.THRESHOLD, Defaults.THRESHOLD));
        getSensitivityModel().setValue(settingsStorage.getValueInt(SettTag.SENSITIVITY, Defaults.SENSITIVITY));
    }

    private void saveSettings() throws TransformerException {
        settingsStorage.setValue(SettTag.THRESHOLD, getThresholdModel().getValue());
        settingsStorage.setValue(SettTag.SENSITIVITY, getSensitivityModel().getValue());
        settingsStorage.save();
    }

    private void setSettingsChangeListeners() {
        setingsChangeListener = new ChangeListener() {
            public void stateChanged(ChangeEvent e) {
                try {
                    saveSettings();
                } catch (TransformerException ex) {
                    LOG.log(Level.ALL, "saving settings", ex);
                }
            }
        };
        getThresholdModel().addChangeListener(setingsChangeListener);
        getSensitivityModel().addChangeListener(setingsChangeListener);
    }

    synchronized public void setLeftChannelGauge(float value) {
        value = Math.min(100, value);
        leftBar.setValue((int) value);
    }

    synchronized public void setRightChannelGauge(float value) {
        value = Math.min(100, value);
        rightBar.setValue((int) value);
    }

    public void handleOnset(float timeStamp) {
        addTextMessage("Boom: " + timeStamp);
    }

    synchronized public void println(String s) {
        addTextMessage(s);
    }

    public void setButtonText(String text) {
        button1.setText(text);
    }

    public void addButtonListener(ActionListener listener) {
        button1.addActionListener(listener);
    }

    public BoundedRangeModel getThresholdModel() {
        return thresholdSlider.getModel();
    }

    public BoundedRangeModel getSensitivityModel() {
        return sensitivitySlider.getModel();
    }

    public ButtonModel getSavingModel(){
        return saveSampleCB.getModel();
    }

    public void addTextMessage(String msg) {
        textArea1.append("\n" + msg);
    }

    private void outStatusLine() {
        StringBuilder sb = new StringBuilder();
        sb.append("Sampling rate:");
        sb.append(settingsStorage.getValueInt(SettTag.SAMPLING_RATE, Defaults.SAMPLING_RATE));
        sb.append(" * ");
        sb.append(settingsStorage.getValueInt(SettTag.BITS_PER_SAMPLE, Defaults.BITS_PER_SAMPLE));
        sb.append("bps / ");
        sb.append(settingsStorage.getValueInt(SettTag.RATE_DIVIDER, Defaults.RATE_DIVIDER));
        sb.append("; search buffer:");
        sb.append(settingsStorage.getValueInt(SettTag.DETECTOR_BUFFER_SIZE, Defaults.DETECTOR_BUFFER_SIZE));
        sb.append("; mic distance:");
        sb.append(settingsStorage.getValueDouble(SettTag.MICROPHONE_DISTANCE, Defaults.MICROPHONE_DISTANCE));
        statusText.setText(sb.toString());
    }

    public synchronized void setBearing(double bearingRad, double bearingRangeMin, double bearingRangeMax) {
        bearingGauge.setBearing(bearingRad, bearingRangeMin, bearingRangeMax);
    }

    {
// GUI initializer generated by IntelliJ IDEA GUI Designer
// >>> IMPORTANT!! <<<
// DO NOT EDIT OR ADD ANY CODE HERE!
        $$$setupUI$$$();
    }

    /**
     * Method generated by IntelliJ IDEA GUI Designer
     * >>> IMPORTANT!! <<<
     * DO NOT edit this method OR call it in your code!
     *
     * @noinspection ALL
     */
    private void $$$setupUI$$$() {
        panel1 = new JPanel();
        panel1.setLayout(new BorderLayout(0, 0));
        panel1.setPreferredSize(new Dimension(600, 600));
        final JPanel panel2 = new JPanel();
        panel2.setLayout(new BorderLayout(0, 0));
        panel1.add(panel2, BorderLayout.NORTH);
        final JPanel panel3 = new JPanel();
        panel3.setLayout(new com.intellij.uiDesigner.core.GridLayoutManager(6, 2, new Insets(0, 0, 0, 0), -1, -1));
        panel2.add(panel3, BorderLayout.CENTER);
        final JLabel label1 = new JLabel();
        label1.setText("Sample rate:");
        panel3.add(label1, new com.intellij.uiDesigner.core.GridConstraints(0, 0, 1, 1, com.intellij.uiDesigner.core.GridConstraints.ANCHOR_WEST, com.intellij.uiDesigner.core.GridConstraints.FILL_NONE, com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_FIXED, com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        sampleRateField = new JTextField();
        sampleRateField.setText("96000");
        panel3.add(sampleRateField, new com.intellij.uiDesigner.core.GridConstraints(0, 1, 1, 1, com.intellij.uiDesigner.core.GridConstraints.ANCHOR_WEST, com.intellij.uiDesigner.core.GridConstraints.FILL_HORIZONTAL, com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_WANT_GROW, com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_FIXED, null, new Dimension(150, -1), null, 0, false));
        final JLabel label2 = new JLabel();
        label2.setText("Sample size in bits:");
        panel3.add(label2, new com.intellij.uiDesigner.core.GridConstraints(2, 0, 1, 1, com.intellij.uiDesigner.core.GridConstraints.ANCHOR_WEST, com.intellij.uiDesigner.core.GridConstraints.FILL_NONE, com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_FIXED, com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        sampleSizeField = new JTextField();
        sampleSizeField.setText("16");
        panel3.add(sampleSizeField, new com.intellij.uiDesigner.core.GridConstraints(2, 1, 1, 1, com.intellij.uiDesigner.core.GridConstraints.ANCHOR_WEST, com.intellij.uiDesigner.core.GridConstraints.FILL_HORIZONTAL, com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_WANT_GROW, com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_FIXED, null, new Dimension(150, -1), null, 0, false));
        final JLabel label3 = new JLabel();
        label3.setText("Buffer length, ms:");
        panel3.add(label3, new com.intellij.uiDesigner.core.GridConstraints(3, 0, 1, 1, com.intellij.uiDesigner.core.GridConstraints.ANCHOR_WEST, com.intellij.uiDesigner.core.GridConstraints.FILL_NONE, com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_FIXED, com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        bufferMSLengthField = new JTextField();
        bufferMSLengthField.setText("400");
        panel3.add(bufferMSLengthField, new com.intellij.uiDesigner.core.GridConstraints(3, 1, 1, 1, com.intellij.uiDesigner.core.GridConstraints.ANCHOR_WEST, com.intellij.uiDesigner.core.GridConstraints.FILL_HORIZONTAL, com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_WANT_GROW, com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_FIXED, null, new Dimension(150, -1), null, 0, false));
        final JLabel label4 = new JLabel();
        label4.setText("Threshold");
        panel3.add(label4, new com.intellij.uiDesigner.core.GridConstraints(4, 0, 1, 1, com.intellij.uiDesigner.core.GridConstraints.ANCHOR_WEST, com.intellij.uiDesigner.core.GridConstraints.FILL_NONE, com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_FIXED, com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        thresholdSlider = new JSlider();
        thresholdSlider.setMajorTickSpacing(5);
        thresholdSlider.setMaximum(20);
        thresholdSlider.setMinorTickSpacing(1);
        thresholdSlider.setPaintLabels(true);
        thresholdSlider.setPaintTicks(true);
        thresholdSlider.setSnapToTicks(true);
        panel3.add(thresholdSlider, new com.intellij.uiDesigner.core.GridConstraints(4, 1, 1, 1, com.intellij.uiDesigner.core.GridConstraints.ANCHOR_WEST, com.intellij.uiDesigner.core.GridConstraints.FILL_HORIZONTAL, com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_WANT_GROW, com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        final JLabel label5 = new JLabel();
        label5.setText("Sensitivity");
        panel3.add(label5, new com.intellij.uiDesigner.core.GridConstraints(5, 0, 1, 1, com.intellij.uiDesigner.core.GridConstraints.ANCHOR_WEST, com.intellij.uiDesigner.core.GridConstraints.FILL_NONE, com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_FIXED, com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        sensitivitySlider = new JSlider();
        sensitivitySlider.setMajorTickSpacing(10);
        sensitivitySlider.setMinorTickSpacing(1);
        sensitivitySlider.setPaintLabels(true);
        sensitivitySlider.setPaintTicks(false);
        sensitivitySlider.setSnapToTicks(true);
        panel3.add(sensitivitySlider, new com.intellij.uiDesigner.core.GridConstraints(5, 1, 1, 1, com.intellij.uiDesigner.core.GridConstraints.ANCHOR_WEST, com.intellij.uiDesigner.core.GridConstraints.FILL_HORIZONTAL, com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_WANT_GROW, com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        final JLabel label6 = new JLabel();
        label6.setText("Sample divider");
        panel3.add(label6, new com.intellij.uiDesigner.core.GridConstraints(1, 0, 1, 1, com.intellij.uiDesigner.core.GridConstraints.ANCHOR_WEST, com.intellij.uiDesigner.core.GridConstraints.FILL_NONE, com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_FIXED, com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        sampleDividerField = new JTextField();
        sampleDividerField.setText("2");
        panel3.add(sampleDividerField, new com.intellij.uiDesigner.core.GridConstraints(1, 1, 1, 1, com.intellij.uiDesigner.core.GridConstraints.ANCHOR_WEST, com.intellij.uiDesigner.core.GridConstraints.FILL_HORIZONTAL, com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_WANT_GROW, com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_FIXED, null, new Dimension(150, -1), null, 0, false));
        final JPanel panel4 = new JPanel();
        panel4.setLayout(new com.intellij.uiDesigner.core.GridLayoutManager(1, 1, new Insets(0, 0, 0, 0), -1, -1));
        panel2.add(panel4, BorderLayout.EAST);
        button1 = new JButton();
        button1.setText("Button");
        panel4.add(button1, new com.intellij.uiDesigner.core.GridConstraints(0, 0, 1, 1, com.intellij.uiDesigner.core.GridConstraints.ANCHOR_CENTER, com.intellij.uiDesigner.core.GridConstraints.FILL_HORIZONTAL, com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_CAN_SHRINK | com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_CAN_GROW, com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        final JPanel panel5 = new JPanel();
        panel5.setLayout(new com.intellij.uiDesigner.core.GridLayoutManager(1, 2, new Insets(0, 0, 0, 0), -1, -1));
        panel1.add(panel5, BorderLayout.SOUTH);
        final JLabel label7 = new JLabel();
        label7.setText("Sound value");
        panel5.add(label7, new com.intellij.uiDesigner.core.GridConstraints(0, 0, 1, 1, com.intellij.uiDesigner.core.GridConstraints.ANCHOR_WEST, com.intellij.uiDesigner.core.GridConstraints.FILL_NONE, com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_FIXED, com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        final JPanel panel6 = new JPanel();
        panel6.setLayout(new com.intellij.uiDesigner.core.GridLayoutManager(2, 1, new Insets(0, 0, 0, 0), -1, -1));
        panel5.add(panel6, new com.intellij.uiDesigner.core.GridConstraints(0, 1, 1, 1, com.intellij.uiDesigner.core.GridConstraints.ANCHOR_CENTER, com.intellij.uiDesigner.core.GridConstraints.FILL_BOTH, com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_CAN_SHRINK | com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_CAN_GROW, com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_CAN_SHRINK | com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_CAN_GROW, null, null, null, 0, false));
        leftBar = new JProgressBar();
        leftBar.setValue(0);
        panel6.add(leftBar, new com.intellij.uiDesigner.core.GridConstraints(0, 0, 1, 1, com.intellij.uiDesigner.core.GridConstraints.ANCHOR_CENTER, com.intellij.uiDesigner.core.GridConstraints.FILL_HORIZONTAL, com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_WANT_GROW, com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        rightBar = new JProgressBar();
        rightBar.setValue(0);
        panel6.add(rightBar, new com.intellij.uiDesigner.core.GridConstraints(1, 0, 1, 1, com.intellij.uiDesigner.core.GridConstraints.ANCHOR_CENTER, com.intellij.uiDesigner.core.GridConstraints.FILL_HORIZONTAL, com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_WANT_GROW, com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        messageScrollPane = new JScrollPane();
        panel1.add(messageScrollPane, BorderLayout.CENTER);
        textArea1 = new JTextArea();
        messageScrollPane.setViewportView(textArea1);
    }

    /**
     * @noinspection ALL
     */
    public JComponent $$$getRootComponent$$$() {
        return panel1;
    }
}
