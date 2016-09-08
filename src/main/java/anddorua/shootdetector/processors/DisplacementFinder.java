
/**
 * Created by andrey on 24.02.16.
 */

package anddorua.shootdetector.processors;

import java.io.*;
import java.text.NumberFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.List;

import anddorua.shootdetector.models.DisplacementItem;
import anddorua.shootdetector.utils.MaximumCollector;
import anddorua.shootdetector.utils.SearchBufferLogger;

import anddorua.shootdetector.handlers.DisplacementHandler;

public class DisplacementFinder implements Runnable {
    private float[] sampleFloat;
    private float[] searchFieldFloat;
    private DisplacementHandler handler;
    private final int dispToCount = 2;
    private final int localRange = 5;

    public DisplacementFinder(float[] sampleFloat, float[] searchFieldFloat, DisplacementHandler handler) {
        this.sampleFloat = sampleFloat;
        this.searchFieldFloat = searchFieldFloat;
        this.handler = handler;
    }

    public void run() {
        double[] sample = new double[sampleFloat.length];
        double[] searchField = new double[searchFieldFloat.length];
        int multCount = searchField.length - sample.length + 1;
        double[] mult = new double[multCount];
        MaximumCollector collector = new MaximumCollector(dispToCount, localRange);
        double sqSample = 0;
        filterSobel(sampleFloat, sample);
        filterSobel(searchFieldFloat, searchField);
        for (double v: sample){
            sqSample += v * v;
        }
        double sqSearch = 0;

        for (int i = 0; i < multCount; i++) {
            if (i == 0) {
                for (int j = 0; j < sample.length; j++) {
                    sqSearch += searchField[j] * searchField[j];
                }
            } else {
                sqSearch += (searchField[sample.length + i - 1] * searchField[sample.length + i - 1]) - (searchField[i - 1] * searchField[i - 1]);
            }
            mult[i] = convolution(sample, searchField, i) / Math.sqrt(sqSample * sqSearch);
            collector.match(i, mult[i]);
        }

        int zeroDisplacement = (searchFieldFloat.length - sampleFloat.length) / 2;
        handler.handle(zeroDisplacement - collector.getMaximum().get(0).getPos());
        //saveSample(sampleFloat, searchFieldFloat, mult);
    }

    private void saveSample(float[] sample, float[] searchField, double[] disp) {
        File f = new File("sample.txt");
        try {
            BufferedWriter fw = new BufferedWriter(new FileWriter(f));
            fw.write("Sample");
            for (float value: sample) {
                fw.write("\n");
                fw.write(NumberFormat.getInstance().format(value));
            }
            fw.write("\n\nSearch");
            for (float value: searchField) {
                fw.write("\n");
                fw.write(NumberFormat.getInstance().format(value));
            }
            fw.write("\n\nDispersion");
            for (double value: disp) {
                fw.write("\n");
                fw.write(NumberFormat.getInstance().format(value));
            }
            fw.close();
        } catch(FileNotFoundException e) {
            System.err.println("file write:" + e.getMessage());
        } catch(IOException e) {
            System.err.println("IO error:" + e.getMessage());
        }
    }

    private int findFirstMaximumDisplacement() {
        int dispSample = findFirstMaximum(sampleFloat);
        int dispSearch = findFirstMaximum(searchFieldFloat);
        return dispSearch - dispSample;
    }

    private int findFirstMaximum(float[] field) {
        int pos = 0;
        for (int i = 1; i < field.length; i++) {
            if (field[i] > field[pos]) {
                pos = i;
            }
        }
        return pos;
    }

    private void filterSobel(float[] data, double[] res){
        for (int i = 0; i < data.length; i++) {
            res[i] = data[i] * data[i];
        }
        double v0 = res[0];
        double v1 = res[1];
        double v2 = res[2];
        res[0] =  -2f * v0 + 2f * v1;
        double sum = res[0];
        for (int i = 1; i < res.length - 1; i++) {
            res[i] = -2f * v0 + 2f * v2;
            sum += res[i];
            v0 = v1;
            v1 = v2;
            v2 = res[i+1];
        }
        res[res.length - 1] =  -2f * v1 + 2f * v2;
        sum += res[res.length - 1];
        sum /= res.length;
        for (int i = 0; i < res.length; i++) {
            res[i] = res[i] - sum;
        }
    }

    private double convolution(double[] a, double[] b, int displacement) {
        float result = 0;
        for (int i = 0; i < a.length; i++) {
            result += a[i] * b[displacement++];
        }
        return result;
    }

}
