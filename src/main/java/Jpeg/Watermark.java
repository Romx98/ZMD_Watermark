package Jpeg;

import Jama.Matrix;

import java.util.Arrays;
import java.util.stream.Collectors;

public class Watermark {

    public static Matrix watermarkedMatrixWithLSB(Matrix image, Matrix watermark, int level, int key) {
        Matrix result = image.copy();

        int[] permutedWatermarkPixel, resetImagePixel, binaryNumber;
        int bottomWith = image.getColumnDimension() - watermark.getColumnDimension() - 1;
        int bottomHeight = image.getRowDimension() - watermark.getRowDimension() - 1;

        for (int col = 0; col < image.getColumnDimension(); col++) {
            for (int row = 0; row < image.getRowDimension(); row++) {
                resetImagePixel = getResetBinaryArrayByLevels((int) image.get(row, col), level);

                // Top Watermark
                if (watermark.getColumnDimension() > col && watermark.getRowDimension() > row) {
                    binaryNumber = convertToBinaryArray((int) watermark.get(row, col));
                    permutedWatermarkPixel = getPermutedBinaryArray(binaryNumber, key, false);
                    resetImagePixel = getMarkedPixelByLevel(resetImagePixel, permutedWatermarkPixel, level);
                }

                // Bottom Watermark
                if (bottomWith < col && bottomHeight < row) {
                    if (watermark.getColumnDimension() > col - bottomWith && watermark.getRowDimension() > row - bottomHeight) {
                        binaryNumber = convertToBinaryArray((int) watermark.get(row - bottomHeight, col - bottomWith));
                        permutedWatermarkPixel = getPermutedBinaryArray(binaryNumber, key, false);
                        resetImagePixel = getMarkedPixelByLevel(resetImagePixel, permutedWatermarkPixel, level);
                    }
                }

                result.set(row, col, convertBinaryArrayToDouble(resetImagePixel));
            }
        }

        return result;
    }

    public static Matrix extractedMatrixWithLSB(Matrix image, int level, int key) {
        int[] watermarkPixels, permutedPixels;
        Matrix result = image.copy();

        for (int col = 0; col < image.getColumnDimension(); col++) {
            for (int row = 0; row < image.getRowDimension(); row++) {
                watermarkPixels = getExtractMarkedPixelByLevel((int) image.get(row, col), level);
                permutedPixels = getPermutedBinaryArray(watermarkPixels, key, true);
                result.set(row, col, convertBinaryArrayToDouble(permutedPixels));
            }
        }

        return result;
    }

    private static int[] getExtractMarkedPixelByLevel(int imagePixel, int level) {
        int[] binaryNumber = convertToBinaryArray(imagePixel);

        for (int i = 0; i < binaryNumber.length; i++) {
            if (i < (binaryNumber.length - level)) {
                binaryNumber[i] = 0;
            }
        }

        return binaryNumber;
    }

    private static int[] getMarkedPixelByLevel(int[] imagePixel, int[] watermarkPixel, int level) {
        for (int l = 1; l <= level; l++) {
            imagePixel[imagePixel.length - l] += watermarkPixel[watermarkPixel.length - l];
        }

        return imagePixel;
    }

    private static int[] getPermutedBinaryArray(int[] binaryNumber, int key, boolean reverse) {
        for (int x = 0; x < binaryNumber.length; x++) {
            int position;

            if (!reverse) {
                position = (x + key) % 8;
            } else {
                position = Math.abs(x - key) % 8;
            }

            binaryNumber[position] = binaryNumber[x];
        }

        return binaryNumber;
    }

    private static int[] getResetBinaryArrayByLevels(int pixel, int level) {
        int[] binaryPixel = convertToBinaryArray(pixel);

        for (int l = 1; l <= level; l++) {
            binaryPixel[binaryPixel.length - l] = 0;
        }

        return binaryPixel;
    }

    private static int[] convertToBinaryArray(int number) {
        final String binary = String.format("%8s", Integer.toBinaryString(number)).replace(' ', '0');

        return Arrays.stream(binary.split(""))
                .mapToInt(Integer::parseInt)
                .toArray();
    }

    private static double convertBinaryArrayToDouble(int[] binaryArray) {
        final String binaryStr = Arrays.stream(binaryArray)
                .mapToObj(String::valueOf)
                .collect(Collectors.joining(""));

        return Integer.parseInt(binaryStr, 2);
    }
}
