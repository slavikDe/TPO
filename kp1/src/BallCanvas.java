import javax.swing.*;
import java.awt.*;
import java.util.ArrayList;

public class BallCanvas extends JPanel {

    private ArrayList<Ball> balls = new ArrayList<>();
    private static ArrayList<Pocket> pockets = new ArrayList<>();

    public int add(Ball b){
        this.balls.add(b);
        return balls.size();
    }

    public void add(Pocket p){
        pockets.add(p);
    }

    public synchronized void remove(Ball b){
        balls.remove(b);
    }

    public static ArrayList<Pocket> getPockets(){
        return pockets;
    }

    public int getBallCounts(){
        return balls.size();
    }

    @Override
    public  void paintComponent(Graphics g){
        super.paintComponent(g);

        Graphics2D g2 = (Graphics2D)g;

        try {
            for (int i = 0; i < balls.size(); i++){
                balls.get(i).draw(g2);
            }
        }
        catch(Exception _){
        }

        for (Pocket p : pockets) {
            p.draw(g2);

        }
    }
}