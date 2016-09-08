package anddorua.shootdetector.handlers;

import anddorua.shootdetector.processors.DisplacementFinder;
import anddorua.shootdetector.utils.LineInputStorage;

/**
 * Created by andrey on 25.01.16.
 */
public class OnsetHandler {
    private LineInputStorage lis;
    private DisplacementHandler dh;

    public OnsetHandler(LineInputStorage lis, DisplacementHandler dh) {
        this.lis = lis;
        this.dh = dh;
    }

    public void handleOnset(float timeStamp) {
        new Thread(
            new DisplacementFinder(
                    lis.sampleBuffer.fetch(new float[lis.testBufferLength + lis.testBufferPrefixLength], lis.sampleDisplacement),
                    lis.searchBuffer.fetch(new float[lis.framesToCompare], lis.searchDisplacement),
                    dh
            ))
        .start();
    }

}
