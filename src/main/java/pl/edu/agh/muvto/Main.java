package pl.edu.agh.muvto;

import java.io.IOException;
import java.net.URI;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Arrays;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.support.ClassPathXmlApplicationContext;
import org.springframework.stereotype.Component;

import fj.P;
import fj.Try;
import fj.data.Either;
import fj.data.Stream;
import pl.edu.agh.muvto.model.MuvtoEdge;
import pl.edu.agh.muvto.model.MuvtoGraph;
import pl.edu.agh.muvto.model.MuvtoVertex;
import pl.edu.agh.muvto.predictor.MuvtoPredictor;
import pl.edu.agh.muvto.util.Util;

/**
 * Main class.
 */
@Component
public class Main {

    private static final Logger logger = LoggerFactory.getLogger(Main.class);

    @Autowired
    private Simulation simulation;

    /**
     * Application entry point.
     * @param args
     */
    public static void main(String[] args) {
    
        try (ClassPathXmlApplicationContext context
            = new ClassPathXmlApplicationContext("applicationContext.xml"))
        {
            (context.getBean(Main.class)).start(args);
            logger.info("done");
        }
    }

    private void start(String[] args) {
        loadGraph("test-graph-01.txt")
            .bimap(Util.liftVoid(Exception::printStackTrace),
                   Util.liftVoid(simulation::runSimulation));
    }

    @SuppressWarnings("unused")
    private static void runPredictorSample() {

      MuvtoPredictor pred = new MuvtoPredictor(0);
      try {
        
        pred.updateData(1.0);
        pred.updateData(2.0);
        pred.updateData(3.0);
 
        /*prediction is executed for the first time. 
         * whole network is being initialized
         * network trains itself on previous data
         * the longest part of "predict" execution
         */
        long time1 = System.currentTimeMillis();
        System.out.println(pred.predict(3.0));
        

        /*prediction without new data
         * network has been already initialized
         * so it is read from tmp file
         * shortest execution of "predict"
         * */
        long time2 = System.currentTimeMillis();
        System.out.println(time2 - time1);
        System.out.println(pred.predict(1.0));
        
        
        /*One new data has been added
         *network has been already initialized
         *short execution of "predict"
         **/
        long time3 = System.currentTimeMillis();
        System.out.println(time3 - time2);
        pred.updateData(3.0);
        System.out.println(pred.predict(2.0));
        
        
        long time4 = System.currentTimeMillis();
        System.out.println(time4 - time3);
        
        System.out.println(pred.predict());
        
      } catch (IOException e) {
        // TODO Auto-generated catch block
        e.printStackTrace();
      }
    }

    /**
     * Loads graph from classpath resource.
     * @param classpathFile
     * @return loaded graph or exception
     */
    private Either<Exception, MuvtoGraph>
        loadGraph(String classpathFile) {

        return Try.f(() -> {
            ClassLoader classloader = Main.class.getClassLoader();
            URI uri = classloader.getResource(classpathFile).toURI();
            return Stream.stream(Files.readAllLines(Paths.get(uri)))
                .filter(s -> !s.startsWith("#"))
                .map(line -> line.split("\\s+"))
                .zipIndex()
                .map(entry -> {
                    String[] row = entry._1();
                    int index = entry._2();
                    return P.p(new MuvtoVertex(Integer.parseInt(row[0])),
                               new MuvtoVertex(Integer.parseInt(row[1])),
                               new MuvtoEdge(index,
                                             Integer.parseInt(row[2]),
                                             Integer.parseInt(row[3]),
                                             Double.parseDouble(row[4]))
                              );
                })
                .foldLeft((graph, data) -> {
                    graph.addVertex(data._1());
                    graph.addVertex(data._2());
                    graph.addEdge(data._1(), data._2(), data._3());
                    return graph;
                }, new MuvtoGraph(MuvtoEdge.class));
        })._1().toEither();
    }
}
