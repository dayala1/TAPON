package javaFX;

import java.io.IOException;
import java.io.OutputStream;

import javafx.application.Platform;
import javafx.scene.control.TextArea;

public class TextConsole extends OutputStream
{
    private TextArea    output;
    private int size = 0;

    public TextConsole(TextArea ta)
    {
        this.output = ta;
    }

    @Override
    public void write(int i) throws IOException
    {

        if(size>2000){
            String currentText = output.getText();
            Platform.runLater(new Runnable() {
                @Override
                public void run() {
                    output.setText(currentText.substring(Math.max(0,currentText.length()-1000), currentText.length()-1));
                }
            });
            size = 1000;
        }
        Platform.runLater(new Runnable() {
            @Override
            public void run() {
                output.appendText(String.valueOf((char) i));
            }
        });
        size++;
    }

}