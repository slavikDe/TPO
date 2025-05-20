package lab8;


import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

public class MatrixMultiplier {
    public static double[][] multiply(double[][] A, double[][] B) {
        int rows = A.length;
        int cols = B[0].length;
        int common = B.length;

        double[][] result = new double[rows][cols];
        final int numThreads = Runtime.getRuntime().availableProcessors();
        ExecutorService executor = Executors.newFixedThreadPool(numThreads);

        int rowsPerThread = (rows + numThreads - 1) / numThreads;  // ceil division

        for (int i = 0; i < numThreads; i++) {
            final int startRow = i * rowsPerThread;
            final int endRow = Math.min(startRow + rowsPerThread, rows);

            executor.execute(() -> {
                for (int j = startRow; j < endRow; j++) {
                    for(int c = 0; c < cols; c++) {
                        for (int k = 0; k < common; k++) {
                            result[j][c] += A[j][k] * B[k][c];
                        }
                    }
                }
            });
        }

        executor.shutdown();
        try {
            executor.awaitTermination(Long.MAX_VALUE, TimeUnit.MILLISECONDS);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        return result;
    }
}
