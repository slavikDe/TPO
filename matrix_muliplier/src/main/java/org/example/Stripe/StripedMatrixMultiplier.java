package org.example.Stripe;

import org.example.Matrix;
import org.example.MatrixMultiplierTask;

import static org.example.Matrix.validateDimensions;

public class StripedMatrixMultiplier {
    private final int stripeSize;

    public StripedMatrixMultiplier(int stripeSize) {
        this.stripeSize = stripeSize > 0 ? stripeSize : 64;
    }

    public Matrix multiplyParallel(Matrix A, Matrix B, int numThreads) throws InterruptedException {
        validateDimensions(A, B);

        int m = A.getRows();
        int p = B.getCols();
        Matrix C = new Matrix(m, p);

        int rowsPerThread = (m + numThreads - 1) / numThreads;

        Thread[] threads = new Thread[numThreads];

        for (int t = 0; t < numThreads; t++) {
            int startRow = t * rowsPerThread;
            int endRow = Math.min(startRow + rowsPerThread, m);

            if (startRow < m) {
                threads[t] = new Thread(new MatrixMultiplierTask(A, B, C, startRow, endRow));
                threads[t].start();
            }
        }

        for (int t = 0; t < numThreads; t++) {
            if (threads[t] != null) {
                threads[t].join();
            }
        }

        return C;
    }


}