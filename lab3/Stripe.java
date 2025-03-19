import java.util.Random;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

public class Stripe {

    public static void main(String[] args) {
//        int size = 2500;
//        int numThreads = 10;
        int[] sizes = {504, 1008, 1512, 2520,  3035};
        int[] threadCounts = {4, 6, 8};
        long seed = 2704880631582L;
        long startTime, endTime;


        for(int size : sizes){
            int[][] A = generateMatrixWithSeed(size, size, seed);
            int[][] B = generateMatrixWithSeed(size, size, seed);
            int[][] C = new int[size][size];

            // start test multiplySequential ----------------------------------------
            startTime = System.currentTimeMillis();
            multiplySequential(A, B, C);
            endTime = System.currentTimeMillis();
            // end test multiplySequential  ----------------------------------------

            System.out.println("SIZE: " + size + " ========================== ");
            System.out.println("Sequential multiplication time: " + (endTime - startTime) + " ms");

            for (int numThreads : threadCounts) {
               // start test multiplyStripe    ----------------------------------------
               startTime = System.currentTimeMillis();
               multiplyStripe(A, B, C, numThreads);
               endTime = System.currentTimeMillis();
               // end test multiplyStripe      ----------------------------------------

               System.out.println("Striped multiplication time with " + numThreads + " threads: " + (endTime - startTime) + " ms");
            }
        }
    }

    public static void multiplyStripe(int [][]A, int [][]B, int [][]result, int numThreads) {
        int rowsA = A.length;
        int colsA = A[0].length;
        int colsB = B[0].length;

        BlockingQueue<int[][]>[] queues = new BlockingQueue[numThreads];
        for (int i = 0; i < numThreads; i++) {
            queues[i] = new LinkedBlockingQueue<>();
        }

        int rowsPerThread = (int) Math.ceil((double) rowsA / numThreads);

        Thread[] threads = new Thread[numThreads];
        for(int i = 0; i < numThreads; i++) {
            final int startIndex = i * rowsPerThread;
            final int actualRowsForThread = Math.min(rowsPerThread, rowsA - startIndex);
            final int endIndex = startIndex + actualRowsForThread - 1;

            int[][] resAMatrix = new int[actualRowsForThread][colsA];
            int[][] resBMatrix = new int[colsA][colsB / numThreads + (colsB % numThreads > 0 ? 1 : 0)];

            for (int r = 0; r < actualRowsForThread; r++) {
                System.arraycopy(A[startIndex + r], 0, resAMatrix[r], 0, colsA);
            }
            copyMatrixByColumn(B, resBMatrix, startIndex);

            if (i == 0) {
                queues[i].offer(resBMatrix);
            }

            threads[i] = new Thread(new StripeRunnable(numThreads, resAMatrix, result, startIndex, endIndex, queues, i));
        }


        for(Thread thread : threads) {
            thread.start();
        }
        try{
            for(Thread thread : threads) {
                thread.join();
            }
        }catch(InterruptedException _){}
//        Stripe.printMatrix(result);
    }

    public static void multiplySequentialWithBounce(int[][] a, int[][] b, int[][] result, int startRow, int startColumn) {
        int rowsA = a.length;
        int colsA = a[0].length;
        int colsB = b[0].length;

        int maxColumns = result[0].length - startColumn;
        int columnsToProcess = Math.min(colsB, maxColumns);

        for (int i = 0; i < rowsA; i++) {
            for (int j = 0; j < columnsToProcess; j++) {
                int sum = 0;
                for (int k = 0; k < colsA; k++) {
                    sum += a[i][k] * b[k][j];
                }
                result[startRow + i][startColumn + j] = sum;
            }
        }
    }


    private static void copyMatrixByColumn(int[][] matrixFrom, int[][] matrixTo, int startColumnIndex) {
        int rows = matrixFrom.length;

        int colsFrom = matrixFrom[0].length;
        int colsTo = matrixTo[0].length;

        for (int i = 0; i < rows; i++) {
            for (int j = startColumnIndex, k = 0; j < colsFrom && k < colsTo; j++, k++) {
                matrixTo[i][k] = matrixFrom[i][j];
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

    public static synchronized void printMatrix(int[][] matrix) {
        for (int[] row : matrix) {
            for (int val : row) {
                System.out.print(val + "\t");
            }
            System.out.println();
        }
        System.out.println();
    }

    public static boolean areMatricesEqual(int[][] matrix1, int[][] matrix2) {
        if (matrix1 == null || matrix2 == null) {
            return false; // Якщо одна з матриць null
        }

        if (matrix1.length != matrix2.length || matrix1[0].length != matrix2[0].length) {
            return false; // Якщо розміри матриць різні
        }

        for (int i = 0; i < matrix1.length; i++) {
            for (int j = 0; j < matrix1[0].length; j++) {
                if (matrix1[i][j] != matrix2[i][j]) {
                    return false; // Якщо елементи різні
                }
            }
        }
        return true;
    }
}
