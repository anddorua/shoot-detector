package anddorua.shootdetector.models;

/**
 * Created by andrey on 28.02.16.
 */
public class DisplacementItem implements Comparable {
    private int pos;
    private double correlation;

    public DisplacementItem(int pos, double correlation) {
        this.pos = pos;
        this.correlation = correlation;
    }

    public int compareTo(Object o) {
        DisplacementItem other = (DisplacementItem) o;
        if (this == other || this.correlation == other.correlation) {
            return 0;
        } else if (this.correlation < other.correlation) {
            return -1;
        } else {
            return 1;
        }
    }

    public double getCorrelation() {
        return correlation;
    }

    public int getPos() {
        return pos;
    }

    public void setPos(int pos) {
        this.pos = pos;
    }

    public void setCorrelation(double correlation) {
        this.correlation = correlation;
    }
}
