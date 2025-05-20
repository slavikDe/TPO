package lab8;


import java.io.IOException;
import java.io.ObjectOutputStream;
import java.net.Socket;

class MatrixWorker implements Runnable {
    double [][] A, B;
    Socket socket;
    public MatrixWorker(Socket socket, double[][] A, double[][] B) {
        this.A = A;
        this.B = B;
        this.socket = socket;
    }

    @Override
    public void run() {
        try (ObjectOutputStream out = new ObjectOutputStream(socket.getOutputStream())) {

            double[][] result = MatrixMultiplier.multiply(A, B);

            out.writeObject(result);
            out.flush();
            System.out.println("Server sent data");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
