import java.awt.*;

public class BallThread extends Thread {
    private Ball b;
    private BallCanvas canvas;

    public BallThread(Ball ball, BallCanvas canvas){
        b = ball;
        this.canvas = canvas;
    }

    @Override
    public void run(){
        try{
            while(b.move()){
//                System.out.println("Thread name = "
//                        + Thread.currentThread().getName());

                Thread.sleep(5);
            }

            try {
                canvas.remove(b);
                canvas.repaint();
            }
            catch (Exception e){
                System.out.println("Ball already removed");
            }

        } catch(InterruptedException _){
        }
    }
}