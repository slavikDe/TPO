package org.example;

import org.example.Sequential.Sequential;
import org.example.Stripe.StripedMatrixMultiplier;

public class Main {
    public static void main(String[] args) {
        try {
            int[] sizes = {512, 1024, 1536};
            int[] threads = {2, 4, 8, 12};

            for(int size : sizes){
                for (int tNum : threads) {
                    performanceTest(size, tNum);
                }
            }

        } catch (InterruptedException e) {
            System.err.println("Thread interrupted: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private static void performanceTest(int size, int threads) throws InterruptedException {
        System.out.println("\n\n=== Performance Test ===");
        System.out.format("\n=== Size %d ===", size);
        Matrix A = createRandomMatrix(size, size);
        Matrix B = createRandomMatrix(size, size);

        StripedMatrixMultiplier stripedMultiplier = new StripedMatrixMultiplier(64);

        // Sequential
        long start = System.currentTimeMillis();
        Matrix C1 = Sequential.multiply(A, B);
        long sequential = System.currentTimeMillis() - start;
        System.out.println("\nSequential: " + sequential + " ms");

        // Parallel
        start = System.currentTimeMillis();
        Matrix C2 = stripedMultiplier.multiplyParallel(A, B, threads);
        long parallel4 = System.currentTimeMillis() - start;
        System.out.format("Parallel (%d threads): " + parallel4 + " ms", threads);

        System.out.format("Speedup (%d threads): " + String.format("%.2f", (double)sequential / parallel4) + "x", threads);
    }

    private static Matrix createRandomMatrix(int rows, int cols) {
        Matrix matrix = new Matrix(rows, cols);
        for (int i = 0; i < rows; i++) {
            for (int j = 0; j < cols; j++) {
                matrix.set(i, j, Math.random() * 10);
            }
        }
        return matrix;
    }
}
