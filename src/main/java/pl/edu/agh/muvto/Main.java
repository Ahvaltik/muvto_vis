package pl.edu.agh.muvto;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Random;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.support.ClassPathXmlApplicationContext;
import org.springframework.stereotype.Component;

import fj.F;
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
            if (args.length > 0) {
                (context.getBean(Main.class)).start(args[0]);
                logger.info("done");
            } else {
                logger.error("Missing required graph file path.");
            }
        }
    }

    private void start(String graphFilePath) {
        loadGraph(graphFilePath)
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

            /*
             * prediction is executed for the first time. whole network is being
             * initialized network trains itself on previous data the longest
             * part of "predict" execution
             */
            long time1 = System.currentTimeMillis();
            System.out.println(pred.predict(3.0));

            /*
             * prediction without new data network has been already initialized
             * so it is read from tmp file shortest execution of "predict"
             */
            long time2 = System.currentTimeMillis();
            System.out.println(time2 - time1);
            System.out.println(pred.predict(1.0));

            /*
             * One new data has been added network has been already initialized
             * short execution of "predict"
             **/
            long time3 = System.currentTimeMillis();
            System.out.println(time3 - time2);
            pred.updateData(3.0);
            System.out.println(pred.predict(2.0));

            long time4 = System.currentTimeMillis();
            System.out.println(time4 - time3);

            System.out.println(pred.predict());

        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    
    private <T,V> T getOr(V[] tab, int index, F<V,T> f, T def) {
        if (index < tab.length) {
            return f.f(tab[index]);
        } else {
            return def;
        }
    }

    private Either<IOException, MuvtoGraph> loadGraph(String filePath) {

        Random prng = new Random(1);

        return Try.f(() -> {
            return Stream.stream(Files.readAllLines(Paths.get(filePath)))
                .filter(s -> !s.startsWith("#"))
                .map(line -> line.split("\\s+"))
                .zipIndex()
                .map(entry -> {
                    String[] row = entry._1();
                    int index = entry._2();

                    int fromVertexId = Integer.parseInt(row[0]);
                    int toVertexId = Integer.parseInt(row[1]);

                    int capacity = getOr(row,
                                         2,
                                         Integer::parseInt,
                                         20 + prng.nextInt(101));
                    int fill = getOr(
                            row,
                            3,
                            Integer::parseInt,
                            (int)(capacity + .5*(1. + prng.nextDouble())));
                    double attract = getOr(row, 4, Double::parseDouble, 1.0);

                    return P.p(new MuvtoVertex(fromVertexId),
                               new MuvtoVertex(toVertexId),
                               new MuvtoEdge(index,
                                             capacity,
                                             fill,
                                             attract));
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
