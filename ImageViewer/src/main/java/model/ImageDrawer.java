package model;

import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.image.Image;
import javafx.scene.image.PixelReader;
import javafx.scene.image.PixelWriter;
import javafx.scene.image.WritableImage;
import javafx.scene.paint.Color;

import java.util.*;

// 工具类，负责画图
public class ImageDrawer {
    /**
     * 画黑白图像
     * @param originalImage 原图
     * @param threshold 亮度阈值，越低亮点越多
     * @return 黑白图
     */
    public static Image drawBlackAndWhiteImage(Image originalImage, double threshold){
        int width = (int) originalImage.getWidth();
        int height = (int) originalImage.getHeight();

        PixelReader pr = originalImage.getPixelReader();
        WritableImage blackAndWhiteImage = new WritableImage(width, height);
        PixelWriter pw = blackAndWhiteImage.getPixelWriter();

        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                Color color = pr.getColor(x, y);
                // 计算亮度（Luminance）
                double luminance = 0.299 * color.getRed() + 0.587 * color.getGreen() + 0.114 * color.getBlue();
                // 根据阈值设置黑白颜色
                Color newColor = (luminance > threshold) ? Color.WHITE : Color.BLACK;
                pw.setColor(x, y, newColor);
            }
        }
        return blackAndWhiteImage;
    }

    /**
     * 返回一个排好序的星体集合
     * @param originalImage 原图
     * @param blackAndWhiteImage 黑白图
     * @param minPixelSize 最小像素数
     * @return 图像中星体集合
     */
    public static List<CelestialBody> findCelestialBodyInBWImage(Image originalImage, Image blackAndWhiteImage, int minPixelSize) {
        int width = (int) blackAndWhiteImage.getWidth();
        int height = (int) blackAndWhiteImage.getHeight();
        PixelReader bWR = blackAndWhiteImage.getPixelReader();

        int[][] pixel = new int[width][height]; // 记录白色像素位置
        DisjointSet ds = new DisjointSet(width * height);

        // 1. 识别白色像素，并用并查集合并星体
        unionFindWhitePixels(bWR, pixel, ds, width, height);

        // 2. 根据并查集结果，分组星体像素
        Map<Integer, List<int[]>> celestialMap = groupPixelsByCelestialBody(pixel, ds, width, height);

        // 3. 计算星体的大小、中心、半径、气体浓度
        List<CelestialBody> celestialBodies = calculateCelestialBodies(celestialMap, originalImage, minPixelSize, width, height);

        // 4. 按大小排序，并重新分配 ID
        return assignIdToCelestialBody(celestialBodies);
    }

    /**
     * 识别白色像素，并用并查集合并星体，一开始pixel[x][y]有自己独特的编号，合并操作后，属于同一个星体的像素点最终共享同一个根编号（由 find() 计算）。
     * @param bWR 黑白图的像素读取器
     * @param pixel 像素坐标数组
     * @param ds 不相交集合
     * @param width 原图宽
     * @param height 原图高
     */
    private static void unionFindWhitePixels(PixelReader bWR, int[][] pixel, DisjointSet ds, int width, int height) {
        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                if (bWR.getColor(x, y).equals(Color.WHITE)) {
                    pixel[x][y] = y * width + x; // 赋编号
                    // 连接上方像素
                    if (y > 0 && pixel[x][y - 1] != 0) {
                        ds.union(pixel[x][y], pixel[x][y - 1]);
                    }
                    // 连接左方像素
                    if (x > 0 && pixel[x - 1][y] != 0) {
                        ds.union(pixel[x][y], pixel[x - 1][y]);
                    }
                }
            }
        }
    }

    /**
     * 根据并查集结果，分组星体像素
     * @param pixel 像素坐标数组
     * @param ds 不相交集合
     * @param width 原图宽
     * @param height 原图高
     * @return 键 = 星体编号，值 = 星体所有像素坐标数组
     */
    private static Map<Integer, List<int[]>> groupPixelsByCelestialBody(int[][] pixel, DisjointSet ds, int width, int height) {
        Map<Integer, List<int[]>> celestialMap = new HashMap<>();
        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                if (pixel[x][y] != 0) { // 不等于0说明找到了白色像素
                    int root = ds.find(pixel[x][y]); // 找到当前坐标的老大
                    celestialMap.computeIfAbsent(root, k -> new ArrayList<>()).add(new int[]{x, y}); // 键 = root， 值 = 坐标数组 int[0] = x坐标， int[1] = y坐标
                }
            }
        }
        return celestialMap;
    }

    /**
     * 计算星体的大小、中心、半径、气体浓度
     * @param celestialMap 键 = 星体编号，值 = 星体所有像素坐标数组 map集合
     * @param originalImage 原图
     * @param minPixelSize 最小像素数
     * @param width 原图宽
     * @param height 原图高
     * @return 图中星体集合
     */
    private static List<CelestialBody> calculateCelestialBodies(Map<Integer, List<int[]>> celestialMap, Image originalImage, int minPixelSize, int width, int height) {
        List<CelestialBody> celestialBodies = new ArrayList<>();
        PixelReader oR = originalImage.getPixelReader();

        for (List<int[]> pixels : celestialMap.values()) {
            if (pixels.size() < minPixelSize) {
                continue;
            }

            // find border of celestial object and S H O concentration 找到当前星体最上，下，左，右边界
            int minX = width, minY = height, maxX = 0, maxY = 0;
            double totalRed = 0, totalGreen = 0, totalBlue = 0;

            for (int[] c : pixels) {
                minX = Math.min(minX, c[0]);
                maxX = Math.max(maxX, c[0]);
                minY = Math.min(minY, c[1]);
                maxY = Math.max(maxY, c[1]);

                // 计算 SHO 值
                Color color = oR.getColor(c[0], c[1]);
                totalRed += color.getRed();
                System.out.println("Red = " + totalRed);
                totalGreen += color.getGreen();
                System.out.println("Green = " + totalGreen);
                totalBlue += color.getBlue();
                System.out.println("Blue = " + totalBlue);
                System.out.println("pixel count" + pixels.size());
            }
            System.out.println("-------------------------------");
            int centerX = (minX + maxX) / 2; // 水平中心点坐标
            int centerY = (minY + maxY) / 2; // 垂直中心点坐标
            int radius = Math.max((maxX - minX) / 2, (maxY - minY) / 2) + 5; // 取水平和垂直半径较大值作为最终半径并加五像素缓冲，后续用于画圆操作

            double sulphur = totalRed / pixels.size();
            System.out.println("total sulphur = " + sulphur);
            double hydrogen = totalGreen / pixels.size();
            System.out.println("total hydrogen = " + hydrogen);
            double oxygen = totalBlue / pixels.size();
            System.out.println("total oxygen = " + oxygen);

            CelestialBody body = new CelestialBody(0, pixels.size(), sulphur, hydrogen, oxygen, pixels, centerX, centerY, radius);
            celestialBodies.add(body);
        }
        return celestialBodies;
    }

    /**
     * 按大小排序，并重新分配 ID
     * @param celestialBodies 用上面方法所得到的星体集合
     * @return 排好序的星体集合
     */
    private static List<CelestialBody> assignIdToCelestialBody(List<CelestialBody> celestialBodies) {
        Collections.sort(celestialBodies);
        int newId = 1;
        for (CelestialBody body : celestialBodies) {
            body.setId(newId++);
        }
        return celestialBodies;
    }

    /**
     * 给图片中星体用蓝色圈出来
     * @param celestialBodies 星体集合
     * @param originalImage 原图
     * @param canvas 画板组件
     */
    public static void drawCircleOnImage(List<CelestialBody> celestialBodies, Image originalImage, Canvas canvas){
        GraphicsContext gc = canvas.getGraphicsContext2D();
        gc.setStroke(Color.BLUE);  // 指定蓝色
        gc.setLineWidth(2);  // 线宽为2
        gc.drawImage(originalImage,0,0);
        for (CelestialBody c : celestialBodies) {
            int centerX = c.getCenterX();
            int centerY = c.getCenterY();
            int radius = c.getRadius();
            gc.strokeOval(centerX - radius, centerY - radius, 2 * radius, 2 * radius);
        }
    }

    /**
     * 将黑白图中的星体用随机颜色涂满
     * @param celestialBodies 星体集合
     * @param canvas 画板组件
     * @param blackAndWhiteImage 黑白图
     */
    public static void colorObjectsInBWImage(List<CelestialBody> celestialBodies, Canvas canvas, Image blackAndWhiteImage){
        GraphicsContext gc = canvas.getGraphicsContext2D();
        gc.drawImage(blackAndWhiteImage,0,0);
        Random random = new Random();

        for (CelestialBody c : celestialBodies) {
            Color randomColor = Color.color(random.nextDouble(), random.nextDouble(), random.nextDouble()); // 得到一个随机颜色
            gc.setFill(randomColor);
            for (int[] pixel : c.getPixels()) {
                int x = pixel[0];
                int y = pixel[1];
                gc.fillRect(x, y, 1, 1);  // 逐个像素填充颜色
            }
        }
    }
}
