package anddorua.shootdetector.utils;

/**
 * Created by andrey on 25.02.16.
 */

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

public class SearchBufferLogger {
    public static void save(String fileName, double[] sample, double[] searchField, double[] mult) {
        BufferedWriter writer = null;
        try {
            File logFile = new File(fileName);
            writer = new BufferedWriter(new FileWriter(logFile));
            writer.write("\nSample");
            for (double v: sample) {
                writer.write("\n" + v);
            }
            writer.write("\n\nField");
            for (double v: searchField) {
                writer.write("\n" + v);
            }
            writer.write("\n\nConvolution");
            for (double v: mult) {
                writer.write("\n" + v);
            }
        } catch(IOException e) {
            e.printStackTrace();
        } finally {
            try {
                writer.close();
            } catch (Exception ec) {
                System.err.println("Cant close file");
            }
        }

    }

    public static void save(String fileName, float[] sample, float[] searchField, double[] mult) {
        BufferedWriter writer = null;
        try {
            File logFile = new File(fileName);
            writer = new BufferedWriter(new FileWriter(logFile));
            writer.write("\nSample");
            for (double v: sample) {
                writer.write("\n" + v);
            }
            writer.write("\n\nField");
            for (double v: searchField) {
                writer.write("\n" + v);
            }
            writer.write("\n\nConvolution");
            for (double v: mult) {
                writer.write("\n" + v);
            }
        } catch(IOException e) {
            e.printStackTrace();
        } finally {
            try {
                writer.close();
            } catch (Exception ec) {
                System.err.println("Cant close file");
            }
        }

    }
}
