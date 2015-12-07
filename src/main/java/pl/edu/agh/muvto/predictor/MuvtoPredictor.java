package pl.edu.agh.muvto.predictor;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
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
    private double learningrate;
    private double momentum;
    private int epochs;
    private Boolean testData;
    private String pathToFile;

    public MuvtoPredictor(int N, int edgeId, double learningrate, double momentum, int epochs, Boolean testData){
        this.data = new NLengthList(N);
        this.learningrate = learningrate;
        this.momentum = momentum;
        this.epochs = epochs;
        this.testData = testData;
        
        try {
            this.pathToFile = File.createTempFile("data_" + edgeId, ".csv").getAbsolutePath();
        } catch (IOException e) {
            logger.error(e.getMessage());
            e.printStackTrace();
        }
    }
    
    public MuvtoPredictor(int edgeId){
        this(1000, edgeId, 0.01, 0.99, 1000, false);
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
                    .exec("python src/main/resources/prediction.py "
                              + this.pathToFile + " " + this.learningrate + " " + this.momentum
                              + " " + this.epochs + " " + this.testData + " " + value + " " + steps);
                 
                inputStream = new InputStreamReader(p.getInputStream());
                stdInput = new BufferedReader(inputStream);
     
                errorStream = new InputStreamReader(p.getErrorStream());
                stdError = new BufferedReader(errorStream);
     
                int iterator = 0;
                result = stdInput.lines().map(x -> Double.valueOf(x)).toArray(Double[]::new);
                
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
            new FileWriter(this.pathToFile));
        
        for (int i = 0; i < this.data.size(); i++) {
            outputWriter.write(this.data.get(i)+",");
        }
        
        outputWriter.flush();  
        outputWriter.close();  
    }
}
