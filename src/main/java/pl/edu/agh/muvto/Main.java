package pl.edu.agh.muvto;

import java.io.IOException;
import java.net.URI;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;
import org.springframework.stereotype.Component;
import org.uma.jmetal.solution.BinarySolution;

import fj.F2;
import fj.P;
import fj.Try;
import fj.data.Either;
import fj.data.Stream;
import pl.edu.agh.muvto.model.MuvtoEdge;
import pl.edu.agh.muvto.model.MuvtoGraph;
import pl.edu.agh.muvto.model.MuvtoVertex;
import pl.edu.agh.muvto.predictor.GraphPredictor;
import pl.edu.agh.muvto.predictor.MuvtoPredictor;
import pl.edu.agh.muvto.solver.GraphTransformer;
import pl.edu.agh.muvto.solver.MuvtoProblem;
import pl.edu.agh.muvto.solver.MuvtoSolver;
import pl.edu.agh.muvto.solver.MuvtoSolverProvider;
import pl.edu.agh.muvto.util.Holder;
import pl.edu.agh.muvto.util.Util;

/**
 * Main class.
 */
@Component
public class Main {

    private static final Logger logger = LoggerFactory.getLogger(Main.class);

    /**
     * Application entry point.
     * @param args
     */
    public static void main(String[] args) {
      
        @SuppressWarnings("resource")
        ApplicationContext context = 
                new ClassPathXmlApplicationContext("applicationContext.xml");

        (context.getBean(Main.class)).start(args);
        
        //runPredictorSample();
    }
    
    private static void runPredictorSample(){
        /* PREDICTION SAMPLE */
        MuvtoPredictor pred = new MuvtoPredictor(0);
        
        try {
          pred.updateData(0.0);
          pred.updateData(2.0);
          pred.updateData(3.0);
  
        } catch (IOException e) {
          // TODO Auto-generated catch block
          e.printStackTrace();
        }
      
        System.out.println(pred.predict(2.0));
        System.out.println(Arrays.toString(pred.predict(2.0, 3)));
    }

    @Autowired
    MuvtoSolverProvider solverProvider;

    @Autowired
    private GraphTransformer transformer;

    @Value("${muvto.solver.maxTransfer}")
    private int maxTransfer;

    private void start(String[] args) {

        loadGraph("test-graph-01.txt")
            .bimap(Util.liftVoid(Exception::printStackTrace),
                   Util.liftVoid(initialGraph -> {

                       logger.debug("graph: " + initialGraph);

                       Holder<F2<MuvtoGraph, Integer, MuvtoGraph>> step
                           = new Holder<>();

                       GraphPredictor predictor = new GraphPredictor(initialGraph, maxTransfer);

                       step.f = (graph, i) -> {

                           logger.debug("fill: " + graph.edgeSet()
                               .stream().map(MuvtoEdge::getFill)
                               .collect(Collectors.toList()));

                           MuvtoProblem problem =new MuvtoProblem(graph,
                                                                  maxTransfer);
                           BinarySolution solution = solverProvider.getSolution(problem, 10);

                           logger.debug("setup: " + IntStream
                                   .range(0, solution.getNumberOfVariables())
                                   .mapToObj(solution::getVariableValueString)
                                   .collect(Collectors.joining()));

                           double objective = solution.getObjective(0);
                           logger.debug("objective: " + objective);

                           MuvtoGraph newGraph =
                                   transformer.graphFlow(graph,
                                                         solution,
                                                         maxTransfer);

                           logger.debug("difference: " + problem.distance(new MuvtoProblem(newGraph, maxTransfer)));

                           return (i > 0) ? step.f.f(newGraph, i-1) : newGraph;
                       };

                       final int steps = 10;
                       step.f.f(initialGraph, steps);

                       logger.debug("done");
                   }));
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
