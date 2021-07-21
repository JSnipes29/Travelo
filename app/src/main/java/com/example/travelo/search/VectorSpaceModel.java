package com.example.travelo.search;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Set;

/**
 * This class implements the Vector-Space model.
 * It takes a corpus and creates the tf-idf vectors for each document.
 * @author swapneel
 * @author julius
 */
public class VectorSpaceModel {

    /**
     * The corpus of documents.
     */
    private Corpus corpus;

    /**
     * The tf-idf weight vectors.
     * The hashmap maps a document to another hashmap.
     * The second hashmap maps a term to its tf-idf weight for this document.
     */
    private HashMap<Document, HashMap<String, Double>> tfIdfWeights;

    /**
     * The constructor.
     * It will take a corpus of documents.
     * Using the corpus, it will generate tf-idf vectors for each document.
     * @param corpus the corpus of documents
     */
    public VectorSpaceModel(Corpus corpus) {
        this.corpus = corpus;
        tfIdfWeights = new HashMap<Document, HashMap<String, Double>>();

        createTfIdfWeights();
    }

    /**
     * This creates the tf-idf vectors.
     */
    private void createTfIdfWeights() {
        System.out.println("Creating the tf-idf weight vectors");
        Set<String> terms = corpus.getInvertedIndex().keySet();

        for (Document document : corpus.getDocuments()) {
            HashMap<String, Double> weights = new HashMap<String, Double>();

            for (String term : terms) {
                double tf = document.getTermFrequency(term);
                double idf = corpus.getInverseDocumentFrequency(term);

                double weight = tf * idf;

                weights.put(term, weight);
            }
            tfIdfWeights.put(document, weights);
        }
    }

    /**
     * This method will return the magnitude of a vector.
     * @param document the document whose magnitude is calculated.
     * @return the magnitude
     */
    private double getMagnitude(Document document) {
        double magnitude = 0;
        HashMap<String, Double> weights = tfIdfWeights.get(document);

        for (double weight : weights.values()) {
            magnitude += weight * weight;
        }

        return Math.sqrt(magnitude);
    }

    /**
     * This will take two documents and return the dot product.
     * @param d1 Document 1
     * @param d2 Document 2
     * @return the dot product of the documents
     */
    private double getDotProduct(Document d1, Document d2) {
        double product = 0;
        HashMap<String, Double> weights1 = tfIdfWeights.get(d1);
        HashMap<String, Double> weights2 = tfIdfWeights.get(d2);

        for (String term : weights1.keySet()) {
            product += weights1.get(term) * weights2.get(term);
        }

        return product;
    }

    /**
     * This will return the cosine similarity of two documents.
     * This will range from 0 (not similar) to 1 (very similar).
     * @param d1 Document 1
     * @param d2 Document 2
     * @return the cosine similarity
     */
    public double cosineSimilarity(Document d1, Document d2) {
        return getDotProduct(d1, d2) / (getMagnitude(d1) * getMagnitude(d2));
    }

    public double averageCosineSimilarity(ArrayList<Document> documents, int i1, int i2) {
        double cosineSum = 0;
        for (int i = i1; i < i2 + 1; i++) {
            for (int j = i + 1; j < i2; j++) {
                Document doc1 = documents.get(i);
                Document doc2 = documents.get(j);
                System.out.println("\nComparing " + doc1 + " and " + doc2);
                double x = cosineSimilarity(doc1, doc2);
                System.out.println(x);
                cosineSum+= x;

            }
        }
        return cosineSum / ((double)(i2 - i1));
    }

    public double averageCosineSimilarity(ArrayList<Document> documents, int i1, int i2, int i3, int i4) {
        double cosineSum = 0;
        for (int i = i1; i < i2; i++) {
            for (int j = i3; j < i4; j++) {
                Document doc1 = documents.get(i);
                Document doc2 = documents.get(j);
                System.out.println("\nComparing " + doc1 + " and " + doc2);
                double x = cosineSimilarity(doc1, doc2);
                System.out.println(x);
                cosineSum+= x;

            }
        }
        return cosineSum / ((double)((i2 - i1) * (i4-i3)));
    }

}
