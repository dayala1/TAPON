package dataset;

import featuresCalculation.Featurable;

public class ClassesPair extends Featurable{

    private String class1;
    private String class2;

    public String getClass1() {
        return class1;
    }

    public void setClass1(String class1) {
        this.class1 = class1;
    }

    public String getClass2() {
        return class2;
    }

    public void setClass2(String class2) {
        this.class2 = class2;
    }
}
