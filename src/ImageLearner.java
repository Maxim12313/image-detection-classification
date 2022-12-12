//Andrew Merrill, edited by Maxim Kim
import java.awt.*;
import java.awt.event.*;
import java.awt.image.*;

import javax.swing.*;

import java.io.*;
import java.util.ArrayList;

import javax.imageio.*;

class ImageLearner
{
    //    final String[] categories = new String[] {
//            "0","1","2","3","4","5"
//    };
    final String[] categories = new String[]{
            "object","no object"
    };

    static final int startCropLeftX = 120;
    static final int startCropTopY = 40;
    static final int startCropWidth = 640-240;
    static final int startCropHeight = 480-80;


    static int cameraCropLeftX = startCropLeftX;
    static int cameraCropTopY = startCropTopY;
    static int cameraCropWidth = startCropWidth;
    static int cameraCropHeight = startCropHeight;

    static final int sensorWidth = 30;
    static final int sensorHeight = 30;


    JFrame controlFrame;
    JLabel dirLabel;
    JLabel categoryLabel;
    File dataDir = null;
    File rawDir = null;
    WebcamViewer videoCapture = null;
    SimpleFile indexFile = null;
    PrintStream indexStream = null;
    NeuralNetwork net;

    ImageLearner()
    {
        JButton selectDirButton = new JButton("Select Directory");
        selectDirButton.addActionListener(new SelectDirListener());
        dirLabel = new JLabel("       ");

        Box controls = Box.createVerticalBox();
        controls.setBorder(BorderFactory.createEmptyBorder(10,10,10,10));
        controls.add(selectDirButton);
        controls.add(dirLabel);
        controls.add(Box.createVerticalStrut(50));

        for (String category : categories) {
            JButton button = new JButton(category);
            button.addActionListener(new CategoryListener(category));
            controls.add(button);
            controls.add(Box.createVerticalStrut(10));
        }

        controls.add(Box.createVerticalStrut(40));
        JButton convertButton = new JButton("Convert Images");
        convertButton.addActionListener(new ConvertListener());
        controls.add(convertButton);

        controls.add(Box.createVerticalStrut(10));
        JButton learnButton = new JButton("Learn!");
        learnButton.addActionListener(new LearnListener());
        controls.add(learnButton);

        controls.add(Box.createVerticalStrut(10));
        JButton categorizeButton = new JButton("Categorize");
        categorizeButton.addActionListener(new CategorizeListener());
        controls.add(categorizeButton);

        controls.add(Box.createVerticalStrut(10));
        JButton detectButton = new JButton("Detect");
        detectButton.addActionListener(new DetectListener());
        controls.add(detectButton);

        categoryLabel = new JLabel("      ");
        controls.add(categoryLabel);

        controlFrame = new JFrame("Image Saver");
        controlFrame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        controlFrame.getContentPane().add(controls);
        controlFrame.pack();
        controlFrame.setLocation(25,50);
        controlFrame.setVisible(true);

        Point nwcorner = controlFrame.getLocation();
        Dimension size = controlFrame.getSize();

        videoCapture = new WebcamViewer();
        videoCapture.setLocation(nwcorner.x+size.width+25, nwcorner.y);

        controlFrame.toFront();
        videoCapture.toFront();
    }

    class SelectDirListener implements ActionListener
    {
        public void actionPerformed(ActionEvent event)
        {
            JFileChooser chooser = new JFileChooser();
            chooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
            int result = chooser.showOpenDialog(controlFrame);
            if (result == JFileChooser.APPROVE_OPTION) {
                dataDir = chooser.getSelectedFile();
                System.out.println("selected directory " + dataDir.getAbsolutePath());
                dirLabel.setText(dataDir.getName());
                indexFile = new SimpleFile(dataDir, "index.txt");
                indexFile.startAppending();
                indexStream = indexFile.getPrintStream();
                rawDir = new File(dataDir, "raw");
                if (! rawDir.exists()) {
                    rawDir.mkdir();
                }
            }
        }
    }

    class CategoryListener implements ActionListener
    {
        String category;

        CategoryListener(String category)
        {
            this.category = category;
        }

        public void actionPerformed(ActionEvent event)
        {
            if (dataDir == null) {
                JOptionPane.showMessageDialog(controlFrame, "You must select a directory first.", "Error", JOptionPane.ERROR_MESSAGE);
                return;
            }
            BufferedImage image = videoCapture.getImage();
            String imageFileName = "image-" + System.currentTimeMillis();
            File imageFile = new File(rawDir, imageFileName + ".png");
            try {
                ImageIO.write(image, "png", imageFile);
                indexStream.println(imageFileName + "#" + category);
                indexStream.flush();
            } catch (IOException ioe) {
                JOptionPane.showMessageDialog(controlFrame, "Error: " + ioe.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    class ConvertListener implements ActionListener
    {
        public void actionPerformed(ActionEvent event)
        {
            if (dataDir == null) {
                JOptionPane.showMessageDialog(controlFrame, "You must select a directory first.", "Error", JOptionPane.ERROR_MESSAGE);
                return;
            }
            convertImages(dataDir.getAbsolutePath());
        }
    }

    class LearnListener implements ActionListener
    {
        public void actionPerformed(ActionEvent event)
        {
            if (dataDir == null) {
                JOptionPane.showMessageDialog(controlFrame, "You must select a directory first.", "Error", JOptionPane.ERROR_MESSAGE);
                return;
            }
            java.util.List<Example> examples = ImageExample.loadExamples(dataDir.getAbsolutePath());
            int numInputSensors = sensorWidth * sensorHeight;
            int numHiddenNeurons = 100;
            double learningRate = 0.1;
            net = new NeuralNetwork(numInputSensors, numHiddenNeurons, ImageExample.getNumCategories(), learningRate);
            net.train(examples, 10000, 0.99, "x-o", 10,true);
        }
    }

    class CategorizeListener implements ActionListener
    {
        public void actionPerformed(ActionEvent event)
        {
            if (dataDir == null) {
                JOptionPane.showMessageDialog(controlFrame, "You must select a directory first.", "Error", JOptionPane.ERROR_MESSAGE);
                return;
            }
            BufferedImage image = videoCapture.getImage();
            image = cookImage(image);
            ImageExample example = new ImageExample(image, null);
            String categoryName = net.categorize(example);
            System.out.println("Identified: " + categoryName);
            categoryLabel.setText(categoryName);
        }
    }

    class DetectListener implements ActionListener
    {
        public void actionPerformed(ActionEvent event)
        {
            if (dataDir == null) {
                JOptionPane.showMessageDialog(controlFrame, "You must select a directory first.", "Error", JOptionPane.ERROR_MESSAGE);
                return;
            }
            detectImage();
        }
    }

    void convertImages(String dataDirPath)
    {
        File dataDir = new File(dataDirPath);
        File rawDir = new File(dataDir, "raw");
        File cookedDir = new File(dataDir, "cooked");
        if (! cookedDir.exists())
            cookedDir.mkdir();

        SimpleFile indexFile = new SimpleFile(dataDir, "index.txt");
        indexFile.startReading();
        while (indexFile.hasMoreLines()) {
            String line = indexFile.readNextLine();
            String[] fields = line.split("#");
            String name = fields[0];
            String category = fields[1];
            String imageName = name + ".png";
            System.out.println("processing " + imageName);
            try {
                BufferedImage rawImage = ImageIO.read(new File(rawDir, imageName));
                BufferedImage cookedImage = cookImage(rawImage);
                ImageIO.write(cookedImage, "png", new File(cookedDir, imageName));
            }
            catch (IOException ioe) {
                System.err.println("IOException: " + ioe.getMessage());
            }
        }
        indexFile.stopReading();
    }

    BufferedImage cookImage(BufferedImage rawImage)
    {
        BufferedImage croppedImage = rawImage.getSubimage(cameraCropLeftX, cameraCropTopY, cameraCropWidth, cameraCropHeight);
        Image sampledImage = croppedImage.getScaledInstance(sensorWidth,sensorHeight,Image.SCALE_AREA_AVERAGING);

        BufferedImage finalImage = new BufferedImage(sensorWidth, sensorHeight, BufferedImage.TYPE_BYTE_GRAY);
        Graphics pen = finalImage.getGraphics();
        pen.drawImage(sampledImage, 0, 0, null);
        return finalImage;
    }

    BufferedImage customCookImage(BufferedImage rawImage,int leftX,int topY, int widthHeight)
    {
        BufferedImage croppedImage = rawImage.getSubimage(leftX, topY, widthHeight, widthHeight);
        Image sampledImage = croppedImage.getScaledInstance(sensorWidth,sensorHeight,Image.SCALE_AREA_AVERAGING);

        BufferedImage finalImage = new BufferedImage(sensorWidth, sensorHeight, BufferedImage.TYPE_BYTE_GRAY);
        Graphics pen = finalImage.getGraphics();
        pen.drawImage(sampledImage, 0, 0, null);
        return finalImage;
    }

    public void detectImage(){
        //ASK ANDREW ABOUT SERVER DOWN TIME/RESTARTS

        int sizeDecrement = 100;
        int movementFactor = 50;

        //Assuming there are only objects and non objects
        ArrayList<String> categories = (ArrayList)net.getCategories();
        int objectIndex;
        if (categories.get(0).equals("object")){
            objectIndex=0;
        }
        else{
            objectIndex=1;
        }


        int imageWidthHeight = startCropWidth; //assuming the input image is always a square
        double greatestObjectValue=0;
        int savedX = 0;
        int savedY = 0;
        int savedWidthHeight = 0;
        int rightX = startCropLeftX+startCropWidth;
        int bottomY = startCropTopY+startCropHeight;
        while (imageWidthHeight>=200){
            for (int x=startCropLeftX;x<rightX-imageWidthHeight;x+=movementFactor){
                for (int y=startCropTopY;y<bottomY-imageWidthHeight;y+=movementFactor){
                    BufferedImage image = videoCapture.getImage();
                    image = customCookImage(image,x,y,imageWidthHeight);
                    ImageExample example = new ImageExample(image, null);
                    double[] objectValue = getDoubleOutput(example);
                    if (objectValue[objectIndex]>greatestObjectValue){
                        savedX = x;
                        savedY = y;
                        savedWidthHeight = imageWidthHeight;
                        greatestObjectValue = objectValue[objectIndex];
                    }
                }
            }
            imageWidthHeight-=sizeDecrement;
        }
        double threshold = 0.1;
        System.out.println(greatestObjectValue);
        if (greatestObjectValue>=threshold){
            cameraCropLeftX = savedX;
            cameraCropTopY = savedY;
            cameraCropWidth = cameraCropHeight = savedWidthHeight;
            System.out.println("Object Found!");
            System.out.println("x: "+savedX+"    y: "+savedY+"    width/height: "+savedWidthHeight);
        }
        else{
            System.out.println("No Object Found!");
        }
    }

    public double[] getDoubleOutput(Example example){
        double[][] outputs = net.computeOutputs(example);
        return outputs[2]; //answer outputs
    }

    public static void main(String[] args)
    {
        new ImageLearner();
    }
}
