package lab8;

import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectInputStream;
import java.net.ServerSocket;
import java.net.Socket;

public class MatrixServerWithoutData {
    public static void main(String[] args) throws IOException, ClassNotFoundException {
        int PORT = 6789;
        ServerSocket serverSocket = new ServerSocket(PORT);
        System.out.println("Server started on port " + PORT);

        while (true) {
            Socket clientSocket = serverSocket.accept();
            System.out.println("Client connected");
            ObjectInput in = new ObjectInputStream(clientSocket.getInputStream());
            double[][] A = (double[][]) in.readObject();
            double[][] B = (double[][]) in.readObject();
            System.out.println("Server got data");

            new Thread(new MatrixWorker(clientSocket, A, B)).start();
            System.out.println("Client disconnected");
        }
    }
}
