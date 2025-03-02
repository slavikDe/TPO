package ProducerConsumer;

import java.util.Random;

public class Consumer implements Runnable {
    private Drop drop;

    public Consumer(Drop drop) {
        this.drop = drop;
    }

    @Override
    public void run() {
//        Random random = new Random();
        for (int number = drop.take(); number != Producer.getSize();  number = drop.take()) {

//            try {
//                Thread.sleep(random.nextInt(5000));
//            } catch (InterruptedException _) {}
        }
    }
}