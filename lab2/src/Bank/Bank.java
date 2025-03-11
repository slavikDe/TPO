package Bank;

import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

class Bank {
    public static final int NTEST = 10000;

    private final int[] accounts;
    private long ntransacts = 0;
    private Lock lock = new ReentrantLock();
    private final Condition sufficientFunds = lock.newCondition();

    public Bank(int n, int initialBalance){
        accounts = new int[n];
        int i;
        for (i = 0; i < accounts.length; i++)
            accounts[i] = initialBalance;
        ntransacts = 0;
    }

    public synchronized void transferSyncMethod(int from, int to, int amount) throws InterruptedException {
       accounts[from] -= amount;
       accounts[to] += amount;
       ntransacts++;

       if(ntransacts % NTEST == 0){
           test();
       }
    }

    public synchronized void transferSyncWait(int from, int to, int amount) throws InterruptedException {
        while(accounts[from] < amount){
            System.out.println("waiting");
            wait();
        }

        accounts[from] -= amount;
        accounts[to] += amount;
        ntransacts++;
        if (ntransacts % NTEST == 0) {
            test();
        }

        notify();
    }

    public void transferSyncLock(int from, int to, int amount) throws InterruptedException {
        lock.lock();
        while(accounts[from] < amount){
            System.out.println("waiting");
            sufficientFunds.await();
        }
        try {
            accounts[from] -= amount;
            accounts[to] += amount;
            ntransacts++;

            if (ntransacts % NTEST == 0) {
                test();
            }
            sufficientFunds.signalAll();
        }finally {
            this.lock.unlock();

        }
    }

    public void test(){
        int sum = 0;
        for (int i = 0; i < accounts.length; i++)
            sum += accounts[i] ;
        System.out.println("Transactions:" + ntransacts  + " Sum: " + sum);
    }
    public int size(){
        return accounts.length;
    }

}