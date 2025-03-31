package TPO.lab4.task3;

import java.io.*;
import java.util.*;
import java.util.concurrent.*;
import TPO.lab4.task1.Document;
import TPO.lab4.task1.Folder;

public class WordCounter {

    String[] wordsIn(String line) {
        return line.trim().split("(\\s|\\p{Punct})+");
    }

    Long occurrencesCount(Document document, String searchedWord) {
        long count = 0;
        for (String line : document.getLines()) {
            for (String word : wordsIn(line)) {
                if (searchedWord.equals(word)) {
                    count = count + 1;
                }
            }
        }
        return count;
    }

    Set<String> uniqueWordsIn(Document document) {
        HashSet<String> commonWords = new HashSet<>();
        for (String line : document.getLines()) {
            for (String word : wordsIn(line)) {
                if (commonWords.contains(word)) {
                    continue;
                }
                else {
                    commonWords.add(word);
                }
            }
        }

        return commonWords;
    }

    class DocumentSearchTask extends RecursiveTask<Set<String>> {
        private final Document document;

        DocumentSearchTask(Document document) {
            super();
            this.document = document;
        }

        @Override
        protected Set<String> compute() {
            return uniqueWordsIn(document);
        }
    }

    class FolderSearchTask extends RecursiveTask<Set<String>> {
        private final Folder folder;

        FolderSearchTask(Folder folder) {
            super();
            this.folder = folder;
        }

        @Override
        protected Set<String> compute() {
            List<RecursiveTask<Set<String>>> forks = new LinkedList<>();
            for (Folder subFolder : folder.getSubFolders()) {
                FolderSearchTask task = new FolderSearchTask(subFolder);
                forks.add(task);
                task.fork();
            }
            for (Document document : folder.getDocuments()) {
                DocumentSearchTask task = new DocumentSearchTask(document);
                forks.add(task);
                task.fork();
            }

            HashSet<String> commonWords = new HashSet<>();
            if (!forks.isEmpty()) {
                commonWords = new HashSet<>(forks.getFirst().join());

                for (int i = 1; i < forks.size(); i++) {
                    commonWords.retainAll(forks.get(i).join());
                }
            }

            return commonWords;
        }
    }

    private final ForkJoinPool forkJoinPool = new ForkJoinPool();

    Set<String> countOccurrencesInParallel(Folder folder) {
        return forkJoinPool.invoke(new FolderSearchTask(folder));
    }

    public static void main(String[] args) throws IOException {
        WordCounter wordCounter = new WordCounter();
        final String dirPath = "/home/slavik/repos/projects/src/TPO/lab4/task3/data";

        Folder folder = Folder.fromDirectory(new File(dirPath));

        Set<String> commonWords = wordCounter.countOccurrencesInParallel(folder);
        System.out.println("Common words: " + commonWords.size());
        for (String word : commonWords) {
            System.out.println(word);
        }



    }
}
