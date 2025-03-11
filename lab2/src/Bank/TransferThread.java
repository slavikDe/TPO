package Bank;

class TransferThread extends Thread {
    private Bank bank;
    private int fromAccount;
    private int initialMaxAmount;
    private static final int REPS = 10000;

    public TransferThread(Bank b, int from, int max){
        bank = b;
        fromAccount = from;
        initialMaxAmount = max;
    }

    @Override
    public void run(){
        for (int i = 0; i < REPS; i++) {
            int toAccount = (int) (bank.size() * Math.random());
            int amount = (int) (initialMaxAmount * 2 * 9* Math.random() / REPS);
            try {
                bank.transferSyncLock(fromAccount, toAccount, amount);
            } catch (InterruptedException _) {}
        }
    }
}