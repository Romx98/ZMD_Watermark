package Jpeg;

import Enums.ComponentType;
import Graphics.Dialogs;
import Watermark.LSB;
import Watermark.DCT;
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
    private Matrix markImageLSB, markImageDCT;


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
     * Extraction of one colour from YCbCr
     *
     * @param color - Matrix of colour
     * @return Image based on one colour
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

    /**
     * Watermarking process using LSB technique
     *
     * @param componentsType - Image component (Y, Cb, Cr)
     * @param level - Bit level
     * @param multiple - Multiple watermark
     * @return Watermarked component image
     */
    public BufferedImage getImageWithLSBWatermark(ComponentType componentsType, int level, boolean multiple) {
        switch (componentsType) {
            case CB:
                markImageLSB = LSB.watermarkedMatrixWithLSB(
                        getOriginalImageCb(), getOriginalWatermarkCb(), level, multiple);
                break;
            case CR:
                markImageLSB = LSB.watermarkedMatrixWithLSB(
                        getOriginalImageCr(), getOriginalWatermarkCr(), level, multiple);
                break;
            case Y:
            default:
                markImageLSB = LSB.watermarkedMatrixWithLSB(
                        getOriginalImageY(), getOriginalWatermarkY(), level, multiple);
                break;
        }

        return getOneColorImageFromYCbCr(markImageLSB);
    }

    /**
     * Watermark extraction process using LSB technique
     *
     * @param level - Bit level
     * @return Extracted watermark
     */
    public BufferedImage getExtractImageWithLSBWatermark(int level) {
        Matrix extractWatermarkLSB = LSB.extractedMatrixWithLSB(getMarkImageLSB(), level);

        return getOneColorImageFromYCbCr(extractWatermarkLSB);
    }

    /**
     * Watermarking process using DCT technique
     *
     * @param componentsType - Image component (Y, Cb, Cr)
     * @param message - message used to watermark (binary text)
     * @param deep - deep level
     * @param x1 - position X for block 1
     * @param y1 - position Y for block 1
     * @param x2 - position X for block 2
     * @param y2 - position Y for block 2
     * @return Watermarked component image
     */
    public BufferedImage getImageWithDCTWatermark(
            ComponentType componentsType, String message, int deep, int x1, int y1, int x2, int y2) {
        switch (componentsType) {
            case CB:
                markImageDCT = DCT.watermarkedMatrixWithDCT(getOriginalImageCb(), message, deep, x1, y1, x2, y2);
                break;
            case CR:
                markImageDCT = DCT.watermarkedMatrixWithDCT(getOriginalImageCr(), message, deep, x1, y1, x2, y2);
                break;
            case Y:
            default:
                markImageDCT = DCT.watermarkedMatrixWithDCT(getOriginalImageY(), message, deep, x1, y1, x2, y2);
                break;
        }

        return getOneColorImageFromYCbCr(markImageDCT);
    }

    /**
     * Watermark extraction process using DCT technique
     *
     * @param deep - deep level
     * @param x1 - position X for block 1
     * @param y1 - position Y for block 1
     * @param x2 - position X for block 2
     * @param y2 - position Y for block 2
     * @param messageLen - message length
     * @return Extracted message
     */
    public String getImageWithDCTWatermark(int deep, int x1, int y1, int x2, int y2, int messageLen) {
        return DCT.extractedMessageWithDCT(getMarkImageDCT(), deep, x1, y1, x2, y2, messageLen);

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

    public Matrix getMarkImageDCT() {
        return markImageDCT;
    }
}
