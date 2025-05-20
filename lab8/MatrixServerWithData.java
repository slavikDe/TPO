package lab8;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.net.ServerSocket;
import java.net.Socket;

public class MatrixServerWithData {
    public static void main(String[] args) throws IOException {
        int PORT = 6789;
        ServerSocket serverSocket = new ServerSocket(PORT);
        System.out.println("Server started on port: " + PORT);

        while (true) {
            Socket clientSocket = serverSocket.accept();
            System.out.println("Client connected");

            ObjectInputStream in = new ObjectInputStream(clientSocket.getInputStream());
            int size = in.readInt();
            double[][] A = generateMatrix(size);
            double[][] B = generateMatrix(size);

            new Thread(new MatrixWorker(clientSocket, A, B)).start();
        }
    }

    public static double[][] generateMatrix(int n) {
        double[][] A = new double[n][n];
        for (int i = 0; i < n; i++) {
            for (int j = 0; j < n; j++) {
                A[i][j] = Math.random();
            }
        }
        return A;
    }
}
