
public class BallThread extends Thread {
    private Ball b;
    private BallCanvas canvas;
    private BallThread anotherThread;

    public BallThread(Ball ball, BallCanvas canvas, BallThread anotherThread){
        b = ball;
        this.canvas = canvas;
        this.anotherThread = anotherThread;
    }

    @Override
    public void run(){
        try{
            if (anotherThread != null){
                anotherThread.join();
            }
            while(b.move()){
                System.out.println("Thread name = " + Thread.currentThread().getName());
                Thread.sleep(3);
            }

            try {
                canvas.remove(b);
            }
            catch (Exception e){
                System.out.println("Ball already removed");
            }

        } catch(InterruptedException _){
        }
    }
}