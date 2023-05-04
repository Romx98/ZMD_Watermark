package Watermark;

import Jama.Matrix;

public class DCT {

    /**
     * Watermark the image matrix of a given component,
     * The main function that provides watermarking using the 2-DCT technique.
     *
     * @param image - Component matrix
     * @param watermark - Message binary text
     * @param deep - Deep level
     * @param x1 - position X for block 1
     * @param y1 - position Y for block 1
     * @param x2 - position X for block 2
     * @param y2 - position Y for block 2
     * @return Watermarked component matrix
     */
    public static Matrix watermarkedMatrixWithDCT(
            final Matrix image, final String watermark, final int deep, final int x1,
            final int y1, final int x2, final int y2) {
        Matrix result = image.copy();

        int iCols = image.getColumnDimension();
        int iRows = image.getRowDimension();
        int iColBlocks = iCols / deep;
        int iRowBlocks = iRows / deep;
        int[] wBinaryArray = convertStringToBinaryArray(watermark);
        int wCount = 0;

        if (wBinaryArray.length > (iColBlocks * iRowBlocks)) {
            return result;
        }

        for (int iCol = 0; iCol < (iCols - deep + 1); iCol  += deep) {
            for (int iRow = 0; iRow < (iRows - deep + 1); iRow += deep) {
                if (wCount > (wBinaryArray.length - 1)) break;

                Matrix iBlock = result.getMatrix(iRow, iRow + deep - 1, iCol, iCol + deep - 1);

                double block1 = result.get(x1, y1);
                double block2 = result.get(x2, y2);
                double[] markedValues = setMarkedValue(block1, block2, wBinaryArray[wCount], deep);

                iBlock.set(x1, y1, markedValues[0]);
                iBlock.set(x2, y2, markedValues[1]);

                result.setMatrix(iRow, iRow + deep - 1, iCol, iCol + deep - 1, iBlock);
                wCount++;
            }

            if (wCount > (wBinaryArray.length - 1)) break;
        }

        return result;
    }

    /**
     * Set marked value
     *
     * @param block1 - coefficient block 1
     * @param block2 - coefficient block 2
     * @param watermark - binary message char
     * @param deep - deep level
     * @return Array[] set blocks
     */
    private static double[] setMarkedValue(double block1, double block2, int watermark, int deep) {
        if (watermark == 0) {
            if (!(block1 < block2)) {
                double tmp = block1;
                block1 = block2;
                block2 = tmp;

            }

            if(Math.abs(block1 - block2) < deep){
                block1 -= deep / 2.0;
                block2 += deep / 2.0;
            }
        }
        else {
            if (!(block1 > block2)) {
                double tmp = block1;
                block1 = block2;
                block2 = tmp;

            }
            if(Math.abs(block1 - block2) < deep){
                block1 += deep / 2.0;
                block2 -= deep / 2.0;
            }
        }

        return new double[]{block1, block2};
    }

    /**
     * Watermark main function to extraction from given components using 2-DCT technique
     *
     * @param image - Marked component matrix
     * @param deep - Deep level
     * @param x1 - position X for block 1
     * @param y1 - position Y for block 1
     * @param x2 - position X for block 2
     * @param y2 - position Y for block 2
     * @param messageLen - Message len (this is temp!)
     * @return Extracted or error message
     */
    public static String extractedMessageWithDCT(
            final Matrix image, final int deep, final int x1, final int y1, final int x2, final int y2, int messageLen) {
        Matrix result = image.copy();

        int iCols = image.getColumnDimension();
        int iRows = image.getRowDimension();
        int[] wExtractedBinary = new int[messageLen];
        int wCount = 0;

        for (int iCol = 0; iCol < (iCols - deep + 1); iCol  += deep) {
            for (int iRow = 0; iRow < (iRows - deep + 1); iRow += deep) {
                if (wCount > (messageLen - 1)) break;

                Matrix iBlock = result.getMatrix(iRow, iRow + deep - 1, iCol, iCol + deep - 1);

                double block1 = iBlock.get(x1, y1);
                double block2 = iBlock.get(x2, y2);

                wExtractedBinary[wCount] = getMarkedValue(block1, block2);
                wCount++;
            }

            if (wCount > (messageLen - 1)) break;
        }

        return convertBinaryArrayToString(wExtractedBinary);
    }

    /**
     * Set Marked value
     *
     * @param block1 - 1. coefficient block
     * @param block2 - 2. coefficient block
     * @return 0/1
     */
    private static int getMarkedValue(final double block1, final double block2) {
        return block1 > block2 ? 1 : 0;
    }

    /**
     * Convert string into Binary array,
     * e.g: "10110..." -> [1,0,1,1,0,...]
     *
     * @param text - String text
     * @return Binary Array
     */
    private static int[] convertStringToBinaryArray(final String text) {
        int[] binaryArray = new int[text.length()];

        for (int i = 0; i < text.length(); i++) {
            binaryArray[i] = Character.getNumericValue(text.charAt(i));
        }

        return binaryArray;

    }

    /**
     * Convert Binary array into string,
     * e.g: [1,0,1,1,0,...] -> "10110..."
     *
     * @param binaryArray - Binary Array
     * @return String text
     */
    private static String convertBinaryArrayToString(final int[] binaryArray) {
        StringBuilder text = new StringBuilder();

        for (int j : binaryArray) {
            text.append(j);
        }

        return text.toString();
    }
}
