package Jpeg;

import Attacks.Rotation;
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
    private BufferedImage rotatedImage;

    private int[][] originalImageRed, originalWatermarkRed;
    private int[][] originalImageGreen, originalWatermarkGreen;
    private int[][] originalImageBlue, originalWatermarkBlue;

    private Matrix originalImageY, originalWatermarkY;
    private Matrix originalImageCr, originalWatermarkCr;
    private Matrix originalImageCb, originalWatermarkCb;
    private Matrix markImageLSB, markImageDCT;

    private Matrix modifyRotateY, modifyRotateCb, modifyRotateCr;

    private int imageHeight, watermarkHeight;
    private int imageWidth, watermarkWidth;

    public Process(String pathToImage, String pathToWatermark) {
        init(Dialogs.loadImageFromPath(pathToImage), Dialogs.loadImageFromPath(pathToWatermark), true);
    }

    public Process(String pathToImage) {
        init(Dialogs.loadImageFromPath(pathToImage), Dialogs.loadImageFromPath(pathToImage), false);
    }

    private void init(BufferedImage originalImage, BufferedImage originalWatermark, boolean watermark) {
        this.originalImage = originalImage;
        this.originalWatermark = originalWatermark;

        this.imageHeight = originalImage.getHeight();
        this.imageWidth = originalImage.getWidth();
        this.watermarkHeight = originalWatermark.getHeight();
        this.watermarkWidth = originalWatermark.getWidth();

        initializeMatrices(watermark);
        setOriginalImageRGBAndYCbCr();

        if (watermark) {
            setOriginalWatermarkRGBAndYCbCr();
        }

    }

    private void initializeMatrices(boolean watermark) {
        // Original Image
        originalImageRed = new int[imageHeight][imageWidth];
        originalImageGreen = new int[imageHeight][imageWidth];
        originalImageBlue = new int[imageHeight][imageWidth];

        originalImageY = new Matrix(imageHeight, imageWidth);
        originalImageCb = new Matrix(imageHeight, imageWidth);
        originalImageCr = new Matrix(imageHeight, imageWidth);

        if (watermark) {
            // Original Watermark
            originalWatermarkRed = new int[watermarkHeight][watermarkWidth];
            originalWatermarkGreen = new int[watermarkHeight][watermarkWidth];
            originalWatermarkBlue = new int[watermarkHeight][watermarkWidth];

            originalWatermarkY = new Matrix(watermarkHeight, watermarkWidth);
            originalWatermarkCb = new Matrix(watermarkHeight, watermarkWidth);
            originalWatermarkCr = new Matrix(watermarkHeight, watermarkWidth);
        }
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
     * --------------------
     */
    private void setRotatedImageYCbCr() {
        int rHeight = rotatedImage.getHeight();
        int rWidth = rotatedImage.getWidth();

        int[][] imageRed = new int[rHeight][rWidth];
        int[][] imageGreen = new int[rHeight][rWidth];
        int[][] imageBlue = new int[rHeight][rWidth];

        for (int h = 0; h < rHeight; h++) {
            for (int w = 0; w < rWidth; w++) {
                Color color = new Color(originalImage.getRGB(w, h));
                imageRed[h][w] = color.getRed();
                imageGreen[h][w] = color.getGreen();
                imageBlue[h][w] = color.getBlue();
            }
        }

        Matrix[] colors = ColorTransform.convertOriginalRGBtoYcBcR(
                imageRed, imageGreen, imageBlue);

        modifyRotateY = colors[0];
        modifyRotateCb = colors[1];
        modifyRotateCr = colors[2];

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
                markImageDCT = DCT.watermarkedMatrixWithDCT(getOriginalImageCb(), message, 8, deep, x1, y1, x2, y2);
                break;
            case CR:
                markImageDCT = DCT.watermarkedMatrixWithDCT(getOriginalImageCr(), message, 8, deep, x1, y1, x2, y2);
                break;
            case Y:
            default:
                markImageDCT = DCT.watermarkedMatrixWithDCT(getOriginalImageY(), message, 8, deep, x1, y1, x2, y2);
                break;
        }

        return getOneColorImageFromYCbCr(markImageDCT);
    }

    /**
     * Watermark extraction process using DCT technique
     *
     * @param x1 - position X for block 1
     * @param y1 - position Y for block 1
     * @param x2 - position X for block 2
     * @param y2 - position Y for block 2
     * @param messageLen - message length
     * @return Extracted message
     */
    public String getImageWithDCTWatermark(int x1, int y1, int x2, int y2, int messageLen) {
        return DCT.extractedMessageWithDCT(getMarkImageDCT(), 8, x1, y1, x2, y2, messageLen);
    }

    public BufferedImage getAttackRotation(final int rotateDegree) {
        rotatedImage = Rotation.rotate(getOneColorImageFromYCbCr(markImageDCT), rotateDegree);
        return rotatedImage;
    }

    public String getExtractAttackRotation(
            final ComponentType componentsType, final int rotateDegree, final int x1, final int y1,
            final int x2, final int y2, final int messageLen) {
        String extractedMessage;

        rotatedImage = Rotation.rotate(rotatedImage, rotateDegree);
        Dialogs.showImageInWindow(rotatedImage, "Back Rotated - " + rotateDegree);
        setRotatedImageYCbCr();

        switch (componentsType) {
            case CB:
                extractedMessage = DCT.extractedMessageWithDCT(getModifyRotateCb(), 8, x1, y1, x2, y2, messageLen);
                break;
            case CR:
                extractedMessage = DCT.extractedMessageWithDCT(getOriginalImageCr(), 8, x1, y1, x2, y2, messageLen);
                break;
            case Y:
            default:
                extractedMessage = DCT.extractedMessageWithDCT(getOriginalImageY(), 8, x1, y1, x2, y2, messageLen);
                break;
        }

        return extractedMessage;
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

    public BufferedImage getRotatedImage() {
        return rotatedImage;
    }


    public Matrix getModifyRotateY() {
        return modifyRotateY;
    }

    public Matrix getModifyRotateCb() {
        return modifyRotateCb;
    }

    public Matrix getModifyRotateCr() {
        return modifyRotateCr;
    }
}
