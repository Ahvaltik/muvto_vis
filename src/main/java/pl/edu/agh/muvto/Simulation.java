package pl.edu.agh.muvto;

import java.util.stream.Collectors;
import java.util.stream.IntStream;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.uma.jmetal.solution.BinarySolution;

import fj.F2;
import pl.edu.agh.muvto.model.MuvtoEdge;
import pl.edu.agh.muvto.model.MuvtoGraph;
import pl.edu.agh.muvto.predictor.GraphPredictor;
import pl.edu.agh.muvto.solver.GraphTransformer;
import pl.edu.agh.muvto.solver.MuvtoProblem;
import pl.edu.agh.muvto.solver.MuvtoSolverProvider;
import pl.edu.agh.muvto.util.Holder;

@Component
public class Simulation {

    @Autowired
    private MuvtoSolverProvider solverProvider;

    @Autowired
    private GraphTransformer transformer;

    @Value("${muvto.solver.maxTransfer}")
    private int maxTransfer;

    private static final Logger logger
        = LoggerFactory.getLogger(Simulation.class);

    public void runSimulation(MuvtoGraph initialGraph) {

        logger.debug("graph: " + initialGraph);

        MuvtoProblem initialProblem = new MuvtoProblem(initialGraph,
                                                       maxTransfer);

        solverProvider.addPredictedProblem(initialProblem);

        try {
            Thread.sleep(5000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        solverProvider.getSolution(initialProblem, 10);

        Holder<F2<MuvtoGraph, Integer, MuvtoGraph>> step
            = new Holder<>();

        GraphPredictor predictor = new GraphPredictor(initialGraph,
                                                      maxTransfer);

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

            logger.debug("difference: " + problem.distance(
                    new MuvtoProblem(newGraph, maxTransfer)));

            return (i > 0) ? step.f.f(newGraph, i-1) : newGraph;
        };

        final int steps = 10;
        step.f.f(initialGraph, steps);

        logger.debug("done");
    }
}
