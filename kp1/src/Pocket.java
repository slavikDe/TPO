import java.awt.*;
import java.awt.geom.Ellipse2D;
import java.util.Random;


public class Pocket {
    private Component c;

    private static final int pocketRadius = 20;

    private int x;
    private int y;

    Pocket(Component c) {
        this.c = c;
        x = new Random().nextInt(this.c.getWidth());
        y = new Random().nextInt(this.c.getHeight());
    }

    public void draw (Graphics2D g2){
        g2.setColor(Color.lightGray);
        g2.fill(new Ellipse2D.Double(x,y,pocketRadius,pocketRadius));
    }

    public static int getPocketRadius() {
        return pocketRadius;
    }

    public Point getPosition(){
        return new Point(x,y);
    }
}
