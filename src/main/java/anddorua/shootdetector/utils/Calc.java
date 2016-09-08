package anddorua.shootdetector.utils;

/**
 * Created by andrey on 07.03.16.
 */
public class Calc {
    public static double angle(double micDistanceM, double lagM) {
        if (lagM > micDistanceM) {
            lagM = micDistanceM;
        } else if(lagM < -micDistanceM) {
            lagM = -micDistanceM;
        }
        return Math.asin(lagM / micDistanceM);
    }
}
