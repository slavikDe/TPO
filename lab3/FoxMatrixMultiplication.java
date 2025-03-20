

import java.util.Arrays;
import java.util.concurrent.BrokenBarrierException;
import java.util.concurrent.CyclicBarrier;
import java.util.concurrent.TimeUnit;

public class FoxMatrixMultiplication {

    public static void main(String[] args) {
//        int size = 1000;
//        int numThreads = 4;
        int[] sizes = {504, 1008, 1512, 2520, 3034};
        int[] threadCounts = {4, 9, 16};
        long seed = 2704880631582L;
        long startTime;

        int[][] result;
        int[][] matrixA = StripeMatrixMultiplication.generateMatrixWithSeed(504, 504, seed);
        int[][] matrixB = StripeMatrixMultiplication.generateMatrixWithSeed(504, 504, seed);


        int cycleCount = 4;
        System.out.println("Warm up");
        startTime = System.nanoTime();
        result = multiplySequential(matrixA, matrixB);
        System.out.println("Sequential total time (ms): " + TimeUnit.NANOSECONDS.toMillis(System.nanoTime() - startTime));


        for(int c = 1; c <= cycleCount; c++) {
            System.out.println("Cycle #" + c);
            for(int size: sizes) {
                matrixA = StripeMatrixMultiplication.generateMatrixWithSeed(size, size, seed);
                matrixB = StripeMatrixMultiplication.generateMatrixWithSeed(size, size, seed);
                result = new int[size][size];

                System.out.println("Size: " + size + "==================================================================");

                for (int i = 0; i < size; i++) {
                    for (int j = 0; j < size; j++) {
                        result[i][j] = 0;
                    }
                }

                startTime = System.nanoTime();
                result = multiplySequential(matrixA, matrixB);
                System.out.println("Sequential multiplication time: " + TimeUnit.NANOSECONDS.toMillis(System.nanoTime() - startTime) + " ms");

                for(int numThreads: threadCounts) {
                    // reset matrix
                    for (int i = 0; i < size; i++) {
                        for (int j = 0; j < size; j++) {
                            result[i][j] = 0;
                        }
                    }

                    startTime = System.nanoTime();
                    multiplyWithFox(matrixA, matrixB, result, numThreads);
                    System.out.println("Fox multiplication time with " + numThreads + " threads: " + TimeUnit.NANOSECONDS.toMillis(System.nanoTime() - startTime) + " ms");
                }
            }
        }

    }


    public static int[][] multiplySequential(int[][] a, int[][] b) {
        int n = a.length;
        int[][] result = new int[n][b[0].length];
        for (int i = 0; i < n; i++) {
            for (int j = 0; j < n; j++) {
                int sum = 0;
                for (int k = 0; k < n; k++) {
                    sum += a[i][k] * b[k][j];
                }
                result[i][j] = sum;
            }
        }
        return result;
    }

    public static void multiplyWithFox(int[][] a, int[][] b, int[][] result, int numThreads) {
        int n = a.length;
        int gridSize = (int) Math.sqrt(numThreads);
        int blockSize = n / gridSize;

        int[][][][] sharedB = new int[gridSize][gridSize][blockSize][blockSize];

        for (int i = 0; i < gridSize; i++) {
            for (int j = 0; j < gridSize; j++) {
                for (int r = 0; r < blockSize; r++) {
                    for (int c = 0; c < blockSize; c++) {
                        sharedB[i][j][r][c] = b[i * blockSize + r][j * blockSize + c];
                    }
                }
            }
        }

        CyclicBarrier barrier = new CyclicBarrier(numThreads);

        Thread[][] threads = new Thread[gridSize][gridSize];
        FoxWorker[][] workers = new FoxWorker[gridSize][gridSize];

        for (int i = 0; i < gridSize; i++) {
            for (int j = 0; j < gridSize; j++) {
                workers[i][j] = new FoxWorker( a, sharedB, result, i, j, gridSize, blockSize, barrier  );
                threads[i][j] = new Thread(workers[i][j]);
                threads[i][j].start();
            }
        }

        try {
            for (int i = 0; i < gridSize; i++) {
                for (int j = 0; j < gridSize; j++) {
                    threads[i][j].join();
                }
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            System.err.println("Computation was interrupted: " + e.getMessage());
        }
    }

    private static class FoxWorker implements Runnable {
        private final int[][] a;
        private final int[][][][] sharedB;
        private final int[][] result;
        private final int rowIdx;
        private final int colIdx;
        private final int gridSize;
        private final int blockSize;
        private final CyclicBarrier barrier;

        private int[][] localA;
        private int[][] localC;

        public FoxWorker(int[][] a, int[][][][] sharedB, int[][] result,  int rowIdx, int colIdx, int gridSize, int blockSize, CyclicBarrier barrier) {
            this.a = a;
            this.sharedB = sharedB;
            this.result = result;
            this.rowIdx = rowIdx;
            this.colIdx = colIdx;
            this.gridSize = gridSize;
            this.blockSize = blockSize;
            this.barrier = barrier;

            this.localA = new int[blockSize][blockSize];
            this.localC = new int[blockSize][blockSize];

            for (int i = 0; i < blockSize; i++) {
                for (int j = 0; j < blockSize; j++) {
                    this.localC[i][j] = 0;
                }
            }
        }

        @Override
        public void run() {
            for (int stage = 0; stage < gridSize; stage++) {
                int sourceCol = (rowIdx + stage) % gridSize;
                for (int i = 0; i < blockSize; i++) {
                    for (int j = 0; j < blockSize; j++) {
                        localA[i][j] = a[rowIdx * blockSize + i][sourceCol * blockSize + j];
                    }
                }

                int bRowIdx = (rowIdx + gridSize - stage) % gridSize;

                multiplyAddBlocks(localA, sharedB[bRowIdx][colIdx], localC);

                try {
                    barrier.await();
                } catch (InterruptedException | BrokenBarrierException e) {
                    Thread.currentThread().interrupt();
                    return;
                }
            }

            for (int i = 0; i < blockSize; i++) {
                for (int j = 0; j < blockSize; j++) {
                    result[rowIdx * blockSize + i][colIdx * blockSize + j] = localC[i][j];
                }
            }
        }

        private void multiplyAddBlocks(int[][] a, int[][] b, int[][] c) {
            for (int i = 0; i < blockSize; i++) {
                for (int j = 0; j < blockSize; j++) {
                    for (int k = 0; k < blockSize; k++) {
                        c[i][j] += a[i][k] * b[k][j];
                    }
                }
            }
        }
    }

    public static boolean compareMatrices(int[][] a, int[][] b) {
        if (a.length != b.length || a[0].length != b[0].length) {
            return false;
        }

        for (int i = 0; i < a.length; i++) {
            for (int j = 0; j < a[0].length; j++) {
                if (a[i][j] != b[i][j]) {
                    System.out.println("Difference at [" + i + "][" + j + "]: " + a[i][j] + " vs " + b[i][j]);
                    return false;
                }
            }
        }

        return true;
    }
}