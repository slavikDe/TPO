package TPO.lab4.task1;
import java.io.*;
import java.util.*;
import java.util.concurrent.*;

public class WordCounter {    
    String[] wordsIn(String line) {
        return line.trim().split("(\\s|\\p{Punct})+");
    }

    Statistic computeSequentially(Document document) {
        int totalLength = 0, minLength = Integer.MAX_VALUE, maxLength = 0, count = 0, sumOfSquares = 0;
        HashMap<Integer, Integer> histogram = new HashMap<>();

        for (String line : document.getLines()) {
            for (String word : wordsIn(line)) {
                totalLength += word.length();
                minLength = Math.min(minLength, word.length());
                maxLength = Math.max(maxLength, word.length());
                sumOfSquares += (int) Math.pow(word.length(), 2);
                count++;
                if(histogram.containsKey(word.length())) {
                    histogram.put(word.length(), histogram.get(word.length()) + 1);
                }
                else {
                    histogram.put(word.length(), 1);
                }
            }
        }
        int avg = (int)(totalLength / count);
        double standardDeviation = Math.sqrt(((double)sumOfSquares / count) - (Math.pow(avg, 2)));
        return new Statistic(avg, minLength, maxLength, standardDeviation, histogram);
    }

    Statistic countStatisticOnSingleThread(Folder folder) {
        Statistic statistic = new Statistic();
        for (Folder subFolder : folder.getSubFolders()) {
            statistic.mergeStatistics(countStatisticOnSingleThread(subFolder));
        }
        for (Document document : folder.getDocuments()) {
            statistic.mergeStatistics(computeSequentially(document));
        }
        return statistic;
    }

    class DocumentStatisticTask extends RecursiveTask<Statistic> {
        private final Document document;
        private Statistic statistic;

        DocumentStatisticTask(Document document) {
            super();
            this.document = document;
        }
        
        @Override
        protected Statistic compute() {
            return computeSequentially(document);
        }
    }


    class FolderSearchTask extends RecursiveTask<Statistic> {
        private final Folder folder;

        FolderSearchTask(Folder folder) {
            super();
            this.folder = folder;
        }
        
        @Override
        protected Statistic compute() {
            Statistic statistic = new Statistic();
            List<RecursiveTask<Statistic>> forks = new LinkedList<>();
            for (Folder subFolder : folder.getSubFolders()) {
                FolderSearchTask task = new FolderSearchTask(subFolder);
                forks.add(task);
                task.fork();
            }
            for (Document document : folder.getDocuments()) {
                DocumentStatisticTask task = new DocumentStatisticTask(document);
                forks.add(task);
                task.fork();
            }
            for (RecursiveTask<Statistic> task : forks) {
                statistic.mergeStatistics(task.join());
            }
            return statistic;
        }
    }
        
    private final ForkJoinPool forkJoinPool = new ForkJoinPool();
    
    Statistic countOccurrencesInParallel(Folder folder) {
        return forkJoinPool.invoke(new FolderSearchTask(folder));
    }

    public static void main(String[] args) throws IOException {
        String filePath = "/home/slavik/repos/projects/src/TPO/lab4/task1/data";

        WordCounter wordCounter = new WordCounter();
        Folder folder = Folder.fromDirectory(new File(filePath));
        
        final int repeatCount = 2;
        Statistic statistic;
        long startTime;
        long stopTime;
        
        long[] singleThreadTimes = new long[repeatCount];
        long[] forkedThreadTimes = new long[repeatCount];
        
        for (int i = 0; i < repeatCount; i++) {
            startTime = System.currentTimeMillis();
            statistic = wordCounter.countStatisticOnSingleThread(folder);
            stopTime = System.currentTimeMillis();
            singleThreadTimes[i] = (stopTime - startTime);
            statistic.prettyPrint();
            System.out.println( " , single thread search took " + singleThreadTimes[i] + "ms");
        }
        
        for (int i = 0; i < repeatCount; i++) {
            startTime = System.currentTimeMillis();
            statistic = wordCounter.countOccurrencesInParallel(folder);
            stopTime = System.currentTimeMillis();
            forkedThreadTimes[i] = (stopTime - startTime);
            statistic.prettyPrint();
            System.out.println(" , fork / join search took " + forkedThreadTimes[i] + "ms");
        }
        
        System.out.println("\nCSV Output:\n");
        System.out.println("Single thread,Fork/Join");        
        for (int i = 0; i < repeatCount; i++) {
            System.out.println(singleThreadTimes[i] + "," + forkedThreadTimes[i]);
        }
        System.out.println();
    }
}
