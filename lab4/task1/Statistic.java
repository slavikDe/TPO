package TPO.lab4.task1;

import java.util.HashMap;

public class Statistic {
    private int average;
    private int min;
    private int max;
    private double standardDeviation;
    private HashMap<Integer, Integer> histogram;

    Statistic() {
        average = 0;
        min = Integer.MAX_VALUE;
        max = 0;
        standardDeviation = 0;
        histogram = new HashMap<>();
    }

    Statistic(int average, int min, int max, double standardDeviation, HashMap<Integer, Integer> histogram) {
        this.average = average;
        this.min = min;
        this.max = max;
        this.standardDeviation = standardDeviation;
        this.histogram = histogram;
    }

    public void mergeStatistics(Statistic other) {
        this.average = (int)((this.average + other.average) / 2);
        this.min = Math.min(this.min, other.min);
        this.max = Math.max(this.max, other.max);
        this.standardDeviation =(int)((this.standardDeviation + other.standardDeviation) / 2);
        this.histogram.putAll(other.histogram);
    }

    public void prettyPrint(){
        System.out.println("Avg: " + average + "\nmin: " + min + "\nmax: " + max + "\nstandardDeviation: " + standardDeviation);
        for(int k : histogram.keySet()){
            System.out.println("Words length - " + k + ": word frequency - " + histogram.get(k));
        }
    }
//    public void setStatistic(int average, int min, int max, double standardDeviation, HashMap<Integer, Integer> histogram) {
//        this.average = average;
//        this.min = min;
//        this.max = max;
//        this.standardDeviation = standardDeviation;
//        this.histogram = histogram;
//    }

    public int getAverage() {return this.average;}
    public int getMin(){return min;}
    public int getMax(){return max;}
    public double getStandardDeviation(){return standardDeviation;}
    public HashMap<Integer, Integer> getHistogram(){return histogram;}
}
