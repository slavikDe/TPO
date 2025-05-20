package org.example;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;

public class MatrixClientWithData {
    public static void main(String[] args) throws IOException, ClassNotFoundException {
        Socket socket = new Socket(Config.HOST, Config.PORT);
        System.out.println("Connected to: " + socket.getRemoteSocketAddress());

        long startTime = System.currentTimeMillis();

        int n = 2500;
        double[][] A = generateMatrix(n);
        double[][] B = generateMatrix(n);

        System.out.println("Client send data");

        ObjectOutputStream out = new ObjectOutputStream(socket.getOutputStream());
        out.writeObject(A);
        out.writeObject(B);
        out.flush();

        System.out.println("Data sent");

        ObjectInputStream in = new ObjectInputStream(socket.getInputStream());
        double[][] result = (double[][]) in.readObject();

        long endTime = System.currentTimeMillis();

        System.out.println("Client receive data in " + (endTime - startTime) + "ms, size: " + result.length);
        socket.close();
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
