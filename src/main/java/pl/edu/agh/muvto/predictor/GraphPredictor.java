package pl.edu.agh.muvto.predictor;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import pl.edu.agh.muvto.model.MuvtoEdge;
import pl.edu.agh.muvto.model.MuvtoGraph;

import java.io.IOException;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * @author Grzegorz Mirek
 */
public class GraphPredictor {

    private static final Logger logger = LoggerFactory.getLogger(GraphPredictor.class);

    private Map<Integer, MuvtoPredictor> predictors;

    public GraphPredictor(MuvtoGraph graph) {
        predictors = graph.edgeSet().parallelStream()
                .collect(Collectors.toMap(MuvtoEdge::getId, edge ->
                        new MuvtoPredictor(edge.getId())));
    }

    public void updateData(MuvtoGraph graph) {
        graph.edgeSet().parallelStream().forEach(edge -> {
            try {
                predictors.get(edge.getId()).updateData(edge.getWeight());
            } catch (IOException exc) {
                logger.error("Update failed", exc);
            }
        });
    }
}
