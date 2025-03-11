import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;

public class BounceFrame extends JFrame {
    private BallCanvas canvas;

    public static final int WIDTH = 450;
    public static final int HEIGHT = 350;

    public static int ballCounter = 0;

    static JLabel labelBallCounter = new JLabel("Ball counter: "  + ballCounter);

    private ArrayList<Thread> threads = new ArrayList<>();


    public BounceFrame() {

        this.setSize(WIDTH, HEIGHT);
        this.setTitle("Bounce programm");
        this.canvas = new BallCanvas();

        System.out.println("In Frame Thread name = " + Thread.currentThread().getName());

        Container content = this.getContentPane();
        content.add(this.canvas, BorderLayout.CENTER);

        JPanel buttonPanel = new JPanel();
        buttonPanel.setBackground(Color.lightGray);
        JButton buttonStart = new JButton("Start");
        JButton buttonTestPriority = new JButton("Test Priority");
        JButton buttonTestJoin = new JButton("Test Join");
        JButton buttonAddPocket = new JButton("Add Pocket");
        JButton buttonStop = new JButton("Stop");

        buttonStart.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
              for (int i = 0; i < 500; i++){
                  Ball b = new Ball(canvas);
                  ballCounter = canvas.add(b);
                  labelBallCounter.setText("Ball counter: " + ballCounter);
                  BallThread thread = new BallThread(b, canvas, null);
                  thread.start();
//                  System.out.println("Thread name = " + thread.getName());
              }
            }
        });

        buttonTestPriority.addActionListener(new ActionListener() {
            final int highPriorityBallCount = 1;
            final int lowPriorityBallCount = 4000;
            final Color lowPriorityBallColor = Color.blue;
            final Color highPriorityBallColor = Color.red;
            final int xPosition = 10;
            final int yPosition = 10;

            @Override
            public void actionPerformed(ActionEvent e) {
                for(int i = 0; i < lowPriorityBallCount; i++){
                    Thread thread = createBallThread(canvas, xPosition, yPosition, lowPriorityBallColor, Thread.MIN_PRIORITY);
                    threads.add(thread);
                }

                for(int i = 0; i < highPriorityBallCount; i++){
                    Thread thread = createBallThread(canvas, xPosition, yPosition, highPriorityBallColor, Thread.MAX_PRIORITY);
                    threads.add(thread);
                }

                for (Thread t : threads)
                    t.start();
            }
        });

        buttonTestJoin.addActionListener(new ActionListener() {
            final int pocketCount = 2;

            @Override
            public void actionPerformed(ActionEvent e) {
                Ball b1 = new Ball(canvas);
                b1.setBallColor(Color.blue);
                ballCounter = canvas.add(b1);
                Ball b2 = new Ball(canvas);
                b2.setBallColor(Color.red);
                ballCounter = canvas.add(b2);
                BallThread thread1 = new BallThread(b1, canvas, null);
                BallThread thread2 = new BallThread(b2, canvas, thread1);

                thread1.start();
                thread2.start();
            }
        });

        buttonAddPocket.addActionListener(new ActionListener() {
           @Override
           public void actionPerformed(ActionEvent e) {
               System.out.println("action event e:" + e.toString());
                Pocket p = new Pocket(canvas);
                canvas.add(p);
                canvas.repaint();
           }
        });

        buttonStop.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                System.exit(0);
            }
        });

        buttonPanel.add(buttonStart);
        buttonPanel.add(buttonTestPriority);
        buttonPanel.add(buttonStop);
        buttonPanel.add(buttonTestJoin);
        buttonPanel.add(buttonAddPocket);
        buttonPanel.add(labelBallCounter);
        content.add(buttonPanel, BorderLayout.SOUTH);
    }

    private Thread createBallThread( BallCanvas canvas, int x, int y, Color c, int priority ) {
        Ball b = new Ball(canvas, x, y);
        ballCounter = canvas.add(b);
        b.setBallColor(c);
        labelBallCounter.setText("Ball counter: " + ballCounter);
        BallThread thread = new BallThread(b, canvas, null);
        thread.setPriority(priority);
        return thread;
    }

    public static void decreaseBallCounter() {
        --ballCounter;
        labelBallCounter.setText("Ball counter: " + ballCounter);
    }
}