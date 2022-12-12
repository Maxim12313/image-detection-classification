//Maxim Kim
public class Neuron{
    private double[] inputWeights;

    public Neuron(int inputNum){
        inputWeights = new double[inputNum+1]; //0 is bias
        for (int i=0;i<inputNum;i++){
            inputWeights[i] = -0.05+Math.random()*0.10;
        }
    }

    public double[] getInputWeights(){
        return inputWeights;
    }

    public void updateInputWeight(double weightCorrection, int index){
        inputWeights[index]+=weightCorrection;
    }

    public double calculateOutput(double[] inputs){
        double x = inputWeights[0];
        for (int i=0;i<inputs.length;i++){
            x+=inputs[i]*inputWeights[i+1];
        }
        //sum all inputs*weights in x
        return 1/(1+Math.exp(-x)); //apply the sigmoid function
    }
}
