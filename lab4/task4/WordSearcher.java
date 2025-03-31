package TPO.lab4.task4;

import java.io.*;
import java.util.*;
import java.util.concurrent.*;
import TPO.lab4.task1.Document;
import TPO.lab4.task1.Folder;


public class WordSearcher {

    String[] wordsIn(String line) {
        return line.trim().toLowerCase().split("(\\s|\\p{Punct})+");
    }

    HashMap<Document, HashMap<String, Integer>> wordSearch(Document document, List<String> searchedWord) {
        HashMap<String, Integer> wordsOccurrenceInDocument = new HashMap<>();
        for(String line : document.getLines()) {
            for(String word : wordsIn(line)) {
                if(searchedWord.contains(word)) {
                    wordsOccurrenceInDocument.put(word, wordsOccurrenceInDocument.getOrDefault(word, 0) + 1);
                }
            }
        }
        HashMap<Document, HashMap<String, Integer>> result = new HashMap<>();
        result.put(document, wordsOccurrenceInDocument);
        return result;
    }


    class DocumentSearchTask extends RecursiveTask<HashMap<Document, HashMap<String, Integer>>> {
        private final Document document;
        private final List<String> searchedWord;

        DocumentSearchTask(Document document, List<String> searchedWord) {
            super();
            this.document = document;
            this.searchedWord = searchedWord;
        }

        @Override
        protected HashMap<Document, HashMap<String, Integer>> compute() {
            return wordSearch(document, searchedWord);
        }
    }

    class FolderSearchTask extends RecursiveTask<HashMap<Document, HashMap<String, Integer>>> {
        private final Folder folder;
        private final List<String> searchingWords;

        FolderSearchTask(Folder folder, List<String> searchingWords) {
            super();
            this.folder = folder;
            this.searchingWords = searchingWords;
        }

        @Override
        protected HashMap<Document, HashMap<String, Integer>> compute() {
            HashMap<Document, HashMap<String, Integer>> wordsOccurrenceInDocument = new HashMap<>();

            List<RecursiveTask<HashMap<Document, HashMap<String, Integer>>>> forks = new LinkedList<>();
            for (Folder subFolder : folder.getSubFolders()) {
                FolderSearchTask task = new FolderSearchTask(subFolder, searchingWords);
                forks.add(task);
                task.fork();
            }
            for (Document document : folder.getDocuments()) {
                DocumentSearchTask task = new DocumentSearchTask(document, searchingWords);
                forks.add(task);
                task.fork();
            }
            for(RecursiveTask<HashMap<Document, HashMap<String, Integer>>> task : forks){
                for(Document document : folder.getDocuments()){
                    wordsOccurrenceInDocument.putAll(task.join());
                }
            }

            return wordsOccurrenceInDocument;
        }
    }

    private final ForkJoinPool forkJoinPool = new ForkJoinPool();

    HashMap<Document, HashMap<String, Integer>> SearchDocumentsWithWords(Folder folder, List<String> searchingWords) {
        return forkJoinPool.invoke(new FolderSearchTask(folder, searchingWords));
    }

    public static void main(String[] args) throws IOException {
        final String dirPath = "/home/slavik/repos/projects/src/TPO/lab4/task3/data";
        final List<String> searchingWords = List.of("java", "learning", "programming", "list", "python", "oop", "ml", "backend");

        WordSearcher wordSearcher = new WordSearcher();
        Folder folder = Folder.fromDirectory(new File(dirPath));
        HashMap<Document, HashMap<String, Integer>> documentsWithWordsOccurrence = wordSearcher.SearchDocumentsWithWords(folder, searchingWords);

        for(Document document : documentsWithWordsOccurrence.keySet()) {
            System.out.println("New doc " + document.getPath());
            HashMap<String, Integer> wordsOccurrenceInDocument = documentsWithWordsOccurrence.get(document);
            if(!wordsOccurrenceInDocument.isEmpty()) {
                for(String word : wordsOccurrenceInDocument.keySet()) {
                    System.out.println("word: \'" + word + "\' occurrence: " + wordsOccurrenceInDocument.get(word));
                }
                System.out.println();
            }
            else {
                System.out.println("None");
            }
        }

    }
}

