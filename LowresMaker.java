import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import javax.imageio.ImageIO;
import java.lang.Math;

public class LowresMaker
{
    private static int xCells = 40;
    private static int yCells = 40;

    public static void main(String[] args)  
    {
        String fileName = args[0];

        lowresMake(fileName);

    }
    
    public LowresMaker()
    {

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

            try {
                File myObj = new File(filename + "Lowres.png");
                if (myObj.createNewFile()) {
                  System.out.println("File created: " + myObj.getName());
                } else {
                  System.out.println("File already exists.");
                }
                ImageIO.write(imageOut, "png", myObj);
              } catch (IOException e) {
                System.out.println("An error occurred.");
                e.printStackTrace();
              }

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