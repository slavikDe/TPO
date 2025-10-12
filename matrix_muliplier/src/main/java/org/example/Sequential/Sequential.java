package org.example.Sequential;

import org.example.Matrix;

import static org.example.Matrix.validateDimensions;

public class Sequential {


    public static Matrix multiply(Matrix A, Matrix B) {
        validateDimensions(A, B);

        int m = A.getRows();
        int n = A.getCols();
        int p = B.getCols();
        Matrix C = new Matrix(m, p);

        for (int i = 0; i < m; i++) {
            for (int j = 0; j < p; j++) {
                double sum = 0.0;
                for (int k = 0; k < n; k++) {
                    sum += A.get(i, k) * B.get(k, j);
                }
                C.set(i, j, sum);
            }
        }

        return C;
    }

}
