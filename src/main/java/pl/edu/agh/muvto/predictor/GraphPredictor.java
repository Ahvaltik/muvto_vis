package pl.edu.agh.muvto.predictor;

import java.io.IOException;
import java.util.Map;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import fj.data.Stream;
import pl.edu.agh.muvto.model.MuvtoEdge;
import pl.edu.agh.muvto.model.MuvtoGraph;
import pl.edu.agh.muvto.model.MuvtoVertex;

/**
 * @author Grzegorz Mirek
 */
public class GraphPredictor {

    private static final Logger logger =
            LoggerFactory.getLogger(GraphPredictor.class);

    private Map<Integer, MuvtoPredictor> predictors;

    public GraphPredictor(MuvtoGraph graph) {
        predictors = graph.edgeSet().parallelStream().collect(Collectors.toMap(
                MuvtoEdge::getId,
                edge -> new MuvtoPredictor(edge.getId())));
    }

    public void updateData(MuvtoGraph graph) {
        graph.edgeSet().parallelStream().forEach(edge -> {
            try {
                double fill = edge.getFill();
                logger.info("[predictor {}] update with fill: {}",
                        edge.getId(), fill);
                predictors.get(edge.getId()).updateData(fill);
            } catch (IOException exc) {
                logger.error("Update failed", exc);
            }
        });
    }

    public MuvtoGraph getPredictedGraph(MuvtoGraph reference) {
        // FIXME this takes a little too long
        return Stream.stream(reference.edgeSet())
                .foldLeft((newGraph, oldEdge) -> {
                    int newFill = predictors
                            .get(oldEdge.getId())
                            .predict(Double.valueOf(oldEdge.getFill()))
                            .intValue();
                    logger.info("[predictor {}] {} predicted to {}",
                            oldEdge.getId(), oldEdge.getFill(), newFill);
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
