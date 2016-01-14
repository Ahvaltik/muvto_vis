package pl.edu.agh.muvto.predictor;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import fj.data.Stream;
import pl.edu.agh.muvto.model.MuvtoGraph;
import pl.edu.agh.muvto.model.MuvtoVertex;

/**
 * @author Grzegorz Mirek
 */
public class GraphPredictor {

    private static final Logger logger =
            LoggerFactory.getLogger(GraphPredictor.class);

    private Map<Integer, MuvtoPredictor> predictors;
    private Map<Integer, Integer> fixedFillEdges;

    // predictors on X% of edges
    double predictorsSoftLimit = 0.5;
    int predictorsHardLimit = 20;

    public GraphPredictor(MuvtoGraph graph) {

        // for uniform distribution
        double predictorsSafeThreshold = Math.min(predictorsSoftLimit,
                                                   predictorsHardLimit
                                                   / graph.edgeSet().size());

        // put predictors on random edges
        Random prng = new Random(1);

        predictors = new HashMap<>();
        fixedFillEdges = new HashMap<>();

        graph.edgeSet().parallelStream().forEach(edge -> {
            if (prng.nextDouble() < predictorsSafeThreshold) {
                predictors.put(edge.getId(), new MuvtoPredictor(edge.getId()));
            } else {
                fixedFillEdges.put(edge.getId(), edge.getFill());
            }
        });
    }

    public void updateData(MuvtoGraph graph) {
        graph.edgeSet().parallelStream().forEach(edge -> {
            try {
                double fill = edge.getFill();
                int id = edge.getId();
                MuvtoPredictor predictor = predictors.get(id);
                if (predictor != null) {
                    logger.info("[predictor {}] update with fill: {}",
                                id,
                                fill);
                    predictor.updateData(fill);
                } else {
                    fixedFillEdges.put(id, (int) fill);
                }
            } catch (IOException exc) {
                logger.error("Update failed", exc);
            }
        });
    }

    public MuvtoGraph getPredictedGraph(MuvtoGraph reference) {
        return Stream.stream(reference.edgeSet())
                .foldLeft((newGraph, oldEdge) -> {
                    int id = oldEdge.getId();
                    MuvtoPredictor predictor = predictors.get(id);
                    int newFill = (predictor != null)
                            ? predictor.predict().intValue()
                            : fixedFillEdges.get(id);
                    if (predictor != null) {
                        logger.info("[predictor {}] {} predicted to {}",
                                oldEdge.getId(), oldEdge.getFill(), newFill);
                    }
                    MuvtoVertex srcVertex = reference.getEdgeSource(oldEdge);
                    MuvtoVertex tgtVertex = reference.getEdgeTarget(oldEdge);
                    newGraph.addVertex(srcVertex);
                    newGraph.addVertex(tgtVertex);
                    newGraph.addEdge(srcVertex,
                                     tgtVertex,
                                     oldEdge.withFill(newFill));
                    return newGraph;
                }, new MuvtoGraph());
    }
}
