package utilities;

public class Batches {

    private final int elements;
    private final int batchSize;

    public Batches(int elements, int batchSize) {
        this.elements = elements;
        this.batchSize = batchSize;
    }

    public int getBeginIndex(int batchIndex) {
        if (batchIndex >= getBatches()) {
            throw new RuntimeException("Out of range batch.");
        }
        return batchIndex * batchSize;
    }

    public int getEndIndex(int batchIndex) {
        return Math.min(getBeginIndex(batchIndex) + batchSize - 1, elements - 1);
    }

    public int getBatches() {
        return (int) Math.ceil(elements / ((double) batchSize));
    }
}
