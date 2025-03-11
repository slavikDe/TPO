import java.util.Random;

public class StripedMatrixMultiplication {

    public static void main(String[] args) {
//        int size = 1000;
        int[] sizes = {500, 1000, 1500, 2000, 2500, 3000};
        int[] threadCounts = {4, 9, 25};

        long seed = 2704880631582L;
        for (int size : sizes) {
            int[][] matrixA = generateRandomMatrixWithSeed(size, size, seed);
            int[][] matrixB = generateRandomMatrixWithSeed(size, size, seed);
            int[][] result = new int[size][size];
            long startTime = System.currentTimeMillis();
            multiplySequential(matrixA, matrixB, result);
            long endTime = System.currentTimeMillis();
            System.out.println("SIZE: " + size + " ========================== ");
            System.out.println("Sequential multiplication time: " + (endTime - startTime) + " ms");

            for (int numThreads : threadCounts) {
                result = new int[size][size];

                startTime = System.currentTimeMillis();
                multiplyWithStripesWaitNotify(matrixA, matrixB, result, numThreads);
                endTime = System.currentTimeMillis();
                System.out.println("Striped multiplication time with " + numThreads + " threads: " + (endTime - startTime) + " ms");
            }
        }
    }

    public static void multiplySequential(int[][] a, int[][] b, int[][] result) {
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
    }

    public static void multiplyWithStripesWaitNotify(int[][] a, int[][] b, int[][] result, int numThreads) {
        int rowsA = a.length;

        int stripeSize = (int) Math.ceil((double) rowsA / numThreads);

        final Object lock = new Object();
        final int[] completedThreads = {0};

        for (int stripe = 0; stripe < numThreads; stripe++) {
            final int startRow = stripe * stripeSize;
            final int endRow = Math.min(startRow + stripeSize, rowsA);

            if (startRow < rowsA) {
                Thread thread = new Thread(() -> {
                    int colsA = a[0].length;
                    int colsB = b[0].length;

                    for (int i = startRow; i < endRow; i++) {
                        for (int j = 0; j < colsB; j++) {
                            int sum = 0;
                            for (int k = 0; k < colsA; k++) {
                                sum += a[i][k] * b[k][j];
                            }
                            result[i][j] = sum;
                        }
                    }

                    synchronized (lock) {
                        completedThreads[0]++;
                        if (completedThreads[0] >= numThreads) {
                            lock.notify();
                        }
                    }
                });
                thread.start();
            } else {
                synchronized (lock) {
                    completedThreads[0]++;
                }
            }
        }

        synchronized (lock) {
            try {
                if (completedThreads[0] < numThreads) {
                    lock.wait();
                }
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                System.err.println("Wait interrupted: " + e.getMessage());
            }
        }
    }

    public static int[][] generateRandomMatrixWithSeed(int rows, int cols, long seed) {
        int[][] matrix = new int[rows][cols];
        Random random = new Random(seed);

        for (int i = 0; i < rows; i++) {
            for (int j = 0; j < cols; j++) {
                matrix[i][j] = random.nextInt(10);
            }
        }

        return matrix;
    }

    public static void printMatrix(int[][] matrix) {
        for (int[] row : matrix) {
            for (int val : row) {
                System.out.print(val + " ");
            }
            System.out.println();
        }
    }
}