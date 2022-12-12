//Maxim Kim
public class Example {
    protected double[] inputValues;
    protected String rightAnswer;

    protected Example(){

    }

    public Example(double[] inputValues){
        this.inputValues = inputValues;
    }

    public Example(double[] inputValues, String rightAnswer){
        this.inputValues = inputValues;
        this.rightAnswer = rightAnswer;
    }
    public double[] getInputValues(){
        return inputValues;
    }
    public String getRightAnswer(){
        return rightAnswer;
    }
}
