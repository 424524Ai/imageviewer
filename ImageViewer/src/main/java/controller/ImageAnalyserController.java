package controller;

import javafx.scene.Scene;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import model.CelestialBody;
import model.ImageDrawer;

import java.io.File;
import java.util.List;

public class ImageAnalyserController {
    // 将以下组件设为成员变量方便管理和更新
    private Stage stage;
    private Button chooseImageButton;  // 按钮
    private Canvas imageCanvas; // 用于显示图片
    private GraphicsContext gc;
    private Button markCelestialBodyButton;
    private Button originalImageButton;
    private Image blackAndWhiteImage;
    private Label celestialCountLabel;
    private Button colorBWImageButton;
    private List<CelestialBody> celestialBodies;

    // 构造器
    public ImageAnalyserController(Stage stage) {
        this.stage = stage;
        this.chooseImageButton = new Button("Select image from file");
        this.imageCanvas = new Canvas();
        this.gc = imageCanvas.getGraphicsContext2D();
    }

    // 创建初始画面
    public BorderPane createLayout() {
        BorderPane root = new BorderPane();

        // 按钮点击事件，选择图片
        chooseImageButton.setOnAction(e -> showImage());
        celestialCountLabel = new Label("Celestial bodies: 0"); // 默认值为 0

        // 将按钮置于图片视图的中心
        root.setCenter(chooseImageButton);
        return root;
    }

    // 打开文件选择器，选择图片并更新ImageView
    private void showImage() {
        // 创建文件选择器
        FileChooser fileChooser = new FileChooser();
        // 创建文件选择筛选对象，只允许选择图片类型文件
        FileChooser.ExtensionFilter extFilter = new FileChooser.ExtensionFilter("Image Files", "*.png", "*.jpg", "*.bmp");
        fileChooser.getExtensionFilters().add(extFilter);

        // 获取用户选择的文件
        File selectedFile = fileChooser.showOpenDialog(stage);

        if (selectedFile != null) {
            // 加载选择的图片并显示
            Image originalImage = new Image(selectedFile.toURI().toString());
            imageCanvas.setWidth(originalImage.getWidth());
            imageCanvas.setHeight(originalImage.getHeight());

            // 在 Canvas 上绘制图片
            gc.drawImage(originalImage, 0, 0);

            // 创建一个新的窗口来显示图片
            Stage imageStage = new Stage();
            ScrollPane sp = new ScrollPane();

            VBox vb = new VBox();
            vb.setSpacing(5);

            Label sliderLabel = new Label("Slide to view black and white image");
            Slider thresholdSlider = new Slider(0,1,0.5);
            thresholdSlider.setBlockIncrement(0.01);
            thresholdSlider.setShowTickMarks(true);
            thresholdSlider.setShowTickLabels(true);
            thresholdSlider.setMajorTickUnit(0.1); // 主要刻度间隔（例如 0.2, 0.4, 0.6, 0.8）
            thresholdSlider.setMinorTickCount(1); // 设置次级刻度（在两个主要刻度之间再增加 1 个小刻度）

            // 监听滑块变化，并更新黑白图片 & ImageView
            thresholdSlider.valueProperty().addListener((observable, oldValue, newValue) -> {
                double threshold = newValue.doubleValue();
                // 更新全局黑白图
                blackAndWhiteImage = ImageDrawer.drawBlackAndWhiteImage(originalImage, threshold);
                gc.drawImage(blackAndWhiteImage, 0,0);
            });

            originalImageButton = new Button("View original image");
            originalImageButton.setOnMouseClicked(event -> {
                gc.drawImage(originalImage,0,0);
            });

            markCelestialBodyButton = new Button("mark celestial body on image");
            Label minPixelSizeLabel = new Label("Set minimum pixel size");
            TextField minPixelSize = new TextField("0");
            // 限制输入，只允许输入正整数
            minPixelSize.textProperty().addListener((obs, oldValue, newValue) -> {
                if (!newValue.matches("[0-9]\\d*")) { // 只允许数字
                    minPixelSize.setText(oldValue); // 非法输入，恢复旧值
                }
            });
            markCelestialBodyButton.setOnMouseClicked(event -> {
                celestialBodies = ImageDrawer.findCelestialBodyInBWImage(originalImage, blackAndWhiteImage, Integer.parseInt(minPixelSize.getText()));
                ImageDrawer.drawCircleOnImage(celestialBodies,originalImage,imageCanvas);
                showInfo(celestialBodies);
                for (CelestialBody celestialBody : celestialBodies) {
                    System.out.println(celestialBody.toString() + "\n");
                }
                celestialCountLabel.setText("Estimated celestial objects amount: " + celestialBodies.size());
            });

            colorBWImageButton = new Button("Color black and white image celestial bodies");
            colorBWImageButton.setOnMouseClicked(event -> {
                ImageDrawer.colorObjectsInBWImage(celestialBodies, imageCanvas, blackAndWhiteImage);
                showInfo(celestialBodies);
            });

            vb.getChildren().addAll(imageCanvas, celestialCountLabel, sliderLabel, thresholdSlider, originalImageButton, minPixelSizeLabel, minPixelSize, markCelestialBodyButton, colorBWImageButton);

            HBox hb = new HBox();
            hb.setSpacing(10);
            hb.getChildren().addAll(vb);

            sp.setContent(hb);

            // 创建场景并设置窗口
            Scene imageScene = new Scene(sp, 800, 500);  // 设置新窗口的大小
            imageStage.setScene(imageScene);
            imageStage.show();  // 显示新窗口
        }
    }

    public void showInfo(List<CelestialBody> celestialBodies){
        imageCanvas.setOnMouseMoved(event -> {
            int mouseX = (int) event.getX();
            int mouseY = (int) event.getY();
            for (CelestialBody c : celestialBodies) {
                // 计算鼠标与星体的距离判断鼠标是否在星体上
                double distance = Math.sqrt(Math.pow(mouseX - c.getCenterX(), 2) + Math.pow(mouseY - c.getCenterY(), 2));
                if (distance <= c.getRadius()) {
                    Tooltip tooltip = new Tooltip(c.toString());
                    Tooltip.install(imageCanvas, tooltip);
                    return; // 只显示一个工具提示，防止多个同时显示
                }
            }
        });
    }
}
