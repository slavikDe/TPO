public class Main {
    public static void main(String[] args) {
        final int counterSize = 100_000;

        Counter counter = new Counter();

        long start = System.currentTimeMillis();
        Thread t1 = new Thread(() -> {
            for (int i = 0; i < counterSize; i++) {
//                counter.increment();
//                counter.syncIncrement();
//                counter.syncIncrementBlock();
                counter.syncIncrementLock();
            }
        });

        Thread t2 = new Thread(() -> {
            for (int i = 0; i < counterSize; i++) {
//                counter.decrement();
//                counter.syncDecrement();
//                counter.syncDecrementBlocK();
                counter.syncDecrementLock();
            }
        });

        t1.start();
        t2.start();

        try {
            t1.join();
            t2.join();
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
        long end = System.currentTimeMillis();
        System.out.println("Counter: " + counter.getCounter());
        System.out.println("Time: " + (end - start));
    }

}
