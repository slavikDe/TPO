package lab5;

import java.util.Random;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;

public class MultiChannelSimulation {
    private static final int CONSUMER_COUNT = 8;
    private static final int QUEUE_SIZE = 40;
    private static final int CONSUMER_GENERATOR_MIN_TIME = 25;  // up to x1.5
    private static final int PRODUCER_GENERATOR_MIN_TIME = 200; // up to x2

    private static final int SIMULATION_TIME_MS = 10_000 * 6;

    private final AtomicInteger totalQueueLength = new AtomicInteger(0);
    private final AtomicInteger rejectedClients = new AtomicInteger(0);
    private final AtomicInteger totalClients = new AtomicInteger(0);
    AtomicInteger sumQueueSizes = new AtomicInteger(0);
    private final BlockingQueue<Consumer> queue = new LinkedBlockingQueue<>(QUEUE_SIZE);
    private final Random random = new Random();

    private class Consumer{}

    private void run() throws InterruptedException {
        ExecutorService servicePool = Executors.newFixedThreadPool(CONSUMER_COUNT);
        for(int i = 0; i < CONSUMER_COUNT; i++){
            servicePool.submit(()->{
                try{
                    while(!Thread.currentThread().isInterrupted()){
                        Consumer consumer = queue.poll();
                        if(consumer != null){
                            Thread.sleep(PRODUCER_GENERATOR_MIN_TIME + random.nextInt(PRODUCER_GENERATOR_MIN_TIME));
                        }
                    }
                } catch (InterruptedException ignored) {}
            });
        }

        Thread consumerGenerator = new Thread(()->{
            long start = System.currentTimeMillis();
            while(System.currentTimeMillis() - start < SIMULATION_TIME_MS){
                Consumer consumer = new Consumer();
                totalClients.incrementAndGet();
                if(!queue.offer(consumer)){
                    rejectedClients.incrementAndGet();
                }
                else{
                    totalQueueLength.incrementAndGet();
                }
                try{
                    Thread.sleep(CONSUMER_GENERATOR_MIN_TIME + random.nextInt(CONSUMER_GENERATOR_MIN_TIME / 2));
                } catch (InterruptedException ignored) {}
            }
        });

        // collect statistic
        final AtomicInteger NOfMeasurements = new AtomicInteger(0);
        ScheduledExecutorService scheduler = Executors.newSingleThreadScheduledExecutor();
        scheduler.scheduleAtFixedRate(()->{
            int currentSize = queue.size();
            NOfMeasurements.incrementAndGet();
            sumQueueSizes.addAndGet(currentSize);

            System.out.println("Thread: " + Thread.currentThread().getName());
            System.out.println("Number of measurements: " + NOfMeasurements.get());
            System.out.println("Current queue size: " + currentSize);
            System.out.println("Rejected consumers: " + rejectedClients.get());
            System.out.println("Total consumers: " + totalClients.get());
            System.out.println("-----------");
        }, 1, 1, TimeUnit.SECONDS);

        consumerGenerator.start();
        consumerGenerator.join();

        scheduler.shutdownNow();
        servicePool.shutdownNow();

        double avgQueueLength = (double) sumQueueSizes.get() / NOfMeasurements.get();
        double rejectionProbability = (double) rejectedClients.get() / totalClients.get();

        Thread.sleep(5_000); // wait other pools for clear output results

        System.out.println("\n=== Simulation Results ===");
        System.out.println("Thread: " + Thread.currentThread().getName());
        System.out.printf("Average queue length: %.2f\n", avgQueueLength);
        System.out.printf("Rejection probability: %.2f%%\n", rejectionProbability * 100);
    }

    public static void main(String[] args) throws InterruptedException {

        int pools = 6;
        CountDownLatch latch = new CountDownLatch(pools);
        ExecutorService servicePool = Executors.newFixedThreadPool(pools);
        for(int i = 0; i < pools; i++){
            servicePool.submit(()->{
                try {
                    new MultiChannelSimulation().run();
                }catch (Exception e){
                    e.printStackTrace();
                }
                finally {
                    latch.countDown();
                }
            });
        }

        latch.await();
        servicePool.shutdown();

        // solo
        new MultiChannelSimulation().run();
    }
}
