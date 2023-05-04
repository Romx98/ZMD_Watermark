package Attacks;

import ij.ImagePlus;
import ij.process.ImageProcessor;

import java.awt.geom.AffineTransform;
import java.awt.image.AffineTransformOp;
import java.awt.image.BufferedImage;

public class Rotation {

    /**
     * Rotation Attacks
     *
     * @param image - Original image
     * @param rotate - rotation degree
     * @return Rotated image
     */
    public static BufferedImage rotate(final BufferedImage image, final int rotate) {
        double iCenterX = image.getHeight() / 2.0;
        double iCenterY = image.getWidth() / 2.0;
        double iRadius = Math.toRadians(rotate);

        // Define rotation
        AffineTransform transform = new AffineTransform();
        transform.rotate(iRadius, iCenterX, iCenterY);

        // Apply rotation to Image
        AffineTransformOp transformOp = new AffineTransformOp(transform, AffineTransformOp.TYPE_BILINEAR);
        BufferedImage iRotated = transformOp.filter(image, null);

        // Create image with new dimensions
        ImageProcessor imageProcessor = new ImagePlus("", iRotated).getProcessor();
        imageProcessor.setInterpolationMethod(ImageProcessor.BICUBIC);
        imageProcessor.setRoi(0, 0, image.getWidth(), image.getHeight());

        return imageProcessor.crop().getBufferedImage();
    }
}
