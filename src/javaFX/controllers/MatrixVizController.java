package javaFX.controllers;

import experimentation.ResultsDataset;
import javaFX.Colors.Color;
import javaFX.Colors.Colors;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.geometry.HPos;
import javafx.geometry.Pos;
import javafx.geometry.VPos;
import javafx.scene.control.Label;
import javafx.scene.control.Tooltip;
import javafx.scene.layout.AnchorPane;
import javafx.scene.paint.CycleMethod;
import javafx.scene.paint.LinearGradient;
import javafx.scene.paint.Paint;
import javafx.scene.paint.Stop;
import javafx.scene.shape.Rectangle;

import java.net.URL;
import java.util.ResourceBundle;

public class MatrixVizController implements Initializable {
    private ResultsDataset results;

    @FXML
    private AnchorPane aPane;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
    }

    public void setResults(ResultsDataset results){
        this.results = results;

        int matSpacing = 1;
        int matSize = 6;
        int matTotalSpace = matSpacing + matSize;

        Rectangle rectangle;
        Color color;
        String class1;
        String class2;
        Integer initialY = matTotalSpace * results.getSlotClasses().size();
        Tooltip tooltip;
        for (int i = 0; i < results.getSlotClasses().size() ; i++) {
            class1 = results.getSlotClasses().get(i);
            for (int j = 0; j < results.getSlotClasses().size() ; j++) {
                class2 = results.getSlotClasses().get(j);
                rectangle = new Rectangle(i*matTotalSpace, initialY - j*matTotalSpace, matSize, matSize);
                color = Colors.interpolateViridis(results.getSimilarityMatrix().get(class1, class2));
                rectangle.fillProperty().setValue(Paint.valueOf(color.toHexString()));

                tooltip = new Tooltip(String.format("%s === %s -> %.2f",  class1, class2, results.getSimilarityMatrix().get(class1, class2)));
                Tooltip.install(rectangle, tooltip);
                aPane.getChildren().add(rectangle);
            }
        }
        int legendSize = 100;
        int legendStartX = initialY/2-legendSize/2;
        int legendStartY = initialY + 25;
        System.out.println(matTotalSpace);
        System.out.println(legendSize);
        Rectangle legend = new Rectangle(legendStartX, legendStartY, legendSize, 15);
        Stop[] stops = new Stop[]{
                new Stop(0.0, javafx.scene.paint.Color.color(0.267004, 0.004874, 0.329415)),
                new Stop(0.2, javafx.scene.paint.Color.color(0.253935, 0.265254, 0.529983)),
                new Stop(0.4, javafx.scene.paint.Color.color(0.163625, 0.471133, 0.558148)),
                new Stop(0.6, javafx.scene.paint.Color.color(0.134692, 0.658636, 0.517649)),
                new Stop(0.8, javafx.scene.paint.Color.color(0.477504, 0.821444, 0.318195)),
                new Stop(1.0, javafx.scene.paint.Color.color(0.993248, 0.906157, 0.143936))};
        legend.setFill(new LinearGradient(0,0,1,0, true, CycleMethod.NO_CYCLE, stops));
        aPane.getChildren().add(legend);
        Label label00 = new Label("0.0");
        label00.translateXProperty().bind(label00.widthProperty().divide(2).negate());
        label00.setLayoutX(legendStartX);
        label00.setLayoutY(legendStartY+label00.getFont().getSize()+5);
        aPane.getChildren().add(label00);
        Label label05 = new Label("0.5");
        label05.translateXProperty().bind(label05.widthProperty().divide(2).negate());
        label05.setLayoutX(legendStartX+legendSize/2);
        label05.setLayoutY(legendStartY+label05.getFont().getSize()+5);
        aPane.getChildren().add(label05);
        Label label10 = new Label("1.0");
        label10.translateXProperty().bind(label10.widthProperty().divide(2).negate());
        label10.setLayoutX(legendStartX+legendSize);
        label10.setLayoutY(legendStartY+label10.getFont().getSize()+5);
        aPane.getChildren().add(label10);


        System.out.println(String.format("Loaded similarity matrix with %s classes", results.getSlotClasses().size()));
    }
}
