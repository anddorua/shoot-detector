package anddorua.shootdetector.utils;

/**
 * Круговой буфер, заполняет наиболее старые позиции
 * Created by andrey on 27.01.16.
 */
public class RoundBuffer {
    private float[] buffer;
    private int pos;
    private long processed;
    private long processedSince;

    public RoundBuffer(float[] buffer) {
        this.buffer = buffer;
        pos = 0;
        processed = 0;
        processedSince = 0;
    }

    public void reset() {
        pos = 0;
        processed = 0;
        processedSince = System.currentTimeMillis();
    }

    public void put(float value) {
        if (pos == buffer.length) {
            pos = 0;
        }
        buffer[pos++] = value;
        processed++;
    }

    public long getProcessed() {
        return processed;
    }

    public long getProcessedSince() {
        return processedSince;
    }

    /**
     * Копирует последовательность length значений в dst начиная с -displacement
     * length не должна быть больше buffer.length
     * @param dst целевой массив куда копируются данные
     * @param dstPos позиция в целевом массиве для начала копирования
     * @param displacement смещение относительно pos влево в буфере откуда копируются данные
     * @param length длина копирования
     */
    public float[] fetch(float[] dst, int dstPos, int displacement, int length) {
        int beginIndex = pos - displacement;
        if (beginIndex < 0) {
            beginIndex = buffer.length + beginIndex;
        }
        int firstCopyLength = length;
        int secondCopyLength = 0;
        if (beginIndex + firstCopyLength > buffer.length) {
            firstCopyLength = buffer.length - beginIndex;
            secondCopyLength = length - firstCopyLength;
        }
        if (firstCopyLength > 0) {
            System.arraycopy(buffer, beginIndex, dst, dstPos, firstCopyLength);
            dstPos += firstCopyLength;
            beginIndex = 0;
        }
        if (secondCopyLength > 0) {
            System.arraycopy(buffer, beginIndex, dst, dstPos, secondCopyLength);
        }
        return dst;
    }

    /**
     * Копирует из буфера данные в массив dst начиная с -displacement
     * @param dst целевой массив куда копируются данные
     * @param displacement смещение относительно pos влево в буфере откуда копируются данные
     */
    public float[] fetch(float[] dst, int displacement) {
        return this.fetch(dst, 0, displacement, dst.length);
    }
}
