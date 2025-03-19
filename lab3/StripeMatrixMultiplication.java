import java.util.Random;
import java.util.concurrent.BrokenBarrierException;
import java.util.concurrent.CyclicBarrier;
import java.util.concurrent.TimeUnit;

public class StripeMatrixMultiplication {
    private static class MatrixMultiplyTask implements Runnable {
        private final int[][] matrixA;
        private final int[][] matrixB;
        private final int[][] result;
        private final int rowStart;
        private final int rowEnd;
        private final int totalCols;
        private final CyclicBarrier barrier;
        private int currentCol;

        public MatrixMultiplyTask(int[][] matrixA, int[][] matrixB, int[][] result, int rowStart, int rowEnd, int totalCols, CyclicBarrier barrier) {
            this.matrixA = matrixA;
            this.matrixB = matrixB;
            this.result = result;
            this.rowStart = rowStart;
            this.rowEnd = rowEnd;
            this.totalCols = totalCols;
            this.barrier = barrier;
            this.currentCol = 0;
        }

        @Override
        public void run() {
            try {
                for (int col = 0; col < totalCols; col++) {
                    for (int row = rowStart; row < rowEnd; row++) {
                        int sum = 0;
                        for (int k = 0; k < matrixB.length; k++) {
                            sum += matrixA[row][k] * matrixB[k][currentCol];
                        }
                        result[row][currentCol] = sum;
                    }

                    barrier.await();

                    if (rowStart == 0) {
                        currentCol = (currentCol + 1) % totalCols;
                    }

                    barrier.await();
                }
            } catch (InterruptedException | BrokenBarrierException e) {
                Thread.currentThread().interrupt();
                System.err.println("Thread interrupted: " + e.getMessage());
            }
        }
    }

    public static int[][] multiplyMatrices(int[][] matrixA, int[][] matrixB, int numThreads) {
        if (matrixA[0].length != matrixB.length) {
            throw new IllegalArgumentException("Invalid matrix dimensions for multiplication");
        }

        int rowsA = matrixA.length;
        int colsB = matrixB[0].length;
        int[][] result = new int[rowsA][colsB];

        CyclicBarrier barrier = new CyclicBarrier(numThreads);

        Thread[] threads = new Thread[numThreads];
        int rowsPerThread = rowsA / numThreads;

        for (int i = 0; i < numThreads; i++) {
            int startRow = i * rowsPerThread;
            int endRow = (i == numThreads - 1) ? rowsA : (i + 1) * rowsPerThread;

            MatrixMultiplyTask task = new MatrixMultiplyTask(matrixA, matrixB, result,
                    startRow, endRow, colsB, barrier);
            threads[i] = new Thread(task);
            threads[i].start();
        }

        for (Thread thread : threads) {
            try {
                thread.join();
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                System.err.println("Main thread interrupted: " + e.getMessage());
            }
        }

        return result;
    }

    public static void main(String[] args) {

        int[] sizes = {504, 1008, 1512, 2520, 3035};
        int[] threadCounts = {4, 6, 12};
        long seed = 2704880631582L;
        long startTime, endTime;
        int[][] result;


        int cycleCount = 4;
        for (int i = 1; i <= cycleCount; i++) {
            System.out.println("Starting cycle " + i + "----------------------------------------------------------------");

            for (int size : sizes) {
                int[][] matrixA = generateMatrixWithSeed(size, size, seed);
                int[][] matrixB = generateMatrixWithSeed(size, size, seed);

                System.out.println("Size: " + size + "==================================================================");
                startTime = System.nanoTime();
                result = multiplySequential(matrixA, matrixB);
                System.out.println("Sequential total time (ms): " + TimeUnit.NANOSECONDS.toMillis(System.nanoTime() - startTime));

                for (int numThreads : threadCounts) {
                    startTime = System.nanoTime();
                    result = multiplyMatrices(matrixA, matrixB, numThreads);
                    System.out.println("Parallel in " + numThreads +  " threads total time (ms): " + TimeUnit.NANOSECONDS.toMillis(System.nanoTime() - startTime));
                }
            }
        }
    }

    public static int[][] generateMatrixWithSeed(int rows, int cols, long seed) {
        int[][] matrix = new int[rows][cols];
        Random random = new Random(seed);

        for (int i = 0; i < rows; i++) {
            for (int j = 0; j < cols; j++) {
                matrix[i][j] = random.nextInt(2);
            }
        }

        return matrix;
    }

    public static int[][] multiplySequential(int[][] a, int[][] b) {
        int[][] result = new int[a.length][b[0].length];
        int rowsA = a.length;
        int colsA = a[0].length;
        int colsB = b[0].length;

        for (int i = 0; i < rowsA; i++) {
            for (int j = 0; j < colsB; j++) {
                int sum = 0;
                for (int k = 0; k < colsA; k++) {
                    sum += a[i][k] * b[k][j];
                }
                result[i][j] = sum;
            }
        }
        return result;
    }
}