package pl.edu.agh.muvto;

import java.util.List;
import java.util.function.Function;
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
import pl.edu.agh.muvto.visualisation.VisualisationController;

@Component
public class Simulation {

    private static final Logger logger
        = LoggerFactory.getLogger(Simulation.class);

    @Autowired
    private MuvtoSolverProvider solverProvider;

    @Autowired
    private GraphTransformer transformer;

    @Value("${muvto.solver.maxTransfer}")
    private int maxTransfer;

    @Value("${muvto.solver.maxDelta}")
    private int maxDelta;

    @Value("${muvto.predictor.maxAllowedDist}")
    private double maxAllowedDist;

    @Value("${muvto.simulation.steps}")
    private int steps;

    @Value("${muvto.simulation.sleep}")
    private int sleep;

    private VisualisationController visualisationController;
    
    private static final boolean VISUALIZATION = true;

    public void runSimulation(MuvtoGraph initialGraph) {

        if (VISUALIZATION) {
            visualisationController = new VisualisationController();
        }

        logger.debug("graph: " + initialGraph);
        
        if (VISUALIZATION) {
            visualisationController.initializeGraphDisplay(initialGraph);
        }

        Holder<F2<MuvtoGraph, Integer, MuvtoGraph>> step
            = new Holder<>();

        GraphPredictor predictor = new GraphPredictor(initialGraph);
        predictor.updateData(initialGraph);

        step.f = (graph, i) -> {

            MuvtoGraph newGraph = simulationStep(graph, predictor);

            try {
                Thread.sleep(sleep);
            } catch(InterruptedException e) {
                logger.error("Interrupted", e);
            }

            return (i > 0) ? step.f.f(newGraph, i-1) : newGraph;
        };

        step.f.f(initialGraph, steps);

        if (VISUALIZATION) {
            visualisationController.stopVisualisation();
        }

        logger.debug("done");
    }

    private MuvtoGraph simulationStep(MuvtoGraph graph,
                                      GraphPredictor predictor) {

        logger.debug("STEP -------------------------------------------------");

//        logger.debug(" fill: " + extractEdgeAttrs(graph, MuvtoEdge::getFill));
//        logger.debug(" attr: " + extractEdgeAttrs(graph,
//                                    MuvtoEdge::getEffectiveAttractiveness));

        MuvtoProblem problem = new MuvtoProblem(graph, maxTransfer);

        BinarySolution solution = solverProvider.getSolution(problem,
                                                             maxAllowedDist);

        logger.debug("setup: " + IntStream
                .range(0, solution.getNumberOfVariables())
                .mapToObj(solution::getVariableValueString)
                .collect(Collectors.joining()));

        logger.debug("objective: " + solution.getObjective(0));

        MuvtoGraph newGraph
            = transformer.graphFlow(graph, solution, maxTransfer);

//        newGraph
//            = transformer.graphTrafficDelta(newGraph, maxDelta);

        if (VISUALIZATION) {
            visualisationController.updateGraph(newGraph, solution);
        }

        predictor.updateData(graph);
        MuvtoGraph predictedGraph = predictor.getPredictedGraph(graph);
        solverProvider.addPredictedProblem(new MuvtoProblem(predictedGraph,
                                                            maxTransfer));

        // TODO consider predicting more entries here

        logger.debug(" distance: " + graph.distanceTo(newGraph));

        return newGraph;
    }

    public static <T> List<T> extractEdgeAttrs(MuvtoGraph graph,
                                                Function<MuvtoEdge, T> f) {
        return graph.edgeSet()
                .stream().map(f)
                .collect(Collectors.toList());
    }
}
