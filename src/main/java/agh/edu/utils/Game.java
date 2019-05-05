package agh.edu.utils;

import javafx.application.Application;
import javafx.geometry.Insets;
import javafx.scene.Group;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.stage.Stage;
import weka.core.Instance;
import weka.core.Instances;
import weka.core.converters.ConverterUtils;

import java.util.HashMap;
import java.util.Map;

public class Game extends Application{
    @Override
    public void start(final Stage stage) throws Exception {
        int rows = 28;
        int columns = 28;

        stage.setTitle("Enjoy your game");


        GridPane grid = new GridPane();
        grid.setGridLinesVisible( true );
        grid.setPadding(new Insets(30));
        grid.getStyleClass().add("game-grid");

        for(int i = 0; i < columns; i++) {
            ColumnConstraints column = new ColumnConstraints(40);
            grid.getColumnConstraints().add(column);
        }

        for(int i = 0; i < rows; i++) {
            RowConstraints row = new RowConstraints(40);
            grid.getRowConstraints().add(row);
        }



        ConverterUtils.DataSource source = new ConverterUtils.DataSource( "C:\\Users\\P50\\Documents\\IdeaProjects\\masters_thesis\\DATA\\mnist_train.arff");
        Instances instances = source.getDataSet();
        Map<Double,Integer> class_ctr = new HashMap<>();
        for (int i = 0; i < instances.size(); i++)
        {

            Instance row = instances.get( i );
            double cl = row.value( row.numAttributes() - 1);
            System.out.println( i + " : " + row.value( row.numAttributes() - 1) );
            if( !class_ctr.containsKey( cl ) ) class_ctr.put( cl,0 );
            else class_ctr.put( cl, class_ctr.get(cl) + 1 );
        }
        class_ctr.forEach(  (k,v)-> System.out.println(k + " " + v) );

        int asd = 59906;


        Instance row = instances.get( asd );
        System.out.println( row.value( row.numAttributes() - 1) );
        grid.setGridLinesVisible(true);
        for (int i = 0; i < columns; i++) {
            for (int j = 0; j < rows; j++) {
                Pane pane = new Pane();
                int val = 255 - ((int) row.value(j + 28 * i));
                String x = "rgb(" + val + "," + val + "," + val + ");";

                pane.setStyle("-fx-background-color:"+ x );
//                pane.setOnMouseReleased(e -> {
//                    pane.getChildren().add(Anims.getAtoms(1));
//                });
                pane.getStyleClass().add("game-grid-cell");
                if (i == 0) {
                    pane.getStyleClass().add("first-column");
                }
                if (j == 0) {
                    pane.getStyleClass().add("first-row");
                }
                grid.add(pane, i, j);
            }
        }


        Scene scene = new Scene(grid, (columns * 40) + 100, (rows * 40) + 100, Color.WHITE);
        scene.getStylesheets().add("game.css");
        stage.setScene(scene);
        stage.show();
    }

//    public static class Anims {
//
//        public static Node getAtoms(final int number) {
//            Circle circle = new Circle(20, 20f, 7);
//            circle.setFill(Color.RED);
//            Group group = new Group();
//            group.getChildren().add(circle);
////            SubScene scene = new SubScene(group, 40, 40);
////            scene.setFill(Color.TRANSPARENT);
//            return group;
//        }
//    }

    public static void main(final String[] arguments) {
        Application.launch(arguments);
    }
}