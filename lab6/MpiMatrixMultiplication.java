import mpi.*;

public class MpiMatrixMultiplication {
    static final int SIZE = 2048;
    static final int NRA = SIZE; // rows in A
    static final int NCA = SIZE; // cols in A
    static final int NCB = SIZE;  // cols in B
    static final int MASTER = 0;
    static final int FROM_MASTER = 1;
    static final int FROM_WORKER = 2;

    public static void main(String[] args) throws Exception {
        int numtasks, taskid, numworkers, source, dest, rows;
        int averow, extra, offset;
        int i, j, k;

        int[][] a = new int[NRA][NCA];
        int[][] b = new int[NCA][NCB];
        int[][] c = new int[NRA][NCB];

        MPI.Init(args);
        taskid = MPI.COMM_WORLD.Rank();
        numtasks = MPI.COMM_WORLD.Size();

        if (numtasks < 2) {
            System.out.println("Need at least two MPI tasks. Quitting...");
            MPI.COMM_WORLD.Abort(1);
            return;
        }
        else if(taskid == MASTER) {
            System.out.printf("Size: %d ms, processes: %d%n", SIZE, numtasks);
        }

        numworkers = numtasks - 1;

        final int cycles = 4;
        long[] startTime = new long[cycles], endTime = new long[cycles];

        for (int cycle = 0; cycle < cycles; cycle++) {
            startTime[cycle] = System.currentTimeMillis();

            if (taskid == MASTER) {
//            System.out.println("mpi_mm has started with " + numtasks + " tasks.");

                // Initialize matrices
                for (i = 0; i < NRA; i++)
                    for (j = 0; j < NCA; j++)
                        a[i][j] = 10;

                for (i = 0; i < NCA; i++)
                    for (j = 0; j < NCB; j++)
                        b[i][j] = 10;

                averow = NRA / numworkers;
                extra = NRA % numworkers;
                offset = 0;

                for (dest = 1; dest <= numworkers; dest++) {
                    rows = (dest <= extra) ? averow + 1 : averow;

//                System.out.println("Sending " + rows + " rows to task " + dest + " offset= " + offset);

                    MPI.COMM_WORLD.Send(new int[]{offset}, 0, 1, MPI.INT, dest, FROM_MASTER);
                    MPI.COMM_WORLD.Send(new int[]{rows}, 0, 1, MPI.INT, dest, FROM_MASTER);
                    MPI.COMM_WORLD.Send(flatten2D(a, offset, rows, NCA), 0, rows * NCA, MPI.INT, dest, FROM_MASTER);
                    MPI.COMM_WORLD.Send(flatten2D(b, 0, NCA, NCB), 0, NCA * NCB, MPI.INT, dest, FROM_MASTER);

                    offset += rows;
                }

                // Receive results
                for (source = 1; source <= numworkers; source++) {
                    int[] offsetArr = new int[1];
                    int[] rowsArr = new int[1];

                    MPI.COMM_WORLD.Recv(offsetArr, 0, 1, MPI.INT, source, FROM_WORKER);
                    MPI.COMM_WORLD.Recv(rowsArr, 0, 1, MPI.INT, source, FROM_WORKER);

                    int[] cPart = new int[rowsArr[0] * NCB];
                    MPI.COMM_WORLD.Recv(cPart, 0, rowsArr[0] * NCB, MPI.INT, source, FROM_WORKER);
                    unflatten2D(c, cPart, offsetArr[0], rowsArr[0], NCB);
                }

//             Print result matrix
//            System.out.println("****\nResult Matrix:");
//            for (i = 0; i < NRA; i++) {
//                for (j = 0; j < NCB; j++) {
//                    System.out.printf("%6.2f ", c[i][j]);
//                }
//                System.out.println();
//            }
//            System.out.println("********\nDone.");
            } else {
                int[] offsetArr = new int[1];
                int[] rowsArr = new int[1];

                MPI.COMM_WORLD.Recv(offsetArr, 0, 1, MPI.INT, MASTER, FROM_MASTER);
                MPI.COMM_WORLD.Recv(rowsArr, 0, 1, MPI.INT, MASTER, FROM_MASTER);

                int rowsReceived = rowsArr[0];
                int[] aPart = new int[rowsReceived * NCA];
                int[] bFlat = new int[NCA * NCB];

                MPI.COMM_WORLD.Recv(aPart, 0, rowsReceived * NCA, MPI.INT, MASTER, FROM_MASTER);
                MPI.COMM_WORLD.Recv(bFlat, 0, NCA * NCB, MPI.INT, MASTER, FROM_MASTER);

                int[][] aLocal = unflattenTo2D(aPart, rowsReceived, NCA);
                int[][] bLocal = unflattenTo2D(bFlat, NCA, NCB);
                int[][] cLocal = new int[rowsReceived][NCB];

                for (i = 0; i < rowsReceived; i++) {
                    for (k = 0; k < NCB; k++) {
                        for (j = 0; j < NCA; j++) {
                            cLocal[i][k] += aLocal[i][j] * bLocal[j][k];
                        }
                    }
                }

                MPI.COMM_WORLD.Send(offsetArr, 0, 1, MPI.INT, MASTER, FROM_WORKER);
                MPI.COMM_WORLD.Send(rowsArr, 0, 1, MPI.INT, MASTER, FROM_WORKER);
                MPI.COMM_WORLD.Send(flatten2D(cLocal, 0, rowsReceived, NCB), 0, rowsReceived * NCB, MPI.INT, MASTER, FROM_WORKER);
            }
            if (taskid == MASTER) {
                endTime[cycle] = System.currentTimeMillis();
                System.out.printf("Blocking time: %d ms%n", (endTime[cycle] - startTime[cycle]));
            }
        }

        MPI.Finalize();
    }

    // Helper to flatten a 2D array into 1D
    static int[] flatten2D(int[][] matrix, int rowStart, int rowCount, int cols) {
        int[] flat = new int[rowCount * cols];
        for (int i = 0; i < rowCount; i++)
            System.arraycopy(matrix[rowStart + i], 0, flat, i * cols, cols);
        return flat;
    }

    // Helper to unflatten into existing 2D array
    static void unflatten2D(int[][] matrix, int[] flat, int rowStart, int rowCount, int cols) {
        for (int i = 0; i < rowCount; i++)
            System.arraycopy(flat, i * cols, matrix[rowStart + i], 0, cols);
    }

    // Create new 2D from flat array
    static int[][] unflattenTo2D(int[] flat, int rows, int cols) {
        int[][] matrix = new int[rows][cols];
        for (int i = 0; i < rows; i++)
            System.arraycopy(flat, i * cols, matrix[i], 0, cols);
        return matrix;
    }
}
