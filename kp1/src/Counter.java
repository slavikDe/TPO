import java.util.concurrent.locks.ReentrantLock;

public class Counter {

    private final ReentrantLock lock = new ReentrantLock();
    private int counter = 0;

    public void increment(){
        counter++;
    }

    public void decrement(){
        counter--;
    }

    public synchronized void syncIncrement(){
        counter++;
    }
    public synchronized void syncDecrement(){
        counter--;
    }
    public void syncIncrementBlock(){
       synchronized (lock){
           counter++;
       }
    }
    public void syncDecrementBlocK(){
        synchronized (lock){
            counter--;
        }
    }
    public void syncIncrementLock(){
        lock.lock();
        try {
            counter++;
        } finally {
            lock.unlock();
        }
    }
    public void syncDecrementLock(){
        lock.lock();
        try {
            counter--;
        } finally {
            lock.unlock();
        }
    }

    public int getCounter() {
        return counter;
    }
}
