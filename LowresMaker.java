import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import javax.imageio.ImageIO;

public class LowresMaker
{
    private static int xCells = 40;
    private static int yCells = 40;

    public static void main(String[] args)  
    {
        // String fileName = args[0];
        // lowresMake(fileName);        

        int width = 40;
        int height = 40;
        int frequency = 4;
        int backgroundColor = 0xFF000000;
        int lineColor = 0xFFFFFFFF;

        BufferedImage image = new BufferedImage(width, height, BufferedImage.TYPE_4BYTE_ABGR);
        for (int x = 0; x < width; x++)
        {
            for (int y = 0; y < height; y++)
            {
                image.setRGB(x, y, backgroundColor);
            }
        }

        drawSinus(image, lineColor, frequency);

        saveImage(image, "drawline.png", "png");
    }
    
    public LowresMaker()
    {

    }

    public static void saveImage(BufferedImage image, String filename, String format)
    {
        try {
            File file = new File(filename);
            
            if (file.createNewFile()) {
            System.out.println("File created: " + file.getName());
            } else {
            System.out.println("Overwriting already existing file.");
            }

            ImageIO.write(image, format, file);
        } catch (Exception e)
        {
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
     * @param x1 x value of the starting point
     * @param y1 y value of the starting point
     * @param x2 x value of the ending point
     * @param y2 y value of the ending point
     */
    public static void drawLineDDA(BufferedImage image, int color, int x1, int y1, int x2, int y2)
    {
        if (   (x1 < 0 || image.getWidth()  <= x2)
            || (y1 < 0 || image.getHeight() <= y2)) {
            System.out.println("Can't draw line, start or end point lies outside image");
            System.out.printf("Image size: %dx%d\n", image.getWidth(), image.getHeight());
            System.out.printf("(x1, y1)=(%d, %d) ", x1, y1);
            System.out.printf("(x2, y2)=(%d, %d)", x2, y2);
            System.exit(1);
        }

        double m = ((double)y2 - (double)y1) / ((double)x2 - (double)x1);

        if (-1 <= m && m <= 1) {
            double y = x1 < x2 ? y1 : y2;
            int xStart = x1 < x2 ? x1 : x2;
            int xEnd = x1 < x2 ? x2 : x1;
                        
            for (int x = xStart; x <= xEnd; x++)
            {
                image.setRGB(x, (int)Math.round(y), color);
                y += m;
            }

        } else {
            m = 1 / m;

            double x = y1 < y2 ? x1 : x2;
            int yStart = y1 < y2 ? y1 : y2;
            int yEnd = y1 < y2 ? y2 : y1;

            for (int y = yStart; y <= yEnd; y++)
            {
                image.setRGB((int)Math.round(x), y, color);
                x += m;
            }

        }
    }

    /**
     * Draw the function sin(frequency*x) for x in [-pi, pi] with color. It is
     * drawn by sampling the function at some points and drawing lines between
     * them.
     * 
     * @param image the image to draw into
     * @param color the color to draw the sinus with
     * @param frequency the frequency of the sinus
     */
    public static void drawSinus(BufferedImage image, int color, double frequency)
    {
        double a = -Math.PI;
        double b = Math.PI;

        int width = image.getWidth();
        int height = image.getHeight();

        int n = width*4+1; // The number of points to sample

        double h = (b - a) / (n - 1);

        double xPrev;
        double yPrev;

        double xCurr = a;
        double yCurr = Math.sin(frequency*a);

        for (int i = 1; i < n; i++)
        {
            xPrev = xCurr;
            xCurr += h;

            yPrev = yCurr;
            yCurr = Math.sin(frequency*xCurr);

            // x and x are in "function" coordinates, but we want to draw it so
            // remap them to image coordinates
            int xImagePrev = (int) Math.round(map(a, b, 0, width-1, xPrev));
            int yImagePrev = (int) Math.round(map(-1, 1, height-1, 0, yPrev));
            int xImageCurr = (int) Math.round(map(a, b, 0, width-1, xCurr));
            int yImageCurr = (int) Math.round(map(-1, 1, height-1, 0, yCurr));

            drawLineDDA(image, color, xImagePrev, yImagePrev, xImageCurr, yImageCurr);
        }
    }

    public static double map(double aOrig, double bOrig, double aTarget, double bTarget, double value)
    {
        return ((value-aOrig) / (bOrig-aOrig))*(bTarget-aTarget)+aTarget;
    }

    public static int averageCellColor(BufferedImage cell) {

        int sum = 0;
        
        for (int y = 0; y < cell.getHeight(); y++)
        {
            for (int x = 0; x < cell.getWidth(); x++)
            {
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

    public static String lowresMake(String filename) {

        System.out.println("Reading image " + filename + "...");

        //open image
        try {
            BufferedImage picture = ImageIO.read(new File(filename));  //picture.png

            //out imagie
            BufferedImage imageOut = new BufferedImage(xCells, yCells, BufferedImage.TYPE_4BYTE_ABGR);

            int cellSizeX = picture.getWidth() / xCells;
            int cellSizeY = picture.getHeight() / yCells;

            for (int y = 0; y < yCells; y++)
            {
                for (int x = 0; x < xCells; x++)
                {
                    // Extract cell from image
                    BufferedImage cell = picture.getSubimage(x*cellSizeX, y*cellSizeY, cellSizeX, cellSizeY);

                    // Average color value in cell
                    int average = averageCellColor(cell);

                    // Write value to new image
                    imageOut.setRGB(x, y, average);
                }
            }

            saveImage(imageOut, filename+"Lowres.png", "png");

            return "temp";
        }
        catch (IOException e)
        {
            String workingDir = System.getProperty("user.dir");
            System.out.println("Current working directory : " + workingDir);
            e.printStackTrace();
        }
        
        return "temp2";

    }
}