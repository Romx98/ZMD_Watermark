package Jpeg;

import Enums.ColorType;
import Enums.ComponentType;
import Graphics.Dialogs;
import Jama.Matrix;

import java.awt.*;
import java.awt.image.BufferedImage;

import static Jpeg.ColorTransform.checkValue;

public class Process {

    private BufferedImage originalImage;
    private BufferedImage originalWatermark;

    private int[][] originalImageRed, originalWatermarkRed;
    private int[][] originalImageGreen, originalWatermarkGreen;
    private int[][] originalImageBlue, originalWatermarkBlue;

    private Matrix originalImageY, originalWatermarkY;
    private Matrix originalImageCr, originalWatermarkCr;
    private Matrix originalImageCb, originalWatermarkCb;
    private Matrix markImageLSB, extractWatermarkLSB;


    private int imageHeight, watermarkHeight;
    private int imageWidth, watermarkWidth;

    public Process(String pathToImage, String pathToWatermark) {
        init(Dialogs.loadImageFromPath(pathToImage), Dialogs.loadImageFromPath(pathToWatermark));
    }

    private void init(BufferedImage originalImage, BufferedImage originalWatermark) {
        this.originalImage = originalImage;
        this.originalWatermark = originalWatermark;

        this.imageHeight = originalImage.getHeight();
        this.imageWidth = originalImage.getWidth();
        this.watermarkHeight = originalWatermark.getHeight();
        this.watermarkWidth = originalWatermark.getWidth();

        initializeMatrices();
        setOriginalImageRGBAndYCbCr();
        setOriginalWatermarkRGBAndYCbCr();
    }

    private void initializeMatrices() {
        // Original Image
        originalImageRed = new int[imageHeight][imageWidth];
        originalImageGreen = new int[imageHeight][imageWidth];
        originalImageBlue = new int[imageHeight][imageWidth];

        originalImageY = new Matrix(imageHeight, imageWidth);
        originalImageCb = new Matrix(imageHeight, imageWidth);
        originalImageCr = new Matrix(imageHeight, imageWidth);

        // Original Watermark
        originalWatermarkRed = new int[watermarkHeight][watermarkWidth];
        originalWatermarkGreen = new int[watermarkHeight][watermarkWidth];
        originalWatermarkBlue = new int[watermarkHeight][watermarkWidth];

        originalWatermarkY = new Matrix(watermarkHeight, watermarkWidth);
        originalWatermarkCb = new Matrix(watermarkHeight, watermarkWidth);
        originalWatermarkCr = new Matrix(watermarkHeight, watermarkWidth);
    }


    /**
     * Set RGB and YCbCr from original Image
     */
    private void setOriginalImageRGBAndYCbCr() {
        for (int h = 0; h < imageHeight; h++) {
            for (int w = 0; w < imageWidth; w++) {
                Color color = new Color(originalImage.getRGB(w, h));
                originalImageRed[h][w] = color.getRed();
                originalImageGreen[h][w] = color.getGreen();
                originalImageBlue[h][w] = color.getBlue();
            }
        }

        Matrix[] colors = ColorTransform.convertOriginalRGBtoYcBcR(
                originalImageRed, originalImageGreen, originalImageBlue);

        originalImageY = colors[0];
        originalImageCb = colors[1];
        originalImageCr = colors[2];
    }

    /**
     * Set RGB and YCbCr from original Watermark
     */
    private void setOriginalWatermarkRGBAndYCbCr() {
        for (int h = 0; h < watermarkHeight; h++) {
            for (int w = 0; w < watermarkWidth; w++) {
                Color color = new Color(originalWatermark.getRGB(w, h));
                originalWatermarkRed[h][w] = color.getRed();
                originalWatermarkGreen[h][w] = color.getGreen();
                originalWatermarkBlue[h][w] = color.getBlue();
            }
        }

        Matrix[] colors = ColorTransform.convertOriginalRGBtoYcBcR(
                originalWatermarkRed, originalWatermarkGreen, originalWatermarkBlue);

        originalWatermarkY = colors[0];
        originalWatermarkCb = colors[1];
        originalWatermarkCr = colors[2];
    }

    /**
     * Metoda pro získání jednobarevného obrázku z 2D polí pro jednu barvu
     *
     * @param color     pole pro jednu barvu
     * @param type      typ barvy, pro zjištění, do které barevné složky se má zapsat
     * @return Obrázek vytvořený z jednobarevného pole
     */
    public BufferedImage getOneColorImageFromRGB(int[][] color, ColorType type) {
        int height = color.length;
        int width = color[0].length;

        BufferedImage bfImage = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
        for (int h = 0; h < height; h++) {
            for (int w = 0; w < width; w++) {
                int pixel = color[h][w];

                bfImage.setRGB(w, h,
                        (new Color((type == ColorType.RED ? pixel : 0),
                                (type == ColorType.GREEN ? pixel : 0),
                                (type == ColorType.BLUE ? pixel : 0))).getRGB());
            }
        }

        return bfImage;
    }

    /**
     * Metoda pro převod obrázku z RGB do YCbCr.
     * Hodnoty se uloží do originálních i modifikovaných matic.
     */
    public BufferedImage getOneColorImageFromYCbCr(Matrix color) {
        int height = color.getRowDimension();
        int width = color.getColumnDimension();

        BufferedImage bfImage = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
        for (int h = 0; h < height; h++) {
            for (int w = 0; w < width; w++) {
                int pixel = checkValue(color.get(h, w));
                bfImage.setRGB(w, h, (new Color(pixel, pixel, pixel)).getRGB());
            }
        }
        return bfImage;
    }

    public BufferedImage getImageWithLSBWatermark(ComponentType componentsType, int level, int key) {
        switch (componentsType) {
            case CB:
                markImageLSB = Watermark.watermarkedMatrixWithLSB(getOriginalImageCb(), getOriginalWatermarkCb(), level, key);
                break;
            case CR:
                markImageLSB = Watermark.watermarkedMatrixWithLSB(getOriginalImageCr(), getOriginalWatermarkCr(), level, key);
                break;
            case Y:
            default:
                markImageLSB = Watermark.watermarkedMatrixWithLSB(getOriginalImageY(), getOriginalWatermarkY(), level, key);
                break;
        }

        return getOneColorImageFromYCbCr(markImageLSB);
    }

    public BufferedImage getExtractImageWithLSBWatermark(int level, int key) {
        extractWatermarkLSB = Watermark.extractedMatrixWithLSB(getMarkImageLSB(), level, key);

        return getOneColorImageFromYCbCr(extractWatermarkLSB);
    }


    /**
     *  GETTERS
     */
    public BufferedImage getOriginalImage() {
        return originalImage;
    }

    public BufferedImage getOriginalWatermark() {
        return originalWatermark;
    }

    public int[][] getOriginalImageRed() {
        return originalImageRed;
    }

    public int[][] getOriginalWatermarkRed() {
        return originalWatermarkRed;
    }

    public int[][] getOriginalImageGreen() {
        return originalImageGreen;
    }

    public int[][] getOriginalWatermarkGreen() {
        return originalWatermarkGreen;
    }

    public int[][] getOriginalImageBlue() {
        return originalImageBlue;
    }

    public int[][] getOriginalWatermarkBlue() {
        return originalWatermarkBlue;
    }

    public Matrix getOriginalImageY() {
        return originalImageY;
    }

    public Matrix getOriginalWatermarkY() {
        return originalWatermarkY;
    }

    public Matrix getOriginalImageCr() {
        return originalImageCr;
    }

    public Matrix getOriginalWatermarkCr() {
        return originalWatermarkCr;
    }

    public Matrix getOriginalImageCb() {
        return originalImageCb;
    }

    public Matrix getOriginalWatermarkCb() {
        return originalWatermarkCb;
    }

    public Matrix getMarkImageLSB() {
        return markImageLSB;
    }

    public int getWatermarkHeight() {
        return watermarkHeight;
    }

    public int getWatermarkWidth() {
        return watermarkWidth;
    }

    public int getImageHeight() {
        return imageHeight;
    }

    public int getImageWidth() {
        return imageWidth;
    }

    /**
     *  SETTERS
     */
    public void setOriginalImageY(Matrix originalImageY) {
        this.originalImageY = originalImageY;
    }

    public void setOriginalImageCr(Matrix originalImageCr) {
        this.originalImageCr = originalImageCr;
    }

    public void setOriginalImageCb(Matrix originalImageCb) {
        this.originalImageCb = originalImageCb;
    }

    public void setMarkImageLSB(Matrix markImageLSB) {
        this.markImageLSB = markImageLSB;
    }
}
