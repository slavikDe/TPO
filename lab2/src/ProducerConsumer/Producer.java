package ProducerConsumer;

public class Producer implements Runnable {
    private Drop drop;
    private static final int SIZE = 5_000;

    public Producer(Drop drop) {
        this.drop = drop;
    }

    @Override
    public void run() {
        int[] importantInfo = new int[SIZE];
        for (int i = 0; i < SIZE; i++) {
            importantInfo[i] = i;
        }

        for (int i = 0;  i < SIZE; i++) {
            drop.put(importantInfo[i]);

        }
        drop.put(Producer.getSize());
    }

    static public int getSize() {
        return SIZE;
    }
}
