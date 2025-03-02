package ProducerConsumer;

public class Drop {
    // Message sent from producer
    // to consumer.
    private int number;
    // True if consumer should wait
    // for producer to send message,
    // false if producer should wait for
    // consumer to retrieve message.
    private boolean empty = true;

    public synchronized int take() {
        // Wait until message is
        // available.
        while (empty) {
            try {
                wait();
            } catch (InterruptedException _) {}
        }
        // Toggle status.
        empty = true;
        // Notify producer that
        // status has changed.

        System.out.format("MESSAGE RECEIVED: %d%n", number);
        notifyAll();

        return number;
    }

    public synchronized void put(int number) {
        // Wait until message has
        // been retrieved.
        while (!empty) {
            try {
                wait();
            } catch (InterruptedException _) {}
        }
        // Toggle status.
        empty = false;
        // Store message.
        this.number = number;
        // Notify consumer that status
        // has changed.
        System.out.format("MESSAGE SEND: %d%n",  number);
        notifyAll();
    }
}