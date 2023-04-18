package Watermark;

import Jama.Matrix;

import java.util.Arrays;
import java.util.stream.Collectors;

public class LSB {

    /**
     * Permutation key
     */
    private static final int PERMUTATION_KEY = 731;

    /**
     * Watermark the image matrix of a given component,
     * The main function that provides watermarking using the lsb technique.
     *
     * @param image - Component matrix
     * @param watermark - Component watermark
     * @param level - Bit level
     * @param multiple - Multiple watermarks
     * @return Watermarked component matrix
     */
    public static Matrix watermarkedMatrixWithLSB(
            final Matrix image, final Matrix watermark, final int level, final boolean multiple) {
        Matrix result = image.copy();

        int iCols = image.getColumnDimension();
        int iRows = image.getRowDimension();
        int wCols = watermark.getColumnDimension();
        int wRows = watermark.getRowDimension();
        int[] iResetBinaryPixels;

        for (int iCol = 0; iCol < iCols; iCol++) {
            for (int iRow = 0; iRow < iRows; iRow++) {
                iResetBinaryPixels = getResetBinaryArrayByLevels((int) image.get(iRow, iCol), level);

                if ( !(!multiple && (iCol > wCols || iRow > wRows)) &&
                        (iCol + wCols) < iCols && (iRow + wRows) <  iRows) {
                    int wCol = iCol % wCols;
                    int wRow = iRow % wRows;

                    setMarkedBinaryPixelByLevel(iResetBinaryPixels, (int) watermark.get(wRow, wCol), level);
                }

                result.set(iRow, iCol, convertBinaryArrayToDouble(iResetBinaryPixels));
            }
        }

        return result;
    }

    /**
     * Pixel watermarking,
     * Calling watermark processing methods
     *
     * @param iBinaryPixel - Binary pixel array of image
     * @param wPixel - Numerical pixel of watermark
     * @param level - Bit level
     */
    private static void setMarkedBinaryPixelByLevel(int[] iBinaryPixel, int wPixel, int level) {
        int[] wBinaryPixel = convertToBinaryArray(wPixel);
        int[] wPermutedPixel = getPermutedBinaryArray(wBinaryPixel, false);

        iBinaryPixel[iBinaryPixel.length - level] += wPermutedPixel[iBinaryPixel.length - level];
    }

    /**
     * Watermark main function to extraction from given components using LSB technique
     *
     * @param image - Marked component matrix
     * @param level - Bit level
     * @return Matrix of the extracted watermark
     */
    public static Matrix extractedMatrixWithLSB(Matrix image, int level) {
        Matrix result = image.copy();
        int[] wBinaryPixel, wPermutedPixel;

        for (int iCol = 0; iCol < image.getColumnDimension(); iCol++) {
            for (int iRow = 0; iRow < image.getRowDimension(); iRow++) {
                wBinaryPixel = getExtractMarkedPixelByLevel((int) image.get(iRow, iCol), level);
                wPermutedPixel = getPermutedBinaryArray(wBinaryPixel, true);

                result.set(iRow, iCol, convertBinaryArrayToDouble(wPermutedPixel));
            }
        }

        return result;
    }

    /**
     * The method is used for watermark extraction,
     * Resets all bits except at the given level.
     *
     * @param imagePixel - Numeric pixel
     * @param level - Bit level
     * @return Matrix with value at the given level
     */
    private static int[] getExtractMarkedPixelByLevel(int imagePixel, int level) {
        int[] binaryPixel = convertToBinaryArray(imagePixel);

        for (int i = 0; i < binaryPixel.length; i++) {
            if (i != (binaryPixel.length - level)) {
                binaryPixel[i] = 0;
            }
        }

        return binaryPixel;
    }

    /**
     * Used in watermarking
     * Pixel converted to a binary array and resetting the value on bit level
     *
     * @param pixel - image pixel
     * @param level - bit level
     * @return binary array with zeroed value at bit level
     */
    private static int[] getResetBinaryArrayByLevels(int pixel, int level) {
        int[] binaryPixel = convertToBinaryArray(pixel);
        binaryPixel[binaryPixel.length - level] = 0;

        return binaryPixel;
    }

    /**
     * Binary array permutation by key
     *
     * @param binaryNumber - Binary array
     * @param reverse - Permutation or inverse
     * @return Permuted binary array
     */
    private static int[] getPermutedBinaryArray(int[] binaryNumber, boolean reverse) {
        for (int x = 0; x < binaryNumber.length; x++) {
            int position;

            if (!reverse) position = (x + PERMUTATION_KEY) % 8;
            else position = Math.abs(x - PERMUTATION_KEY) % 8;

            binaryNumber[position] = binaryNumber[x];
        }

        return binaryNumber;
    }

    /**
     * Convert number to binary array
     *
     * @param number - integer number
     * @return binary array of size 8 item
     */
    private static int[] convertToBinaryArray(int number) {
        final String binary = String.format("%8s", Integer.toBinaryString(number)).replace(' ', '0');

        return Arrays.stream(binary.split(""))
                .mapToInt(Integer::parseInt)
                .toArray();
    }

    /**
     * Convert binary array to double
     *
     * @param binaryArray - binary array
     * @return number in double format
     */
    private static double convertBinaryArrayToDouble(int[] binaryArray) {
        final String binaryStr = Arrays.stream(binaryArray)
                .mapToObj(String::valueOf)
                .collect(Collectors.joining(""));

        return Integer.parseInt(binaryStr, 2);
    }
}
