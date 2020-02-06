package experimentation;

import com.google.common.collect.HashBasedTable;
import com.google.common.collect.Sets;
import com.google.common.collect.Table;
import org.simmetrics.StringMetric;

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.util.*;

public class ResultsDataset implements Serializable {
    private static final long serialVersionUID = 6547795591084839882L;
    private Table<String, String, Integer> confusionMatrix;
    private List<String> slotClasses;
    private Double accuracy;
    private Double macroPrecision;
    private Double macroRecall;
    private Double macroF1;
    private Map<String, ResultsClass> resultsClasses;
    private Integer totalTP;
    private Integer totalTN;
    private Integer totalFP;
    private Integer totalFN;
    private Table<String, String, Double> similarityMatrix;


    public ResultsDataset(){
        this.confusionMatrix = HashBasedTable.create();
        this.resultsClasses = new HashMap<>();
        this.similarityMatrix = HashBasedTable.create();
    }

    public Table<String, String, Integer> getConfusionMatrix() {
        return confusionMatrix;
    }

    public Double getAccuracy() {
        return accuracy;
    }

    public Double getMacroPrecision() {
        return macroPrecision;
    }

    public Double getMacroRecall() {
        return macroRecall;
    }

    public Double getMacroF1() {
        return macroF1;
    }

    public List<String> getSlotClasses() {
        return slotClasses;
    }

    public Integer getTotalTP() {
        return totalTP;
    }

    public Integer getTotalTN() {
        return totalTN;
    }

    public Integer getTotalFP() {
        return totalFP;
    }

    public Integer getTotalFN() {
        return totalFN;
    }

    public Table<String, String, Double> getSimilarityMatrix() {
        return similarityMatrix;
    }

    public Map<String, ResultsClass> getResultsClasses() {
        return resultsClasses;
    }

    public void setSlotClasses(List<String> slotClasses) {
        this.slotClasses = slotClasses;
    }

    public void addPrediction(String realClass, String inferedClass){
        if (!confusionMatrix.contains(realClass, inferedClass)){
            confusionMatrix.put(realClass, inferedClass, 0);
        }
        confusionMatrix.put(realClass, inferedClass, confusionMatrix.get(realClass, inferedClass) + 1);
    }

    public void computeMeasures(){
        double[] vector1;
        double[] vector2;


        this.totalTP = 0;
        this.totalTN = 0;
        this.totalFP = 0;
        this.totalFN = 0;

        Double totalPrecision = 0.0;
        Double totalRecall = 0.0;
        Double totalF1 = 0.0;

        if(slotClasses == null){
            slotClasses = new ArrayList<>(Sets.union(confusionMatrix.rowKeySet(), confusionMatrix.columnKeySet()));
        }

        for (String slotClass1 : slotClasses) {
            for (String slotClass2 : slotClasses) {
                if (!confusionMatrix.contains(slotClass1, slotClass2)){
                    confusionMatrix.put(slotClass1, slotClass2, 0);
                }
            }
        }

        //Similarity matrix
        for (String slotClass1 : slotClasses){
            for (String slotClass2 : slotClasses) {
                if(slotClass1.endsWith("terval") && slotClass2.endsWith("CityName")){
                    System.out.println(String.format("CASO EXTRANYO:\nvector1: %s\n vector2: %s", confusionMatrix.row(slotClass1), confusionMatrix.row(slotClass2)));
                }
                vector1 = confusionMatrix.row(slotClass1).entrySet().stream().sorted(Map.Entry.comparingByKey()).mapToDouble(i -> i.getValue()*1.0).toArray();
                vector2 = confusionMatrix.row(slotClass2).entrySet().stream().sorted(Map.Entry.comparingByKey()).mapToDouble(i -> i.getValue()*1.0).toArray();
                double cosineDistance = cosineDistance(vector1, vector2);
                similarityMatrix.put(slotClass1, slotClass2, cosineDistance);
            }
        }

        for (String slotClass : slotClasses) {
            ResultsClass resultsClass = new ResultsClass();
            resultsClass.setClassName(slotClass);
            for (String slotClass1 : slotClasses) {
                for (String slotClass2 : slotClasses) {
                    if (slotClass1.equals(slotClass)) {
                        if (slotClass1.equals(slotClass2)) {
                            resultsClass.setTP(resultsClass.getTP() + (confusionMatrix.get(slotClass1, slotClass2)));
                        } else {
                            resultsClass.setFN(resultsClass.getFN() + (confusionMatrix.get(slotClass1, slotClass2)));
                        }
                    } else {
                        if (slotClass2.equals(slotClass)) {
                            resultsClass.setFP(resultsClass.getFP() + (confusionMatrix.get(slotClass1, slotClass2)));
                        } else {
                            resultsClass.setTN(resultsClass.getTN() + (confusionMatrix.get(slotClass1, slotClass2)));
                        }
                    }
                }
            }
            resultsClass.computeMeasures();
            this.totalTP += resultsClass.getTP();
            this.totalTN += resultsClass.getTN();
            this.totalFP += resultsClass.getFP();
            this.totalFN += resultsClass.getFN();

            totalPrecision += resultsClass.getPrecision();
            totalRecall += resultsClass.getRecall();
            totalF1 += resultsClass.getF1();

            resultsClasses.put(slotClass, resultsClass);
        }

        this.accuracy = (1+totalTP*1.0)/(1+totalTP+totalFP);
        this.macroPrecision = totalPrecision / slotClasses.size();
        this.macroRecall = totalRecall / slotClasses.size();
        this.macroF1 = totalF1 / slotClasses.size();
    }

    public String toString(){
        String res;
        res = String.format("Accuracy: %.2f\nMacro precision: %.2f\nMacro recall: %.2f\nMacro F1: %.2f", this.accuracy, this.macroPrecision, this.macroRecall, this.macroF1);

        return res;
    }

    private double cosineDistance(double[] vector1, double[] vector2){
        double dotProduct;
        double normA;
        double normB;
        double cosineDistance;

        dotProduct = 0.0;
        normA = 0.0;
        normB = 0.0;
        for (int i = 0; i < vector1.length; i++) {
            dotProduct += vector1[i] * vector2[i];
            normA += vector1[i]*vector1[i];
            normB += vector2[i]*vector2[i];
        }
        cosineDistance = dotProduct / (Math.sqrt(normA) * Math.sqrt(normB));
        return cosineDistance;
    }
}
