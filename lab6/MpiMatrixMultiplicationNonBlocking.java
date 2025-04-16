import mpi.*;

public class MpiMatrixMultiplicationNonBlocking {
    static final int SIZE = 3072;
    static final int NRA = SIZE;
    static final int NCA = SIZE;
    static final int NCB = SIZE;
    static final int MASTER = 0;
    static final int FROM_MASTER = 1;
    static final int FROM_WORKER = 2;

    public static void main(String[] args) throws Exception {
        int taskid, numtasks;

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
        else if (taskid == MASTER) {
            System.out.printf("Size: %d ms, processes: %d%n", SIZE, numtasks);
        }

        int numworkers = numtasks - 1;

        long[] startTime = new long[numtasks], endTime = new long[numtasks];
        final int cycles = 4;

        for (int cycle = 0; cycle < cycles; cycle++) {
            startTime[cycle] = System.currentTimeMillis();
            if (taskid == MASTER) {
                // ініціалізація
                for (int i = 0; i < NRA; i++)
                    for (int j = 0; j < NCA; j++)
                        a[i][j] = 10;
                for (int i = 0; i < NCA; i++)
                    for (int j = 0; j < NCB; j++)
                        b[i][j] = 10;

                int averow = NRA / numworkers;
                int extra = NRA % numworkers;
                int offset = 0;

                // масиви для всіх запитів
                Request[] sendRequests = new Request[numworkers * 4];
                int reqIdx = 0;

                for (int dest = 1; dest <= numworkers; dest++) {
                    int rows = (dest <= extra) ? averow + 1 : averow;

                    int[] aPart = flatten2D(a, offset, rows, NCA);
                    int[] bFlat = flatten2D(b, 0, NCA, NCB);

                    sendRequests[reqIdx++] = MPI.COMM_WORLD.Isend(new int[]{offset}, 0, 1, MPI.INT, dest, FROM_MASTER);
                    sendRequests[reqIdx++] = MPI.COMM_WORLD.Isend(new int[]{rows}, 0, 1, MPI.INT, dest, FROM_MASTER);
                    sendRequests[reqIdx++] = MPI.COMM_WORLD.Isend(aPart, 0, rows * NCA, MPI.INT, dest, FROM_MASTER);
                    sendRequests[reqIdx++] = MPI.COMM_WORLD.Isend(bFlat, 0, NCA * NCB, MPI.INT, dest, FROM_MASTER);

                    offset += rows;
                }

                Request.Waitall(sendRequests);

                // Прийом результатів
                Request[] recvRequests = new Request[numworkers * 3];
                int[][] offsets = new int[numworkers][1];
                int[][] rowsArr = new int[numworkers][1];
                int[][] results = new int[numworkers][];

                reqIdx = 0;
                for (int source = 1; source <= numworkers; source++) {
                    int index = source - 1;
                    results[index] = new int[(averow + 1) * NCB]; // макс розмір

                    recvRequests[reqIdx++] = MPI.COMM_WORLD.Irecv(offsets[index], 0, 1, MPI.INT, source, FROM_WORKER);
                    recvRequests[reqIdx++] = MPI.COMM_WORLD.Irecv(rowsArr[index], 0, 1, MPI.INT, source, FROM_WORKER);
                    recvRequests[reqIdx++] = MPI.COMM_WORLD.Irecv(results[index], 0, results[index].length, MPI.INT, source, FROM_WORKER);
                }

                Request.Waitall(recvRequests);

                // Запис у фінальний масив
                for (int i = 0; i < numworkers; i++) {
                    unflatten2D(c, results[i], offsets[i][0], rowsArr[i][0], NCB);
//                System.out.println("Received results from task " + (i + 1));
                }

            } else {
                // Worker
                int[] offsetArr = new int[1];
                int[] rowsArr = new int[1];

                int[] aPart, bFlat;

                Request[] recvRequests = new Request[4];
                recvRequests[0] = MPI.COMM_WORLD.Irecv(offsetArr, 0, 1, MPI.INT, MASTER, FROM_MASTER);
                recvRequests[1] = MPI.COMM_WORLD.Irecv(rowsArr, 0, 1, MPI.INT, MASTER, FROM_MASTER);

                // Чекаємо на перші 2 recv, щоб знати скільки рядків і яка матриця
                Request.Waitall(new Request[]{recvRequests[0], recvRequests[1]});

                int rows = rowsArr[0];
                aPart = new int[rows * NCA];
                bFlat = new int[NCA * NCB];

                recvRequests[2] = MPI.COMM_WORLD.Irecv(aPart, 0, rows * NCA, MPI.INT, MASTER, FROM_MASTER);
                recvRequests[3] = MPI.COMM_WORLD.Irecv(bFlat, 0, NCA * NCB, MPI.INT, MASTER, FROM_MASTER);

                Request.Waitall(new Request[]{recvRequests[2], recvRequests[3]});

                // Обчислення
                int[][] aLocal = unflattenTo2D(aPart, rows, NCA);
                int[][] bLocal = unflattenTo2D(bFlat, NCA, NCB);
                int[][] cLocal = new int[rows][NCB];

                for (int i = 0; i < rows; i++)
                    for (int k = 0; k < NCB; k++)
                        for (int j = 0; j < NCA; j++)
                            cLocal[i][k] += aLocal[i][j] * bLocal[j][k];

                // Надсилаємо результат
                int[] cPart = flatten2D(cLocal, 0, rows, NCB);
                Request[] sendRequests = new Request[3];
                sendRequests[0] = MPI.COMM_WORLD.Isend(offsetArr, 0, 1, MPI.INT, MASTER, FROM_WORKER);
                sendRequests[1] = MPI.COMM_WORLD.Isend(rowsArr, 0, 1, MPI.INT, MASTER, FROM_WORKER);
                sendRequests[2] = MPI.COMM_WORLD.Isend(cPart, 0, cPart.length, MPI.INT, MASTER, FROM_WORKER);

                Request.Waitall(sendRequests);
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
