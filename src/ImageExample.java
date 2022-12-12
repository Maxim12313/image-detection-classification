//Andrew Merrill
import java.awt.*;
import java.awt.image.*;
import java.io.*;
import javax.imageio.*;
import java.util.*;

class ImageExample extends Example
{
    // static fields to store category names and numbers
    private static Map<String,Integer> categoryNumbers;  // map category names to numbers
    private static String[] categoryNames;
    private static int numCategories = 0;

    ImageExample(BufferedImage image, String categoryName)
    {
        rightAnswer = categoryName;
        int width = image.getWidth();
        int height = image.getHeight();
        inputValues = new double[width*height];
        for (int x=0; x<width; x++) {
            for (int y=0; y<height; y++) {
                int rgb = image.getRGB(x, y);
                Color color = new Color(rgb);
                int gray = (color.getRed() + color.getGreen() + color.getBlue()) / 3;
                inputValues[y*width+x] = gray/255.0;
            }
        }
    }

    static int getNumCategories()
    {
        return numCategories;
    }

    static String getCategoryName(int categoryNumber)
    {
        if (categoryNumber == -1)
            return "unknown";
        else
            return categoryNames[categoryNumber];
    }





    static java.util.List<Example> loadExamples(String dataDirPath)
    {
        LinkedList<Example> examples = new LinkedList<Example>();
        categoryNumbers = new HashMap<String,Integer>();
        categoryNames = new String[100];
        File dataDir = new File(dataDirPath);
        File cookedDir = new File(dataDir, "cooked");
        SimpleFile indexFile = new SimpleFile(dataDir, "index.txt");
        indexFile.startReading();
        while (indexFile.hasMoreLines()) {
            String line = indexFile.readNextLine();
            String[] fields = line.split("#");
            String name = fields[0];
            String category = fields[1];
            //System.out.println("reading " + name);
            if (! categoryNumbers.containsKey(category)) {
                //System.out.println("Category " + category + " is number " + numCategories);
                categoryNumbers.put(category, numCategories);
                categoryNames[numCategories] = category;
                numCategories++;
            }
            String imageName = name + ".png";

            try {
                BufferedImage image = ImageIO.read(new File(cookedDir, imageName));
                ImageExample example = new ImageExample(image, category);
                examples.add(example);
            }
            catch (IOException ioe) {
                System.err.println("IOException: " + ioe.getMessage());
            }
        }
        indexFile.stopReading();
        return examples;
    }


}
