package app;

import javafx.application.Application;
import javafx.scene.image.Image;
import javafx.stage.Stage;

import java.io.File;

/**
 * Instantiates one instance of GridPaneNIO and passes it to the primaryStage
 *
 * @author  Linus Schlepp
 * @version 1.0.0
 */
public class Main extends Application {

    public static void main(String[] args) {
        launch();
    }

    @Override
    public void start(Stage primaryStage) throws Exception{

        GridPaneNIO grid = new GridPaneNIO(primaryStage);
        grid.start();


    }
}
