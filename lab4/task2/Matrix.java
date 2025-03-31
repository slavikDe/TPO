package TPO.lab4.task2;

public class Matrix {
    private int [][] matrix;
    private int NRows;
    private int NCols;


    public Matrix(int NRows, int NCols) {
        matrix = new int[NRows][NCols];
        this.NRows = NRows;
        this.NCols = NCols;
    }

    public Matrix(int NRows, int NCols, int value) {
        matrix = new int[NRows][NCols];
        this.NRows = NRows;
        this.NCols = NCols;
        this.fillWithValue(value);
    }

    public int getElement(int i, int j) {
        return matrix[i][j];
    }

    public void setElement(int i, int j, int value) {
        matrix[i][j] = value;
    }
    public void fillWithValue(int value){
        for (int i = 0; i < NRows; i++) {
            for (int j = 0; j < NCols; j++) {
                matrix[i][j] = value;
            }
        }
    }

    public void reset(){
        this.matrix = new int[NRows][NCols];
    }

    public Matrix getSubMatrixByColumns(int CStart, int CEnd){
        Matrix subMatrix = new Matrix(NRows, CEnd - CStart);
        for (int i = 0; i < NRows; i++) {
            for (int j = CStart; j < CEnd; j++) {
                subMatrix.matrix[i][j] = matrix[i][j];
            }
        }
        return subMatrix;
    }

    public Matrix getSubMatrixByRow(int RStart, int REnd){
        Matrix subMatrix = new Matrix(REnd - RStart, NCols);
        for (int i = RStart; i < REnd; i++) {
            for (int j = 0; j < NCols; j++) {
                subMatrix.matrix[i][j] = matrix[i][j];
            }
        }
        return subMatrix;
    }

    int getNRows(){return NRows;}
    int getNCols(){return NCols;}

    public static Matrix generateMatrix(int NRows, int NCols) {
        Matrix matrix = new Matrix(NRows, NCols);
        for (int i = 0; i < NRows; i++) {
            for (int j = 0; j < NCols; j++) {
                matrix.matrix[i][j] = 1;
            }
        }
        return matrix;
    }

    public void prettyPrint() {
        for (int[] row : matrix) {
            for (int val : row) {
                System.out.print(val + " ");
            }
            System.out.println();
        }
    }

    @Override
    public boolean equals(Object obj) {
        if (!(obj instanceof Matrix)) {
            return false;
        }
        Matrix other = (Matrix) obj;
        for (int i = 0; i < NRows; i++) {
            for (int j = 0; j < NCols; j++) {
                if (matrix[i][j] != other.matrix[i][j]) {
                    return false;
                }
            }
        }
        return true;
    }
}
