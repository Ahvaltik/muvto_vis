package pl.edu.agh.muvto.solver;

import java.util.BitSet;
import java.util.Collection;
import java.util.Comparator;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.uma.jmetal.problem.impl.AbstractBinaryProblem;
import org.uma.jmetal.solution.BinarySolution;

import fj.P;
import fj.P2;
import fj.data.Stream;
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

    public MuvtoProblem(MuvtoGraph graph) {

        this.graph = graph;

        setNumberOfVariables(graph.vertexSet().size());
        setNumberOfObjectives(1);
        setName("MuvtoProblem");
    }

    @Override
    public void evaluate(BinarySolution solution) {

        int maxTransfer = 10;

        double meanWeight = graph.edgeSet().parallelStream()
                .mapToDouble(MuvtoEdge::getWeight)
                .sum() / graph.edgeSet().size();

        /** Maps edge index to fill delta */
        TreeMap<Integer, Integer> edgeDelta = Stream
                .stream(graph.vertexSet())
                .bind(vertex -> {

                    BitSet setup = solution.getVariableValue(vertex.getId());

                    Collection<MuvtoEdge> input
                        = graph.incomingEdgesOf(vertex).stream()
                        .sorted(Comparator.comparing(MuvtoEdge::getId))
                        .collect(Collectors.toList());

                    Collection<MuvtoEdge> output
                        = graph.outgoingEdgesOf(vertex).stream()
                        .sorted(Comparator.comparing(MuvtoEdge::getId))
                        .collect(Collectors.toList());

                    int outDegree = graph.outDegreeOf(vertex);

                    double outAttractiveness = graph.outgoingEdgesOf(vertex)
                            .stream().mapToDouble(MuvtoEdge::getAttractiveness)
                            .sum();

                    return Stream.stream(input).zipIndex()
                        .bind(inputEntry -> {

                            MuvtoEdge inEdge = inputEntry._1();
                            int inIndex = inputEntry._2();

                            int toTransfer = Math.max(inEdge.getFill(),
                                                      maxTransfer);

                            return Stream.stream(output).zipIndex()
                                    .filter(outputEntry -> {
                                        int outIndex = outputEntry._2();
                                        int bitIndex
                                            = inIndex*outDegree + outIndex;
                                        return setup.get(bitIndex);
                                    })
                                    .bind(outputEntry -> {

                                        MuvtoEdge outEdge = outputEntry._1();
                                        double factor
                                            = outEdge.getAttractiveness()
                                                / outAttractiveness;

                                        int delta = (int)(factor*toTransfer);

                                        @SuppressWarnings("unchecked")
                                        Stream<P2<MuvtoEdge, Integer>> result
                                            = Stream.stream(
                                                    P.p(outEdge, delta),
                                                    P.p(inEdge, -delta));

                                        return result;
                                    });
                        });
                })
                .toList()
                .groupBy(entry -> entry._1().getId())
                .map(deltas -> deltas.map(P2::_2).foldLeft((x,y) -> x+y, 0));
        
        logger.debug("delta map: " + edgeDelta);

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
