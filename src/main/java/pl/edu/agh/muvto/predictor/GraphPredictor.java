package pl.edu.agh.muvto.predictor;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.uma.jmetal.solution.BinarySolution;
import pl.edu.agh.muvto.model.MuvtoEdge;
import pl.edu.agh.muvto.model.MuvtoGraph;
import pl.edu.agh.muvto.solver.MuvtoProblem;

import java.io.IOException;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * @author Grzegorz Mirek
 */
public class GraphPredictor {

    private static final Logger logger =
            LoggerFactory.getLogger(GraphPredictor.class);
    private Map<Integer, MuvtoPredictor> predictors;
    private PredictionExecutor executor;
    private int maxTransfer;

    public GraphPredictor(MuvtoGraph graph, int maxTransfer) {
        predictors = graph.edgeSet().parallelStream()
                .collect(Collectors.toMap(MuvtoEdge::getId, edge ->
                        new MuvtoPredictor(edge.getId())));
        this.maxTransfer = maxTransfer;
    }

    public void updateData(MuvtoGraph graph) {
        graph.edgeSet().parallelStream().forEach(edge -> {
            try {
                predictors.get(edge)
                        .updateData(edge.getWeight());
            } catch (IOException exc) {
                logger.error("Update failed", exc);
            }
        });
    }

    public BinarySolution getPredictionOrCalculate(MuvtoGraph graph) {
        MuvtoProblem problem = new MuvtoProblem(graph, maxTransfer);
        return executor.getSolution(problem);
    }
}
