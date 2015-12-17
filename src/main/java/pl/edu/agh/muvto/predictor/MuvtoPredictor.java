package pl.edu.agh.muvto.predictor;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class MuvtoPredictor {

    private static final Logger logger =
            LoggerFactory.getLogger(MuvtoPredictor.class);
    
    private static final String PREDICTOR_SCRIPT =
            "src/main/resources/prediction.py";

    private NLengthList<Double> data;
    private double learningrate;
    private double momentum;
    private int epochs;
    private Boolean testData;
    private String pathToFile;

    public MuvtoPredictor(int N,
                          int edgeId,
                          double learningrate,
                          double momentum,
                          int epochs,
                          Boolean testData) {

        this.data = new NLengthList<>(N);
        this.learningrate = learningrate;
        this.momentum = momentum;
        this.epochs = epochs;
        this.testData = testData;

        try {
            this.pathToFile = File
                    .createTempFile("data_" + edgeId, ".csv")
                    .getAbsolutePath();
        } catch (IOException e) {
            logger.error(e.getMessage());
            e.printStackTrace();
        }
    }

    public MuvtoPredictor(int edgeId){
        this(1000, edgeId, 0.01, 0.99, 1000, false);
    }

    public void updateData(Double sample) throws IOException {
        this.data.add(sample);
        writeDataToFile();
    }

    public Double predict(Double value){
        return predict(value, 1)[0];
    }

    public Double[] predict(Double value, int steps) {

        Double[] result = new Double[steps];

        try {
            InputStreamReader inputStream = null;
            BufferedReader stdInput = null;
            InputStreamReader errorStream = null;
            BufferedReader stdError = null;

            try {
                Process p = Runtime.getRuntime()
                        .exec(new String[]{
                                "python",
                                PREDICTOR_SCRIPT,
                                this.pathToFile,
                                String.valueOf(this.learningrate),
                                String.valueOf(this.momentum),
                                String.valueOf(this.epochs),
                                String.valueOf(this.testData),
                                String.valueOf(value),
                                String.valueOf(steps)
                        });

                inputStream = new InputStreamReader(p.getInputStream());
                stdInput = new BufferedReader(inputStream);

                errorStream = new InputStreamReader(p.getErrorStream());
                stdError = new BufferedReader(errorStream);

                result = stdInput.lines()
                        .map(Double::valueOf)
                        .toArray(Double[]::new);

                stdError.lines().forEach(logger::error);

                p.waitFor();
            }
            catch (IOException | InterruptedException e) {
                System.out.println("exception happened: ");
                e.printStackTrace();
            } finally {
                inputStream.close();
                stdInput.close();
                errorStream.close();
                stdError.close();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        return result;
    }

    public void writeDataToFile() throws IOException {
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
