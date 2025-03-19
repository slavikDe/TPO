

import java.util.concurrent.BrokenBarrierException;
import java.util.concurrent.CyclicBarrier;

public class FoxMatrixMultiplication {

    public static void main(String[] args) {
        int size = 1000;
        long seed = 2704880631582L;
        int numThreads = 4;

        int[][] matrixA = Stripe.generateMatrixWithSeed(size, size, seed);
        int[][] matrixB = Stripe.generateMatrixWithSeed(size, size, seed);
        int[][] result = new int[size][size];

        for (int i = 0; i < size; i++) {
            for (int j = 0; j < size; j++) {
                result[i][j] = 0;
            }
        }

        long startTime = System.currentTimeMillis();
        multiplySequential(matrixA, matrixB, result);
        long endTime = System.currentTimeMillis();
        System.out.println("Sequential multiplication time: " + (endTime - startTime) + " ms");

        // reset matrix
        for (int i = 0; i < size; i++) {
            for (int j = 0; j < size; j++) {
                result[i][j] = 0;
            }
        }

        startTime = System.currentTimeMillis();
        multiplyWithFox(matrixA, matrixB, result, numThreads);
        endTime = System.currentTimeMillis();
        System.out.println("Fox multiplication time with " + numThreads + " threads: " + (endTime - startTime) + " ms");

        /**
         * compare results
         */
//        if (size <= 10) {
//            System.out.println("Matrix A:");
//            printMatrix(matrixA);
//            System.out.println("Matrix B:");
//            printMatrix(matrixB);
//            System.out.println("Result (Sequential):");
//            printMatrix(resultSequential);
//            System.out.println("Result (Fox):");
//            printMatrix(resultFox);
//        } else
//        {
//            boolean match = compareMatrices(resultSequential, resultFox);
//            System.out.println("Results match: " + match);
//
//            if (!match) {
//                int diffCount = 0;
//                System.out.println("Sample differences:");
//                for (int i = 0; i < size && diffCount < 5; i++) {
//                    for (int j = 0; j < size && diffCount < 5; j++) {
//                        if (resultSequential[i][j] != resultFox[i][j]) {
//                            System.out.println("At [" + i + "][" + j + "]: Sequential=" +
//                                    resultSequential[i][j] + ", Fox=" + resultFox[i][j]);
//                            diffCount++;
//                        }
//                    }
//                }
//            }
//        }

    }


    public static void multiplySequential(int[][] a, int[][] b, int[][] result) {
        int n = a.length;

        for (int i = 0; i < n; i++) {
            for (int j = 0; j < n; j++) {
                int sum = 0;
                for (int k = 0; k < n; k++) {
                    sum += a[i][k] * b[k][j];
                }
                result[i][j] = sum;
            }
        }
    }

    public static void multiplyWithFox(int[][] a, int[][] b, int[][] result, int numThreads) {
        int n = a.length;
        int gridSize = (int) Math.sqrt(numThreads);
        int blockSize = n / gridSize; // matrix length / sqrt numThreads

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
                workers[i][j] = new FoxWorker(
                        a, sharedB, result, i, j, gridSize, blockSize, barrier
                );
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

        public FoxWorker(int[][] a, int[][][][] sharedB, int[][] result,
                         int rowIdx, int colIdx, int gridSize,
                         int blockSize, CyclicBarrier barrier) {
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

    public static void printMatrix(int[][] matrix) {
        for (int[] row : matrix) {
            for (int val : row) {
                System.out.print(val + " ");
            }
            System.out.println();
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