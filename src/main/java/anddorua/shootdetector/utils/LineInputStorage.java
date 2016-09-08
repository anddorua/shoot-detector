package anddorua.shootdetector.utils;

/**
 * Created by andrey on 08.03.16.
 * хранит данные с входной линии
 *
 * testBufferLength - это количество сэмплов, принимаемое за один раз из входной линии.
 * Величина выбирается примерно из расчета характерной длительности звука удара.
 * Буфер должен хранить основной период удара, такой же период после удара и
 * примерно половину длительности перед ударом, т.к. при обнаружении удара
 * не всегда его фронт попадает в период удара, иногда он находится чуть раньше.
 * Детектор удара определяет факт удара по резкому затуханию звука, таким образом
 * звук удара хранится на один цикл ранее текущего цикла считывания сэмплов.
 */
public class LineInputStorage {
    public RoundBuffer sampleBuffer; // буфер для хранения ведущего канала
    public RoundBuffer searchBuffer; // второй канал, в нем ищется сэмпл из ведущего канала

    public final int testBufferLength;
    public final int testBufferPrefixLength;
    public final int framesToCompare; // общая длина буфера второго канала в которм будет вестись поиск
    public final int sampleDisplacement; // позиция начала образца относительно хвоста буфера
    public final int searchDisplacement; // позиция начала поля поиска относительно хвоста буфера второго канала

    public LineInputStorage(int testBufferLength, int testBufferPrefixLength, int sampleRate, double micDistanceCM, double micDistanceErrorCM, double soundVelocity) {
        this.testBufferLength = testBufferLength;
        this.testBufferPrefixLength = testBufferPrefixLength;
        final double possibleTimeShiftSec = (micDistanceCM + micDistanceErrorCM) / 100d / soundVelocity;
        final int sideBufferCount = (int)Math.ceil(possibleTimeShiftSec * sampleRate / (float)testBufferLength); // количество боковых буфферов, в которых может вестись поиск начала сигнала
        final int searchBufferLength = (sideBufferCount * 2 + 1) * testBufferLength; // длина буфера второго канала для поиска сэмпла из первого канала
        final int sideFramesToSearch = (int)Math.ceil(possibleTimeShiftSec * sampleRate); // теоретически возможный сдвиг сигнала в сэмплах
        framesToCompare = sideFramesToSearch * 2 + (testBufferPrefixLength + testBufferLength); // общая длина буфера второго канала в которм будет вестись поиск
        sampleDisplacement = testBufferLength * 2 + testBufferPrefixLength; // позиция начала образца относительно хвоста буфера
        searchDisplacement = testBufferLength * 2 + testBufferPrefixLength + sideFramesToSearch; // позиция начала поля поиска относительно хвоста буфера второго канала
        sampleBuffer = new RoundBuffer(new float[(int)(Math.ceil((float)sampleDisplacement / (float)testBufferLength) * testBufferLength)]);
        searchBuffer = new RoundBuffer(new float[searchBufferLength]);
    }
}
