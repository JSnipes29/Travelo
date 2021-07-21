package com.example.travelo.search;

import java.util.HashMap;
import java.util.Scanner;
import java.util.Set;

/**
 * This class represents one document.
 * It will keep track of the term frequencies.
 * @author swapneel
 * @author julius
 */
public class Document implements Comparable<Document> {

    /**
     * A hashmap for term frequencies.
     * Maps a term to the number of times this terms appears in this document.
     */
    private HashMap<String, Integer> termFrequency;

    /**
     * The contents of the document.
     */
    private String document;

    /**
     * The constructor.
     * @param text the contents of the string
     */
    public Document(String text) {
        this.document = text;
        termFrequency = new HashMap<String, Integer>();

        readFileAndPreProcess();
    }

    /**
     * This method will read in the file and do some pre-processing.
     * The following things are done in pre-processing:
     * Every word is converted to lower case.
     * Every character that is not a letter or a digit is removed.
     * We don't do any stemming.
     * Once the pre-processing is done, we create and update the
     */
    private void readFileAndPreProcess() {
        String[] text = document.split(" ");
        for (int i = 0; i < text.length; i++) {
            String nextWord = text[i];
            String filteredWord = nextWord.replaceAll("[^A-Za-z0-9]", "").toLowerCase();

            if (!(filteredWord.equalsIgnoreCase(""))) {
                if (termFrequency.containsKey(filteredWord)) {
                    int oldCount = termFrequency.get(filteredWord);
                    termFrequency.put(filteredWord, ++oldCount);
                } else {
                    termFrequency.put(filteredWord, 1);
                }
            }
        }
    }

    /**
     * This method will return the term frequency for a given word.
     * If this document doesn't contain the word, it will return 0
     * @param word The word to look for
     * @return the term frequency for this word in this document
     */
    public double getTermFrequency(String word) {
        if (termFrequency.containsKey(word)) {
            return termFrequency.get(word);
        } else {
            return 0;
        }
    }

    /**
     * This method will return a set of all the terms which occur in this document.
     * @return a set of all terms in this document
     */
    public Set<String> getTermList() {
        return termFrequency.keySet();
    }

    @Override
    /**
     * The overriden method from the Comparable interface.
     */
    public int compareTo(Document other) {
        return document.compareTo(other.getDocument());
    }

    /**
     * @return the filename
     */
    private String getDocument() {
        return document;
    }

    /**
     * This method is used for pretty-printing a Document object.
     * @return the filename
     */
    public String toString() {
        return document;
    }
}
