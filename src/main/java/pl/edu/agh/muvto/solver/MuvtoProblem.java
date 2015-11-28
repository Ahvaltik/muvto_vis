package pl.edu.agh.muvto.solver;

import java.util.BitSet;
import java.util.Collection;
import java.util.Comparator;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.uma.jmetal.problem.impl.AbstractBinaryProblem;
import org.uma.jmetal.solution.BinarySolution;

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

    public MuvtoProblem(MuvtoGraph graph) {

        this.graph = graph;

        setNumberOfVariables(graph.vertexSet().size());
        setNumberOfObjectives(1);
        setName("MuvtoProblem");
    }

    @Override
    public void evaluate(BinarySolution solution) {

        double meanWeight = graph.edgeSet().parallelStream()
                .mapToDouble(MuvtoEdge::getWeight)
                .sum() / graph.edgeSet().size();

        Collection<MuvtoEdge> updatedEdges = graph.vertexSet()
                .parallelStream()
                .map(vertex -> {

                    BitSet setup = solution.getVariableValue(vertex.getId());

                    Collection<MuvtoEdge> input
                        = graph.incomingEdgesOf(vertex).stream()
                        .sorted(Comparator.comparing(MuvtoEdge::getId))
                        .collect(Collectors.toList());

                    Collection<MuvtoEdge> output
                        = graph.outgoingEdgesOf(vertex).stream()
                        .sorted(Comparator.comparing(MuvtoEdge::getId))
                        .collect(Collectors.toList());

                    int toTransfer = 10;

                    // TODO transfer cars (reduce will work here, not map)

                    return input.iterator().next(); // dummy value
                })
                .collect(Collectors.toList());

        double meanDeviation = Math.sqrt(
                updatedEdges.parallelStream()
                    .mapToDouble(MuvtoEdge::getWeight)
                    .map(w -> (w-meanWeight)*(w-meanWeight))
                    .sum() / (graph.edgeSet().size())
                );

        logger.debug("objective: " + meanDeviation);

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

}
