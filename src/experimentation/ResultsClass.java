package experimentation;

import java.io.Serializable;

public class ResultsClass implements Serializable{
    private static final long serialVersionUID = 1801594478183968428L;
    private String className;
    private Double precision;
    private Double recall;
    private Double f1;
    private Integer TP;
    private Integer TN;
    private Integer  FP;
    private Integer FN;

    public ResultsClass(){
        this.TP = 0;
        this.TN = 0;
        this.FP = 0;
        this.FN = 0;
    }

    public String getClassName() {
        return className;
    }

    public void setClassName(String className) {
        this.className = className;
    }

    public Double getPrecision() {
        return precision;
    }

    public void setPrecision(Double precision) {
        this.precision = precision;
    }

    public Double getRecall() {
        return recall;
    }

    public void setRecall(Double recall) {
        this.recall = recall;
    }

    public Double getF1() {
        return f1;
    }

    public void setF1(Double f1) {
        this.f1 = f1;
    }

    public Integer getTP() {
        return TP;
    }

    public void setTP(Integer TP) {
        this.TP = TP;
    }

    public Integer getTN() {
        return TN;
    }

    public void setTN(Integer TN) {
        this.TN = TN;
    }

    public Integer getFP() {
        return FP;
    }

    public void setFP(Integer FP) {
        this.FP = FP;
    }

    public Integer getFN() {
        return FN;
    }

    public void setFN(Integer FN) {
        this.FN = FN;
    }

    public void computeMeasures() {
        this.precision = (1+TP*1.0)/(1+TP+FP);
        this.recall = (1+TP*1.0)/(1+TP+FN);
        this.f1 = 2*(precision*recall)/(precision+recall);
    }

    public String toString(){
        String res;
        res = String.format("%s\nPrecision: %.2f\nRecall: %.2f\nF1: %.2f", this.className, this.precision, this.recall, this.f1);
        return res;
    }
}
