import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import javax.imageio.ImageIO;

public class LowresMaker {
    private static int cellSizeX = 25;
    private static int cellSizeY = 25;

    private static int maximumFrequency = 8;

    public static void main(String[] args) {
        if (args.length == 1) {
            lowresMake(args[0]);
        } else if (args.length == 4) {
            cellSizeX = Integer.parseInt(args[1]);
            cellSizeY = Integer.parseInt(args[2]);
            maximumFrequency = Integer.parseInt(args[3]);
            lowresMake(args[0]);
        } else {
            System.out.println("Usage: filename [cellSizeX cellSizeY maximumFrequency]");
        }
    }

    public LowresMaker() {

    }

    public static void saveImage(BufferedImage image, String filename, String format) {
        try {
            File file = new File(filename);

            if (file.createNewFile()) {
                System.out.println("File created: " + file.getName());
            } else {
                System.out.println("Overwriting already existing file... " + filename);
            }

            ImageIO.write(image, format, file);
        } catch (Exception e) {
            System.out.println("An error occurred when trying to save image.");
            e.printStackTrace();
        }

    }

    /**
     * Draw a line between (x1, y1) and (x2, y2) in image using color. It is
     * implemented using the Digital Differential Algorithm.
     * 
     * @param image the image to draw into
     * @param color the color to draw the line in
     * @param x1    x value of the starting point
     * @param y1    y value of the starting point
     * @param x2    x value of the ending point
     * @param y2    y value of the ending point
     */
    public static void drawLineDDA(BufferedImage image, int color, int x1, int y1, int x2, int y2) {
        if ((x1 < 0 || image.getWidth() <= x2)
                || (y1 < 0 || image.getHeight() <= y2)) {
            System.out.println("Can't draw line, start or end point lies outside image");
            System.out.printf("Image size: %dx%d\n", image.getWidth(), image.getHeight());
            System.out.printf("(x1, y1)=(%d, %d) ", x1, y1);
            System.out.printf("(x2, y2)=(%d, %d)", x2, y2);
            System.exit(1);
        }

        double m = ((double) y2 - (double) y1) / ((double) x2 - (double) x1);

        if (-1 <= m && m <= 1) {
            double y = x1 < x2 ? y1 : y2;
            int xStart = x1 < x2 ? x1 : x2;
            int xEnd = x1 < x2 ? x2 : x1;

            for (int x = xStart; x <= xEnd; x++) {
                image.setRGB(x, (int) Math.round(y), color);
                y += m;
            }

        } else {
            m = 1 / m;

            double x = y1 < y2 ? x1 : x2;
            int yStart = y1 < y2 ? y1 : y2;
            int yEnd = y1 < y2 ? y2 : y1;

            for (int y = yStart; y <= yEnd; y++) {
                image.setRGB((int) Math.round(x), y, color);
                x += m;
            }

        }
    }

    /**
     * Draw the function sin(frequency*x) for x in [-pi, pi] with color. It is
     * drawn by sampling the function at some points and drawing lines between
     * them.
     * 
     * @param image     the image to draw into
     * @param color     the color to draw the sinus with
     * @param frequency the frequency of the sinus
     */
    public static void drawSinus(BufferedImage image, int color, double frequency) {
        double a = -Math.PI;
        double b = Math.PI;

        int width = image.getWidth();
        int height = image.getHeight();

        int n = width * 4 + 1; // The number of points to sample

        double h = (b - a) / (n - 1);

        double xPrev;
        double yPrev;

        double xCurr = a;
        double yCurr = Math.sin(frequency * a);

        for (int i = 1; i < n; i++) {
            xPrev = xCurr;
            xCurr += h;

            yPrev = yCurr;
            yCurr = Math.sin(frequency * xCurr);

            // x and x are in "function" coordinates, but we want to draw it so
            // remap them to image coordinates
            int xImagePrev = (int) Math.round(map(a, b, 0, width - 1, xPrev));
            int yImagePrev = (int) Math.round(map(-1, 1, height - 1, 0, yPrev));
            int xImageCurr = (int) Math.round(map(a, b, 0, width - 1, xCurr));
            int yImageCurr = (int) Math.round(map(-1, 1, height - 1, 0, yCurr));

            drawLineDDA(image, color, xImagePrev, yImagePrev, xImageCurr, yImageCurr);
        }
    }

    /**
     * Maps value in range [aOrig, bOrig] to range [aTarget, bTarget].
     * 
     * @param aOrig   start of the original range
     * @param bOrig   end of the original ramge
     * @param aTarget start of the target range
     * @param bTarget end of the target range
     * @param value   the value to map, must lie in range [aOrig, bOrig]
     * @return
     */
    public static double map(double aOrig, double bOrig, double aTarget, double bTarget, double value) {
        return ((value - aOrig) / (bOrig - aOrig)) * (bTarget - aTarget) + aTarget;
    }

    public static int discreteMap(double aOrig, double bOrig, int aTarget, int bTarget, double value) {
        return (int) Math.round(map(aOrig, bOrig, (double) aTarget, (double) bTarget, value));
    }

    public static int averageCellColor(BufferedImage cell) {

        int sum = 0;

        for (int y = 0; y < cell.getHeight(); y++) {
            for (int x = 0; x < cell.getWidth(); x++) {
                int color = cell.getRGB(x, y);
                int r = (color & 0x00FF0000) >> 16;
                int g = (color & 0x0000FF00) >> 8;
                int b = (color & 0x000000FF) >> 0;
                sum += (r + g + b) / 3;
            }
        }

        int average = sum / (cell.getHeight() * cell.getWidth());
        int avarageGray = 0xFF000000 | (average << 16) | (average << 8) | (average << 0);
        return avarageGray;
    }

    /**
     * Copy source image into target image starting at coordinate (startX, startY).
     * 
     * @param target the image to write to
     * @param startX x coordinate of top left corner
     * @param startY y coordinate of top left corner
     * @param source the image to copy from
     */
    public static void copyIntoImage(BufferedImage target, int startX, int startY, BufferedImage source) {
        for (int j = 0; j < source.getWidth(); j++) {
            for (int i = 0; i < source.getHeight(); i++) {
                target.setRGB(startX + j, startY + i, source.getRGB(j, i));
            }
        }
    }

    public static String lowresMake(String filename) {

        System.out.println("Reading image " + filename + "...");

        // open image
        try {
            BufferedImage picture = ImageIO.read(new File(filename)); // picture.png

            int xCells = picture.getWidth() / cellSizeX;
            int yCells = picture.getHeight() / cellSizeY;

            // out imagie
            BufferedImage imageOut = new BufferedImage(xCells, yCells, BufferedImage.TYPE_4BYTE_ABGR);

            for (int y = 0; y < yCells; y++) {
                for (int x = 0; x < xCells; x++) {
                    // Extract cell from image
                    BufferedImage cell = picture.getSubimage(x * cellSizeX, y * cellSizeY, cellSizeX, cellSizeY);

                    // Average color value in cell
                    int average = averageCellColor(cell);

                    // Write value to new image
                    imageOut.setRGB(x, y, average);
                }
            }

            saveImage(imageOut, filename + "Lowres.png", "png");

            // Make the squiggle image!
            for (int y = 0; y < yCells; y++) {
                int prevFrequency = 0;

                for (int x = 0; x < xCells; x++) {
                    int color = imageOut.getRGB(x, y);
                    int frequency = discreteMap(0, 255, 0, maximumFrequency, 255 - (color & 0x000000FF));

                    BufferedImage image = new BufferedImage(cellSizeX, cellSizeY, BufferedImage.TYPE_4BYTE_ABGR);
                    for (int i = 0; i < cellSizeX; i++) {
                        for (int j = 0; j < cellSizeY; j++) {
                            image.setRGB(i, j, 0xFFFFFFFF);
                        }
                    }

                    // We want two adjacent sinusoids to be differentiable,
                    // i.e. we want them to move in the same direction. So
                    // fiddle about with the sign of the frequency to achieve
                    // this.
                    int sign = ((int) Math.signum(prevFrequency));
                    if (sign != 0) {
                        boolean sameParity = ((prevFrequency % 2) == (frequency % 2));
                        frequency = sign * frequency * (sameParity ? 1 : -1);
                    }

                    drawSinus(image, 0xFF000000, frequency);
                    copyIntoImage(picture, x * cellSizeX, y * cellSizeY, image);

                    prevFrequency = frequency;
                }
            }

            saveImage(picture, filename + "Squiggle.png", "png");

            return "temp";
        } catch (IOException e) {
            String workingDir = System.getProperty("user.dir");
            System.out.println("Current working directory : " + workingDir);
            e.printStackTrace();
        }

        return "temp2";

    }
}