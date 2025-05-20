package org.example;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;

public class MatrixClientWithoutData {
    public static void main(String[] args) throws IOException, ClassNotFoundException {
        Socket socket = new Socket(Config.HOST, Config.PORT);
        System.out.println("Connected to: " + socket.getRemoteSocketAddress());

        long startTime = System.currentTimeMillis();

        ObjectOutputStream out = new ObjectOutputStream(socket.getOutputStream());
        int size = 2500;
        System.out.println("Client sent size: " + size);
        out.writeInt(size);
        out.flush();


        ObjectInputStream in = new ObjectInputStream(socket.getInputStream());
        double[][] result = (double[][]) in.readObject();

        long endTime = System.currentTimeMillis();
        System.out.println("Client receive data in " + (endTime - startTime) + "ms, size: " + result.length);
        socket.close();
    }


}
