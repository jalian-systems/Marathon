package net.sourceforge.marathon.action;

/**
 * Compare to images: careful only for images of same size
 */
import java.awt.image.*;

import java.io.*;

import javax.imageio.*;

public class ImageCompareAction {
    /**
     * @param path1
     * @param path2
     * @return
     * @throws IOException
     */
    public static boolean compare(String path1, String path2, double differenceInPercent) throws IOException {
        File file1 = new File(path1);

        File file2 = new File(path2);

        /**
         * The first image for comparison.
         */
        BufferedImage image1 = ImageIO.read(file1);

        /**
         * The second image for comparison.
         */
        BufferedImage image2 = ImageIO.read(file2);

        /**
         * Count different pixels
         */
        double falsepixel = 0;

        /**
         * Initialize percentage of unequal pixels
         */
        double percentage = 0.00;

        if ((image1.getHeight() != image2.getHeight()) || (image1.getWidth() != image2.getWidth())) {
            falsepixel = Double.POSITIVE_INFINITY;
            percentage = 100;
        } else {
            /**
             * for loop over every pixel of the image -> compare the colour if
             * there is a difference increase number of falsepixel TODO: Diff
             * image save row, column of false pixels and create new image in
             * the end
             */
            int columns = image1.getWidth();
            int rows = image1.getHeight();

            for (int row = 0; row < rows; row++) {
                for (int col = 0; col < columns; col++) {
                    if (image1.getRGB(col, row) != image2.getRGB(col, row)) {
                        falsepixel++;
                    }
                }
            }

            /**
             * calculate the percentage of different pixels.
             */
            percentage = (falsepixel) / (image1.getWidth() * image1.getHeight()) * 100;
        }

        /**
         * return the information to marathon
         */
        if (percentage <= differenceInPercent) {
            return true;
        } else {
            return false;
        }
    }
}
