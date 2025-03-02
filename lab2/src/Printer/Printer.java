package Printer;

public class Printer implements Runnable {
    static int counter = 0;
    private static final Object lock = new Object();

    private final char c;
    private final int myTurn;

    Printer(char c, int myTurn){
        this.c = c;
        this.myTurn = myTurn;
    }

    @Override
    public void run() {
        synchronized (lock) {
            for (; counter < 99 * 30 - 2; ) {
                while (counter % 3 != myTurn) {
                    try {
                        lock.wait();
                    } catch (InterruptedException _) {}
                }
                System.out.print(c);
                counter++;
                if (counter % 99 == 0 && counter != 0) {
                    System.out.println();
                }
                lock.notifyAll();
            }
        }
    }


}
