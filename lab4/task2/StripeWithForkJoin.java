package TPO.lab4.task2;

import java.util.concurrent.ForkJoinPool;
import java.util.concurrent.RecursiveAction;

public class StripeWithForkJoin  {
    private static final int THRESHOLD = 64;

    public static void main(String[] args) {
        final int[] sizes =  {512, 1024, 1536};
//        final int[] sizes =  {512, 1024, 2048, 3072};
        final int repeatCount = 4;

        long startTime;
        long stopTime;
        long[] forkJoinTime = new long[sizes.length];
        long[] singleThreadTimes = new long[sizes.length];

        ForkJoinPool fjp = new ForkJoinPool();

        // for warm up
        Matrix A = new Matrix(10, 10, 1);
        Matrix B = new Matrix(10, 10, 1);
        Matrix R = new Matrix(A.getNRows(), B.getNRows());
        Matrix RFJ;

        System.out.println("Start warm up");
        multiplySequential(A, B, R);
        System.out.println("End warm up");

        for(int i = 0; i< repeatCount; i++) {
            for (int size : sizes){
                A = new Matrix(size, size, 1);
                B = new Matrix(size, size, 1);
                R = new Matrix(A.getNRows(), B.getNCols());
                RFJ = new Matrix(A.getNRows(), B.getNCols());

                startTime = System.currentTimeMillis();
                multiplySequential(A, B, R);
                stopTime = System.currentTimeMillis();

                singleThreadTimes[i] = stopTime - startTime;
                System.out.println("Sequential with size " + size + ", takes " + singleThreadTimes[i] + " ms");

                startTime = System.currentTimeMillis();
                MatrixMultiplyTask task = new MatrixMultiplyTask(A, B, RFJ, 0, A.getNRows(), 0, B.getNCols());
                fjp.invoke(task);
                stopTime = System.currentTimeMillis();

                forkJoinTime[i] = stopTime - startTime;
                System.out.println("ForkJoin with size " + size + ", takes " + forkJoinTime[i] + " ms");

                if (R.equals(RFJ)) {
                    System.out.println("Matrix equals!\n");
                } else {
                    throw new RuntimeException("Matrix not equals!");
                }
            }
        }
        fjp.shutdown();

        System.out.println("\nBenchmark end\n");
        System.out.println("Single\tForkJoin");
        for(int i=0; i< sizes.length; i++) {
            System.out.println(sizes[i] + " " + singleThreadTimes[i] + "\t" + forkJoinTime[i]);
        }
    }



    private static class MatrixMultiplyTask extends RecursiveAction {
        private final Matrix A, B, R;
        private final int rowStart, colStart;
        private final int rowEnd, colEnd;

        MatrixMultiplyTask(Matrix a, Matrix b, Matrix r, int rowStart, int rowEnd, int colStart, int colEnd) {
            this.A = a;
            this.B = b;
            this.R = r;
            this.rowStart = rowStart;
            this.colStart = colStart;
            this.rowEnd = rowEnd;
            this.colEnd = colEnd;
        }

        @Override
        protected void compute() {
            if(rowEnd - rowStart <= THRESHOLD){
                multiplySequentially();
            }
            else{
                int rowMid = (rowEnd + rowStart) / 2;
                int colMid = (colEnd + colStart) / 2;

                invokeAll(
                        new MatrixMultiplyTask(A, B, R, rowStart, rowMid, colStart, colMid),
                        new MatrixMultiplyTask(A, B, R, rowMid, rowEnd, colStart, colMid),
                        new MatrixMultiplyTask(A, B, R, rowStart, rowMid, colMid, colEnd),
                        new MatrixMultiplyTask(A, B, R, rowMid, rowEnd, colMid, colEnd)
                );
            }
        }

        private void multiplySequentially() {
            for (int i = rowStart; i < rowEnd; i++) {
                for (int j = colStart; j < colEnd; j++) {
                    int sum = 0;
                    for (int k = 0; k < B.getNRows(); k++) {
                        sum += A.getElement(i, k) * B.getElement(k, j);
                    }
                    R.setElement(i, j, sum);
                }
            }
        }
    }

    public static void multiplySequential(Matrix a, Matrix b, Matrix R) {
        int rowsA = a.getNRows();
        int colsA = a.getNCols();
        int colsB = b.getNCols();

        for (int i = 0; i < rowsA; i++) {
            for (int j = 0; j < colsB; j++) {
                int sum = 0;
                for (int k = 0; k < colsA; k++) {
                    sum += a.getElement(i, k) * b.getElement(k, j);
                }
                R.setElement(i, j, sum);
            }
        }
    }
}
