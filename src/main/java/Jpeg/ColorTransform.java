package Jpeg;

import Jama.Matrix;

public class ColorTransform {

    /**
     * Metoda, která provede transformaci RGB do YCbCr.
     *
     * @param red   height x width
     * @param green height x width
     * @param blue  height x width
     * @return Matrix [Y, Cb, Cr]
     */
    public static Matrix[] convertOriginalRGBtoYcBcR(int[][] red, int[][] green, int[][] blue) {
        int height = red.length;
        int width = red[0].length;

        Matrix convertedY = new Matrix(height, width);
        Matrix convertedCb = new Matrix(height, width);
        Matrix convertedCr = new Matrix(height, width);

        for (int h = 0; h < height; h++) {
            for (int w = 0; w < width; w++) {
                convertedY.set(h, w, 0.257 * red[h][w] + 0.504 * green[h][w] + 0.098 * blue[h][w] + 16);
                convertedCb.set(h, w, -0.148 * red[h][w] - 0.291 * green[h][w] + 0.439 * blue[h][w] + 128);
                convertedCr.set(h, w, 0.439 * red[h][w] - 0.368 * green[h][w] - 0.071 * blue[h][w] + 128);
            }
        }

        return new Matrix[]{convertedY, convertedCb, convertedCr};
    }

    /**
     * Metoda, která provede transformaci YCbCr do RGB.
     *
     * @param Y  Matrix
     * @param Cb Matrix
     * @param Cr Matrix
     * @return 3D pole intů, v pořadí [R, G, B]
     */
    public static int[][][] convertModifiedYcBcRtoRGB(Matrix Y, Matrix Cb, Matrix Cr) {
        int height = Y.getRowDimension();
        int width = Y.getColumnDimension();

        int[][] convertedRed = new int[height][width];
        int[][] convertedGreen = new int[height][width];
        int[][] convertedBlue = new int[height][width];

        for (int h = 0; h < height; h++) {
            for (int w = 0; w < width; w++) {
                convertedRed[h][w] = checkValue(1.164 * (Y.get(h, w) - 16) + 1.596 * (Cr.get(h, w) - 128));
                convertedGreen[h][w] = checkValue(1.164 * (Y.get(h, w) - 16) - 0.813 * (Cr.get(h, w) - 128) - 0.391 * (Cb.get(h, w) - 128));
                convertedBlue[h][w] = checkValue(1.164 * (Y.get(h, w) - 16) + 2.018 * (Cb.get(h, w) - 128));
            }
        }
        return new int[][][]{convertedRed, convertedGreen, convertedBlue};
    }

    /**
     * Metoda, která zaokrouhlí double na int a také
     * zajistí, že hodnota bude v rozsahu 0-255.
     *
     * @param value Hodnota, která se má zaokrouhlit.
     * @return Zaokrouhlená hodnota v rozmezí 0-255.
     */
    public static int checkValue(double value) {
        int w = (int) Math.round(value);
        if (w < 0) w = 0;
        if (w > 255) w = 255;
        return w;
    }
}
