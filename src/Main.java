//Maxim Kim
import java.io.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class Main {


    public static void learnAND() {
        int inputNum = 2;
        int hiddenNeuronNum = 2;
        double learningRate = 1;

        ArrayList<Example> trainingData = new ArrayList<>(Arrays.asList(
                new Example(new double[]{0, 0}, "false"),
                new Example(new double[]{0, 1}, "false"),
                new Example(new double[]{1, 0}, "false"),
                new Example(new double[]{1, 1}, "true")
        ));

        int categories = 2;

        NeuralNetwork network = new NeuralNetwork(inputNum, hiddenNeuronNum, categories, learningRate);


        int epochs = 200; //after this number of epochs, quit no matter what the accuracy is
        double desiredAccuracy = 1.0;
        int reportIntervals = 10; //report accuracy after this many epochs
        String name = "AND";
        boolean validation = false;
        network.train(trainingData, epochs, desiredAccuracy, name, reportIntervals,validation);
    }

    public static NeuralNetwork learnXOR() {
        int inputNum = 2;
        int hiddenNeuronNum = 2;
        double learningRate = 0.05;

        ArrayList<Example> trainingData = new ArrayList<>(Arrays.asList(
                new Example(new double[]{0, 0}, "false"),
                new Example(new double[]{0, 1}, "true"),
                new Example(new double[]{1, 0}, "true"),
                new Example(new double[]{1, 1}, "false")
        ));

        int categories = 2;

        NeuralNetwork network = new NeuralNetwork(inputNum, hiddenNeuronNum, categories, learningRate);


        int epochs = 50000; //(50k) after this number of epochs, quit no matter what the accuracy is
        double desiredAccuracy = 1.0;
        int reportIntervals = 100; //report accuracy after this many epochs
        String name = "XOR";
        boolean validation = false;

        network.train(trainingData, epochs, desiredAccuracy, name, reportIntervals,false);
        return network;
    }

    //64 inputs from 8x8 pixel image
    //each input is a greyscale value from 0-16
    //65th item is the category
    public static NeuralNetwork learnHandwrittenDigits() {

        int inputNum = 64;
        int hiddenNeuronNum = 128;
        double learningRate = 0.05;

        String fileName = "data/handwritten-digits/digits-train.txt";
        ArrayList<Example> trainingData = processData(fileName);

        int categories = 10;

        NeuralNetwork network = new NeuralNetwork(inputNum, hiddenNeuronNum, categories, learningRate);

        int epochs = 500; //(1 mil) after this number of epochs, quit no matter what the accuracy is
        double desiredAccuracy = 0.95;
        int reportIntervals = 10; //report accuracy after this many epochs
        String name = "Handwritten Digits";
        boolean validation = true;
        network.train(trainingData, epochs, desiredAccuracy, name, reportIntervals,validation);
        return network;
    }

    public static void testHandwrittenDigits(NeuralNetwork network) {
        String fileName = "data/handwritten-digits/digits-test.txt";
        test(network, fileName);
    }


    public static void test(NeuralNetwork network, String fileName) {
        ArrayList<Example> testData = processData(fileName);
        String name = "Handwritten Digits";
        network.test(testData, name);
    }

    public static void runMNIST(){
        int inputs = 28*28;
        int hiddenNeurons = 100;
        int categories = 10;
        double learningRate = 0.05;
        NeuralNetwork network = new NeuralNetwork(inputs,hiddenNeurons,categories,learningRate);
        GUI gui = new GUI(network);
    }


    public static ArrayList<Example> processData(String fileName) {
        ArrayList<Example> data = new ArrayList<>();
        try {
            FileReader file = new FileReader(fileName);
            BufferedReader reader = new BufferedReader(file);
            String line = reader.readLine();
            while (line != null) {
                String[] splitData = line.split(",");
                double[] inputData = new double[splitData.length - 1];
                for (int i = 0; i < splitData.length - 1; i++) {
                    inputData[i] = Double.parseDouble(splitData[i]);
                }
                String rightAnswer = splitData[splitData.length - 1];
                Example example = new Example(inputData, rightAnswer);
                data.add(example);

                line = reader.readLine();
            }
        } catch (IOException ioexception) {
            System.out.println("Ack!  We had a problem: " + ioexception.getMessage());
        }
        return data;
    }



    public static void main(String[] args) {
        // write your code here

        learnAND();
        learnXOR();
        testHandwrittenDigits(learnHandwrittenDigits());
//        runMNIST();

//        Ask andrew about test.

    }


}
