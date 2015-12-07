package pl.edu.agh.muvto.predictor;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.Arrays;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import pl.edu.agh.muvto.solver.MuvtoSolver;

public class MuvtoPredictor {
  
    private static final Logger logger =
        LoggerFactory.getLogger(MuvtoPredictor.class);
    
    private NLengthList<Double> data;
    private int edgeId;
    private double learningrate;
    private double momentum;
    private int epochs;
    private Boolean testData;

    public MuvtoPredictor(int N, int edgeId, double learningrate, double momentum, int epochs, Boolean testData){
        this.data = new NLengthList(N);
        this.edgeId = edgeId;
        this.learningrate = learningrate;
        this.momentum = momentum;
        this.epochs = epochs;
        this.testData = testData;
    }
    
    public MuvtoPredictor(int edgeId){
        this.data = new NLengthList(1000);
        this.edgeId = edgeId;
        this.learningrate = 0.01;
        this.momentum = 0.99;
        this.epochs = 1000;
        this.testData = false;
    }
    
    public void updateData(Double sample) throws IOException{
        this.data.add(sample);

        writeDataToFile();
    }
    
    public Double predict(Double value){
        return predict(value, 1)[0];
    }
    
    public Double[] predict(Double value, int steps){
        
        Double[] result = new Double[steps];
      
        try{
            String s = null;
            InputStreamReader inputStream = null;
            BufferedReader stdInput = null;
            InputStreamReader errorStream = null;
            BufferedReader stdError = null;
            
            try {
                Process p = Runtime.getRuntime()
                    .exec("python src/main/resources/predictor/prediction.py "
                              + this.edgeId + " " + this.learningrate + " " + this.momentum
                              + " " + this.epochs + " " + this.testData + " " + value + " " + steps);
                 
                inputStream = new InputStreamReader(p.getInputStream());
                stdInput = new BufferedReader(inputStream);
     
                errorStream = new InputStreamReader(p.getErrorStream());
                stdError = new BufferedReader(errorStream);
     
                int iterator = 0;
                while ((s = stdInput.readLine()) != null) {
                    //System.out.println(s);
                    result[iterator] = new Double(s);
                    iterator++;
                }
                
                while ((s = stdError.readLine()) != null) {
                    logger.error(s);
                }
            }
            catch (IOException e) {
                System.out.println("exception happened: ");
                e.printStackTrace();
            }finally {
                inputStream.close();
                stdInput.close();
                errorStream.close();
                stdError.close();
            }
        }catch (IOException e) {
            e.printStackTrace();
        }
        
        return result;
    }
    
    public void writeDataToFile () throws IOException{
        BufferedWriter outputWriter = null;
        outputWriter = new BufferedWriter(
            new FileWriter("src/main/resources/predictor/prediction_data/data_" + this.edgeId + ".csv"));
        
        for (int i = 0; i < this.data.size(); i++) {
            outputWriter.write(this.data.get(i)+",");
        }
        
        outputWriter.flush();  
        outputWriter.close();  
    }
}
