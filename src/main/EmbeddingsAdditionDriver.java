package main;

import au.com.bytecode.opencsv.CSVReader;
import au.com.bytecode.opencsv.CSVWriter;
import edu.stanford.nlp.ling.Word;
import org.spark_project.guava.collect.Lists;
import utils.GloveEmbeddingsCalculator;

import java.io.*;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public class EmbeddingsAdditionDriver {

    public static double calculateDistance(double[] array1, double[] array2) {
        double sum = 0.0;

        for (int i = 0; i < array1.length; i++) {
            sum = sum + Math.pow((array1[i] - array2[i]), 2.0);
        }

        return Math.sqrt(sum);
    }

    public static double[] calculateDifference(double[] array1, double[] array2) {
        double[] result = new double[300];
        for (int i = 0; i < array1.length; i++) {
            result[i] = array1[i] - array2[i];
        }

        return result;
    }

    public static void main(String[] args) throws IOException {
        GloveEmbeddingsCalculator ec = GloveEmbeddingsCalculator.getInstance();
        CSVReader reader = new CSVReader(new FileReader("E:/matching-features/tvs/tvs-features.csv"), ',', '"');
        CSVWriter writer = new CSVWriter(new FileWriter("E:/matching-features/tvs/tvs-features-2.csv"), ',', CSVWriter.NO_QUOTE_CHARACTER);
        String[] row;
        int v = 0;
        List<String> header = Lists.newArrayList(reader.readNext());
        for (int i = 0; i < 300 ; i++) {
            header.add(String.format("NameGlove300.%d-dif", i));
        }
        writer.writeNext(header.toArray(new String[0]));
        while( (row = reader.readNext()) != null){
            List<String> data = Lists.newArrayList(row);
            String label1 = data.get(data.size()-5).split("\\|")[0].trim();
            String label2 = data.get(data.size()-4).split("\\|")[0].trim();
            if(label1.length() > 0 && label2.length() > 0){
                List<String> words1 = Lists.newArrayList(label1.toLowerCase().split("[ _]"));
                List<String> words2 = Lists.newArrayList(label2.toLowerCase().split("[ _]"));
                System.out.println(words1);
                System.out.println(words2);
                double[] avg1 = new double[300];
                double[] avg2 = new double[300];
                for (int i = 0; i < 300; i++) {
                    avg1[i] = 0;
                    avg2[i] = 0;
                }
                int numWordsAvg1 = 0;
                int numWordsAvg2 = 0;
                for (String word:words1) {
                    double[] embeddings = ec.getEmbeddings(word);
                    if(embeddings != null) {
                        numWordsAvg1++;
                        for (int i = 0; i < 300; i++) {
                            avg1[i] += embeddings[i];
                        }
                    }
                }
                if(numWordsAvg1==0){
                    numWordsAvg1++;
                }

                for (String word:words2) {
                    double[] embeddings = ec.getEmbeddings(word);
                    if(embeddings != null) {
                        numWordsAvg2++;
                        for (int i = 0; i < 300; i++) {
                            avg2[i] += embeddings[i];
                        }
                    }
                }
                if(numWordsAvg2==0){
                    numWordsAvg2++;
                }

                for (int i = 0; i < 300; i++) {
                    avg1[i] = avg1[i]/numWordsAvg1;
                    avg2[i] = avg2[i]/numWordsAvg2;
                }

                double[] difference = calculateDifference(avg1, avg2);
                data.addAll(Arrays.stream(difference).mapToObj(x -> Double.toString(x)).collect(Collectors.toList()));
                writer.writeNext(data.toArray(new String[0]));
            }
            v++;
        }
        writer.close();
        reader.close();
    }

}
