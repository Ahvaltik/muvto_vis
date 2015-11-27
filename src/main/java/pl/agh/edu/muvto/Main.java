package pl.agh.edu.muvto;

import java.net.URI;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.uma.jmetal.operator.CrossoverOperator;
import org.uma.jmetal.operator.MutationOperator;
import org.uma.jmetal.operator.SelectionOperator;
import org.uma.jmetal.operator.impl.crossover.SinglePointCrossover;
import org.uma.jmetal.operator.impl.mutation.BitFlipMutation;
import org.uma.jmetal.operator.impl.selection.BinaryTournamentSelection;
import org.uma.jmetal.problem.BinaryProblem;
import org.uma.jmetal.problem.singleobjective.OneMax;
import org.uma.jmetal.solution.BinarySolution;

import fj.P;
import fj.Try;
import fj.data.Either;
import fj.data.Stream;
import pl.agh.edu.muvto.model.MuvtoEdge;
import pl.agh.edu.muvto.model.MuvtoGraph;
import pl.agh.edu.muvto.model.MuvtoVertex;
import pl.agh.edu.muvto.solver.MuvtoSolver;
import pl.agh.edu.muvto.solver.builder.GeneticAlgorithmBuilderExt;

/**
 * Main class.
 */
public class Main {

    private static final Logger logger = LoggerFactory.getLogger(Main.class);

    /**
     * Application entry point.
     * @param args
     */
    public static void main(String[] args) {

        BinaryProblem problem = new OneMax(512) ;

        CrossoverOperator<BinarySolution> crossoverOperator =
                new SinglePointCrossover(0.9);

        MutationOperator<BinarySolution> mutationOperator =
                new BitFlipMutation(1.0 / problem.getNumberOfBits(0));

        SelectionOperator<List<BinarySolution>, BinarySolution>
            selectionOperator =
                new BinaryTournamentSelection<BinarySolution>();

        GeneticAlgorithmBuilderExt<BinarySolution> builder =
                (GeneticAlgorithmBuilderExt<BinarySolution>)
                new GeneticAlgorithmBuilderExt<>(
                        crossoverOperator,
                        mutationOperator)
                .setPopulationSize(100)
                .setMaxEvaluations(25000)
                .setSelectionOperator(selectionOperator);

        MuvtoSolver solver = new MuvtoSolver(builder);

        @SuppressWarnings("unused")
        BinarySolution solution = solver.solve(problem);

        loadGraph("test-graph-01.txt")
            .bimap(Util.liftVoid(Exception::printStackTrace),
                   Util.liftVoid(graph -> logger.info(graph.toString())));
    }

    /**
     * Loads graph from classpath resource.
     * @param classpathFile
     * @return loaded graph or exception
     */
    public static Either<Exception, MuvtoGraph>
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
