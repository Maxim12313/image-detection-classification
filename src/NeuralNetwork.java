//Maxim Kim
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static java.lang.System.currentTimeMillis;

public class NeuralNetwork {


    private Neuron[] hiddenNeurons;
    private Neuron[] answerNeurons;
    private List<String> categories;
    private double learningRate;


    public NeuralNetwork(int inputNum, int hiddenNum, int categories, double learningRate){
        this.learningRate = learningRate;
//        this.categories = categories;


        hiddenNeurons = new Neuron[hiddenNum];
        for (int i=0;i<hiddenNum;i++){
            hiddenNeurons[i] = new Neuron(inputNum);
        }

        answerNeurons = new Neuron[categories];
        for (int i=0;i<answerNeurons.length;i++){
            answerNeurons[i] = new Neuron(hiddenNum);
        }
    }

    public List<String> getCategories(){
        return categories;
    }

    public void test(ArrayList<Example> examples,String name){
        long start = currentTimeMillis();
        int timesCorrect = 0;
        for (Example example:examples){
            String prediction = categorize(example);
            if (prediction.equals(example.getRightAnswer())){
                timesCorrect++;
            }
        }
        double accuracy = (double)timesCorrect/examples.size();
        double time = (currentTimeMillis()-start);

        System.out.println("                                                       test: "+name);
        System.out.println(
                "Accuracy: "+accuracy+"    Fraction Correct: "+timesCorrect+"/"+examples.size()+"    Time: "+time+" ms"+"   Hidden Neurons: "+hiddenNeurons.length+"    Learning Rate: "+learningRate
        );
        System.out.println("--------------------------------------------------------------------------------------------------------------------------------------------");

    }

    private List<String> findCategories(List<Example> examples){

        ArrayList<String> categories = new ArrayList<>();
        for (Example example:examples){
            String category = example.getRightAnswer();
            if (!categories.contains(category)) categories.add(category);
        }

        return categories;
    }

    private void reportPartialPerformance(double validationAccuracy, int epoch, double time) {
        System.out.println(
                "Validation Accuracy: "+validationAccuracy+"    Epoch: "+epoch+"    Time: "+time+" ms"
        );
    }
    private void reportFinalPerformance(String name,double desiredAccuracy,double validationAccuracy, int epoch, double time){

            System.out.println("--------------------------------------------------------------------------------------------------------------------------------------------");
            System.out.println("                                                        train: "+name);
            System.out.println(
                    "Desired Accuracy: "+desiredAccuracy+"    Validation Accuracy: "+validationAccuracy+"    Epoch: "+epoch+"    Time: "+time+" ms"+"   Hidden Neurons: "+hiddenNeurons.length+"    Learning Rate: "+learningRate
            );
            System.out.println("--------------------------------------------------------------------------------------------------------------------------------------------");
        }

    public void train(List<Example> examples, int epochNum, double desiredAccuracy, String name, int reportIntervals,boolean validation){
        long start = currentTimeMillis();
        Collections.shuffle(examples);

        List<Example> trainingData;
        List<Example> validationData;

        if (validation){
            int splitPoint = examples.size()/2;
            trainingData = examples.subList(0,splitPoint); //inclusive-exclusive
            validationData = examples.subList(splitPoint, examples.size());
        }
        else{
            trainingData = examples;
            validationData = examples;
        }

        this.categories = findCategories(examples);



        for (int epoch=1;epoch<=epochNum;epoch++){

            //learn every example
            for (Example example:trainingData){
                learnExample(example);
            }

            //compute accuracy with final network, no learning
            int correctPredictions = 0;
            for (Example example:validationData) {
                String prediction = categorize(example);
                if (prediction.equals(example.getRightAnswer())){
                    correctPredictions++;
                }
            }

            double validationAccuracy = (double)correctPredictions/validationData.size();
            double time = (currentTimeMillis()-start);


            if (validationAccuracy>=desiredAccuracy || epoch==epochNum){ //if hit desired accuracy or last epoch (redundant, but works)
                reportFinalPerformance(name,desiredAccuracy,validationAccuracy,epoch,time);
                break;
            }

            if (epoch%reportIntervals==0 || epoch==1){ //print accuracy every 10 epochs
                reportPartialPerformance(validationAccuracy, epoch,time);
            }
        }

    }

    private double[] getDoubleAnswer(String answer){
        double[] rightAnswerValues = new double[categories.size()];
        for (int i=0;i<categories.size();i++){
            String category = categories.get(i);
            if (answer.equals(category)){
                rightAnswerValues[i] = 1;
                break;
            }
        } //get answer value ex: {0,0,1,0,0}
        return rightAnswerValues;
    }

    public String categorize(Example example){
        double[][] outputs = computeOutputs(example);
        double[] answerOutputs = outputs[2];

        return getCategory(answerOutputs);
    }

    public void learnExample(Example example){
        double[][] outputs = computeOutputs(example);
        double[] inputValues = outputs[0];
        double[] hiddenOutputs = outputs[1];
        double[] answerOutputs = outputs[2];

        String rightAnswer = example.getRightAnswer();
        double[] rightAnswerValues = getDoubleAnswer(rightAnswer);

        double[] answerErrorSignals = getAnswerErrorSignals(answerOutputs,rightAnswerValues);
        double[] hiddenErrorSignals = getHiddenErrorSignal(answerErrorSignals,answerNeurons,hiddenNeurons,hiddenOutputs);

        setInputWeights(answerNeurons,answerErrorSignals,hiddenOutputs);
        setInputWeights(hiddenNeurons,hiddenErrorSignals,inputValues);
    }


    private String getCategory(double[] predictedAnswer){
//        return categorizeDifference(predictedAnswer);
        return categorize1(predictedAnswer);
    }

    //Pick the highest valued answer neuron
    private String categorize1(double[] predictedAnswer){
        double threshold = 0;
        int indexLargest = aboveThreshold(predictedAnswer,threshold);
        return categories.get(indexLargest);
    }

    //Pick the highest valued answer neuron, but only if it is above some threshold (for example, 0.75 or 0.5)
    private String categorize2(double [] predictedAnswer){
        double threshold = 0.6;
        int indexLargest = aboveThreshold(predictedAnswer,threshold);
        if (indexLargest!=-1){
            return categories.get(indexLargest);
        }
        return "Not sure";
    }

    //Pick the highest valued answer neuron, but only if it is above some threshold (for example, 0.75 or 0.5) and all of the other answer neurons are below the same threshold
    private String categorize3(double[] predictedAnswer){
        double threshold = 0.75;
        int indexLargest = aboveThreshold(predictedAnswer,threshold);
        if (indexLargest!=-1 && belowThreshold(predictedAnswer,indexLargest,threshold)){
                return categories.get(indexLargest);
            }
        return "Not Sure";
    }

    //Pick the highest valued answer neuron, but only if it is above some threshold (for example, 0.75 or 0.5) and all of the other answer neurons are below the same threshold
    private String categorize4(double[] predictedAnswer){
        double threshold = 0.5;
        int indexLargest = aboveThreshold(predictedAnswer,threshold);
        if (indexLargest!=-1 && belowThreshold(predictedAnswer,indexLargest,threshold)){
            return categories.get(indexLargest);
        }
        return "Not Sure";
    }

    //Pick the highest valued answer neuron, but only if it is above some threshold (for example, 0.75 or 0.5) and all of the other answer neurons are below the same threshold
    private String categorize5(double[] predictedAnswer){
        double aboveThreshold = 0.6;
        double belowThreshold = 0.2;
        int indexLargest = aboveThreshold(predictedAnswer,aboveThreshold);
        if (indexLargest!=-1 && belowThreshold(predictedAnswer,indexLargest,belowThreshold)){
            return categories.get(indexLargest);
        }
        return "Not Sure";
    }

    //Pick the highest valued answer neuron, but only if the difference between its value and the second-highest valued neuron is above some threshold (say 0.25)
    private String categorize6(double[] predictedAnswer){
        double largestValue = 0;
        double secondLargest = 0;
        int indexLargest = 0;
        for (int i=0;i<predictedAnswer.length;i++){
            double answerPart = predictedAnswer[i];
            if (answerPart>largestValue){
                secondLargest = largestValue;
                largestValue = answerPart;
                indexLargest = i;
            }
            else if (answerPart>secondLargest){
                secondLargest = answerPart;
            }
        }
        double difference = largestValue-secondLargest;
        double threshold = 0.25;
        if (difference>=threshold){
            return categories.get(indexLargest);
        }
        return "Not sure";
    }



    private boolean belowThreshold(double[] predictedAnswer,int indexLargest, double threshold){
        for (int i=0;i<predictedAnswer.length;i++){
            if (i!=indexLargest){
                double answerPart = predictedAnswer[i];
                if (answerPart>threshold){
                    return false;
                }
            }
        }
        return true;
    }

    private int aboveThreshold(double[] predictedAnswer, double threshold){
        double largestValue = 0;
        int indexLargest = 0;
        for (int i=0;i<predictedAnswer.length;i++){
            double answerPart = predictedAnswer[i];
            if (answerPart>largestValue){
                largestValue = answerPart;
                indexLargest = i;
            }
        }
        if (largestValue>=threshold){
            return indexLargest;
        }
        return -1;
    }

    public double[][] computeOutputs(Example example){

        double[] inputValues = example.getInputValues();

        double[] hiddenOutputs = calculateNeuronOutputs(hiddenNeurons,inputValues);
        double[] answerOutputs = calculateNeuronOutputs(answerNeurons,hiddenOutputs);

        return new double[][]{
                inputValues,
                hiddenOutputs,
                answerOutputs
        };
    }



    private void setInputWeights(Neuron[] neurons, double [] errorSignals, double[] inputValues){

        for (int i=0;i<neurons.length;i++){ //go through each neuron
            Neuron neuron = neurons[i];
            double errorSignal = errorSignals[i];
            neuron.updateInputWeight(errorSignal*learningRate,0); //for bias
            for (int u=0;u<inputValues.length;u++){ //go through each input
                double weightCorrection = errorSignal*inputValues[u]*learningRate;
                neuron.updateInputWeight(weightCorrection,u+1);
            }

        }
    }

    private double[] getHiddenErrorSignal(double[] nextErrorSignals, Neuron[] nextNeurons, Neuron[] theseNeurons,double[] theseOutputs) {
        double[] errorSignals = new double[theseNeurons.length];
        for (int i = 0; i < nextNeurons.length; i++) { //loop through neuron layer ahead
            Neuron nextNeuron = nextNeurons[i];
            double nextErrorSignal = nextErrorSignals[i];
            double[] inputWeights = nextNeuron.getInputWeights();
            for (int u = 0; u < theseNeurons.length; u++) { //add to the errorsignal of each of these neurons
                errorSignals[u] += nextErrorSignal * inputWeights[u];
            }
        }
        for (int i = 0; i < theseNeurons.length; i++) { //loop through each of these neurons
            double hiddenOutput = theseOutputs[i];
            double slope = hiddenOutput * (1 - hiddenOutput);
            errorSignals[i] = errorSignals[i]*slope; //difference*slope
        }
        return errorSignals;
    }

    private double[] getAnswerErrorSignals(double[] answerOutputs, double[] rightAnswerValues){
        double[] errorSignals = new double[answerOutputs.length];
        for (int i=0;i<answerOutputs.length;i++){
            double answerOutput = answerOutputs[i];
            double correctOutput = rightAnswerValues[i];
            double difference = correctOutput-answerOutput;
            double slope = answerOutput*(1-answerOutput);
            double errorSignal = difference*slope;
            errorSignals[i] = errorSignal;
        }
        return errorSignals;
    }

    private double[] calculateNeuronOutputs(Neuron[] neurons, double[] inputs){
        double[] outputs = new double[neurons.length];
        for (int i=0;i<neurons.length;i++){
            Neuron neuron = neurons[i];
            outputs[i] = neuron.calculateOutput(inputs);
        }
        return outputs;
    }
}
