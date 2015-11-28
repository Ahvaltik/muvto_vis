package pl.edu.agh.muvto;

import java.net.URI;
import java.nio.file.Files;
import java.nio.file.Paths;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;
import org.springframework.stereotype.Component;
import org.uma.jmetal.solution.BinarySolution;

import fj.P;
import fj.Try;
import fj.data.Either;
import fj.data.Stream;
import pl.edu.agh.muvto.model.MuvtoEdge;
import pl.edu.agh.muvto.model.MuvtoGraph;
import pl.edu.agh.muvto.model.MuvtoVertex;
import pl.edu.agh.muvto.solver.MuvtoProblem;
import pl.edu.agh.muvto.solver.MuvtoSolver;

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
    }

    @Autowired
    private MuvtoSolver solver;

    private void start(String[] args) {

        loadGraph("test-graph-01.txt")
            .bimap(Util.liftVoid(Exception::printStackTrace),
                   Util.liftVoid(graph -> {

                       logger.debug("graph: "+ graph);

                       MuvtoProblem problem = new MuvtoProblem(graph);
//                       BinaryProblem problem = new OneMax(512) ;

                       @SuppressWarnings("unused")
                       BinarySolution solution = solver.solve(problem);

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
