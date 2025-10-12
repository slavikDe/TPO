package org.example;

public class MatrixMultiplierTask implements Runnable {
    private final Matrix A;
    private final Matrix B;
    private final Matrix C;
    private final int startRow;
    private final int endRow;

    public MatrixMultiplierTask(Matrix A, Matrix B, Matrix C, int startRow, int endRow) {
        this.A = A;
        this.B = B;
        this.C = C;
        this.startRow = startRow;
        this.endRow = endRow;
    }

    @Override
    public void run() {
        int n = A.getCols();
        int p = B.getCols();

        // Process assigned rows
        for (int i = startRow; i < endRow; i++) {
            for (int j = 0; j < p; j++) {
                double sum = 0.0;
                for (int k = 0; k < n; k++) {
                    sum += A.get(i, k) * B.get(k, j);
                }
                C.set(i, j, sum);
            }
        }
    }
}