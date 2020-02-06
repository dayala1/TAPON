package utils;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

public class GloveEmbeddingsCalculator {
    private static GloveEmbeddingsCalculator instance = null;
    private static final String filePath = "E:/Documents/US/Tesis/glove.42B.300d.txt";

    private Map<String, double[]> embeddingsMap;

    private GloveEmbeddingsCalculator() throws IOException {
        embeddingsMap = new HashMap<>();
        try (BufferedReader br = new BufferedReader(new FileReader(filePath))) {
            String line;
            int i = 1;
            while ((line = br.readLine()) != null) {
                i++;
                // process the line.
                String[] elements = line.split(" ");
                String token = elements[0];
                String[] embeddingsStr = Arrays.copyOfRange(elements,1,301);
                double[] embeddings = Arrays.stream(embeddingsStr).mapToDouble(Double::parseDouble).toArray();
                embeddingsMap.put(token, embeddings);
                System.out.println(i); // Erase line content
            }
            br.close();
        }
    }

    public static GloveEmbeddingsCalculator getInstance() throws IOException {
        if(instance == null){
            instance = new GloveEmbeddingsCalculator();
        }
        return instance;
    }

    public double[] getEmbeddings(String word){
        return embeddingsMap.getOrDefault(word.toLowerCase(), null);
    }

    public double getSimilarity(String word1, String word2){
        double dotProduct, normA, normB, cosineSimilarity;
        double[] vector1 = getEmbeddings(word1);
        double[] vector2 = getEmbeddings(word2);

        dotProduct = 0.0;
        normA = 0.0;
        normB = 0.0;
        for (int i = 0; i < vector1.length; i++) {
            dotProduct += vector1[i] * vector2[i];
            normA += vector1[i]*vector1[i];
            normB += vector2[i]*vector2[i];
        }
        cosineSimilarity = dotProduct / (Math.sqrt(normA) * Math.sqrt(normB));

        return cosineSimilarity;
    }
}
