package main;

import controller.ImageAnalyserController;
import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.layout.BorderPane;
import javafx.stage.Stage;

public class ImageAnalyserApp extends Application {
    private ImageAnalyserController iac;
    @Override
    public void start(Stage primaryStage) throws Exception {
        // 创建控制器实例
        iac = new ImageAnalyserController(primaryStage);

        BorderPane root = iac.createLayout();

        // 创建场景并设置窗口
        Scene scene = new Scene(root, 800, 500);
        primaryStage.setScene(scene);
        primaryStage.setTitle("Welcome to Image Analyser App!");
        primaryStage.show();
    }

    public static void main(String[] args) {
        launch();
    }
}
