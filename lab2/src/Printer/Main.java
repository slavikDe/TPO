package Printer;

public class Main {
    public static void main(String[] args) {

        Thread first  = new Thread(new Printer('|', 0));
        Thread second = new Thread(new Printer('\\', 1));
        Thread third = new Thread(new Printer('/', 2));

        first.start();
        second.start();
        third.start();

    }
}
