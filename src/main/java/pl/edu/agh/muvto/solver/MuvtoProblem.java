package pl.edu.agh.muvto.solver;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.uma.jmetal.problem.impl.AbstractBinaryProblem;
import org.uma.jmetal.solution.BinarySolution;

import fj.data.TreeMap;
import pl.edu.agh.muvto.model.MuvtoEdge;
import pl.edu.agh.muvto.model.MuvtoGraph;

/**
 * Muvto problem.
 */
public class MuvtoProblem extends AbstractBinaryProblem {

    private static final long serialVersionUID = 1L;

    private static final Logger logger
        = LoggerFactory.getLogger(MuvtoProblem.class);

    private MuvtoGraph graph;
    private int maxTransfer;

    public MuvtoProblem(MuvtoGraph graph, int maxTransfer) {

        this.graph = graph;
        this.maxTransfer = maxTransfer;

        setNumberOfVariables(graph.vertexSet().size());
        setNumberOfObjectives(1);
        setName("MuvtoProblem");
    }

    @Override
    public void evaluate(BinarySolution solution) {

        double meanWeight = graph.edgeSet().parallelStream()
                .mapToDouble(MuvtoEdge::getWeight)
                .sum() / graph.edgeSet().size();

        TreeMap<Integer, Integer> edgeDelta = new GraphTransformer()
            .calculateEdgeDelta(graph, solution, maxTransfer);

        double meanDeviation = Math.sqrt(
            graph.edgeSet().stream()
                .mapToDouble(edge ->
                    edgeDelta.get(edge.getId())
                        .map(delta -> delta + edge.getFill())
                        .orSome(edge::getFill) / (double) edge.getCapacity()
                )
                .map(w -> (w-meanWeight)*(w-meanWeight))
                .sum() / (double) (graph.edgeSet().size())
        );

//        logger.debug("objective: " + meanDeviation);

        solution.setObjective(0, meanDeviation);
    }

    @Override
    protected int getBitsPerVariable(int variableIndex) {
        return graph.vertexSet().stream()
            .filter(v -> v.getId() == variableIndex)
            .findFirst()
            .map(v -> graph.outDegreeOf(v) * graph.inDegreeOf(v))
            .orElseThrow(() ->
                new RuntimeException("Invalid variable number")
            );
    }
    
    public float distance(MuvtoProblem secondProblem){
    	float dist = 0;
    	for(MuvtoEdge edge : this.graph.edgeSet()){
    		dist += Math.abs(edge.getFill() - secondProblem.graph.getEdge(this.graph.getEdgeSource(edge), this.graph.getEdgeTarget(edge)).getFill());
    	}
    	dist /= this.graph.edgeSet().size();
    	return dist;
    }

}
