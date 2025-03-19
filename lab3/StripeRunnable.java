import java.util.concurrent.BlockingQueue;

public class StripeRunnable implements Runnable {

    private final int numThreads;
    private final int threadIndex;
    private final int[][] resAMatrix;
    private final int[][] result;
    int startRowIndex, indexDiff;

    private final BlockingQueue<int[][]>[] queues;

    StripeRunnable(int numThreads, int[][] resAMatrix, int[][] result, int startIndex, int endIndex, BlockingQueue<int[][]>[] queues, int threadIndex) {
        this.numThreads = numThreads;
        this.resAMatrix = resAMatrix;
        this.result = result;
        this.startRowIndex = startIndex;
        this.indexDiff = endIndex - startIndex;
        this.queues = queues;
        this.threadIndex = threadIndex;
    }

    @Override
    public void run() {
        int startColumnIndex = startRowIndex;
        try {
            for (int i = 0; i < numThreads; i++) {
                int[][] resBMatrix = queues[threadIndex].take();
                Stripe.multiplySequentialWithBounce(resAMatrix, resBMatrix, result, startRowIndex, startColumnIndex);

                startColumnIndex = Math.min((startColumnIndex + indexDiff), result.length);
                if (startColumnIndex == result.length) {
                    startColumnIndex = 0;
                }
                queues[(threadIndex + 1) % numThreads].put(resBMatrix);
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }

}
