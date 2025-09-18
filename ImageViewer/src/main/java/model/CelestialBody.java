package model;

import java.util.List;

public class CelestialBody implements Comparable<CelestialBody>{
    private int id;
    private int size;
    private double sulphur;
    private double hydrogen;
    private double oxygen;
    private List<int[]> pixels;  // 每个天体拥有的所有像素坐标
    private int centerX;
    private int centerY;
    private int radius;

    public CelestialBody() {
    }

    public CelestialBody(int id, int size, double sulphur, double hydrogen, double oxygen, List<int[]> pixels, int centerX, int centerY, int radius) {
        this.id = id;
        this.size = size;
        this.sulphur = sulphur;
        this.hydrogen = hydrogen;
        this.oxygen = oxygen;
        this.pixels = pixels;
        this.centerX = centerX;
        this.centerY = centerY;
        this.radius = radius;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getSize() {
        return size;
    }

    public void setSize(int size) {
        this.size = size;
    }

    public double getSulphur() {
        return sulphur;
    }

    public void setSulphur(double sulphur) {
        this.sulphur = sulphur;
    }

    public double getHydrogen() {
        return hydrogen;
    }

    public void setHydrogen(double hydrogen) {
        this.hydrogen = hydrogen;
    }

    public double getOxygen() {
        return oxygen;
    }

    public void setOxygen(double oxygen) {
        this.oxygen = oxygen;
    }

    public List<int[]> getPixels() {
        return pixels;
    }

    public void setPixels(List<int[]> pixels) {
        this.pixels = pixels;
    }

    public int getCenterX() {
        return centerX;
    }

    public void setCenterX(int centerX) {
        this.centerX = centerX;
    }

    public int getCenterY() {
        return centerY;
    }

    public void setCenterY(int centerY) {
        this.centerY = centerY;
    }

    public int getRadius() {
        return radius;
    }

    public void setRadius(int radius) {
        this.radius = radius;
    }

    @Override
    public int compareTo(CelestialBody c) {
        return Integer.compare(c.size, this.size); // 按大小降序排列;
    }
    @Override
    public String toString() {
        return String.format("Celestial Object number: %d\n " +
                "Estimated size(pixel units): %d\n " +
                "Estimated sulphur: %.6f\n " +
                "Estimated hydrogen: %.6f\n " +
                "Estimated oxygen: %.6f", id, size, sulphur, hydrogen, oxygen);
    }
}
