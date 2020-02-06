package javaFX.Colors;

public class Color {
    private int r,g,b;

    public Color(){
    }

    public Color(int r, int g, int b){
        this.setR(r);
        this.setG(g);
        this.setB(b);
    }

    public Color(double r, double g, double b){
        this.setR((int)(255*r));
        this.setG((int)(255*g));
        this.setB((int)(255*b));
    }

    public int getR() {
        return r;
    }

    public void setR(int r) {
        this.r = r;
    }

    public int getG() {
        return g;
    }

    public void setG(int g) {
        this.g = g;
    }

    public int getB() {
        return b;
    }

    public void setB(int b) {
        this.b = b;
    }

    public String toHexString(){
        return String.format("#%02X%02X%02X", r, g, b);
    }

}
