package pl.edu.agh.muvto.solver;

import java.util.BitSet;
import java.util.Collection;
import java.util.Comparator;
import java.util.Random;
import java.util.stream.Collectors;

import org.springframework.stereotype.Component;
import org.uma.jmetal.solution.BinarySolution;

import fj.F;
import fj.F2;
import fj.P;
import fj.P2;
import fj.data.List;
import fj.data.Stream;
import fj.data.TreeMap;
import pl.edu.agh.muvto.model.MuvtoEdge;
import pl.edu.agh.muvto.model.MuvtoGraph;
import pl.edu.agh.muvto.model.MuvtoVertex;

/**
 * Applies solution to a graph.
 */
@Component
public class GraphTransformer {

    private Random prng = new Random();

    public MuvtoGraph graphFlow(MuvtoGraph graph,
                                BinarySolution solution,
                                int maxTransfer) {
        return applyEdgeDelta(graph,
                              calculateEdgeDelta(graph,
                                                 solution,
                                                 maxTransfer));
    }

    public MuvtoGraph graphTrafficDelta(MuvtoGraph graph,
                                        int maxDelta) {
        int d = maxDelta;
        int d2 = 2*maxDelta;
        return applyEdgeDelta(graph,
                              List.list(graph.edgeSet())
                                  .groupBy(MuvtoEdge::getId)
                                  .map((e) -> Math.max(prng.nextInt(d2)-d, 0)));
    }

    private MuvtoGraph applyEdgeDelta(MuvtoGraph graph,
                                      TreeMap<Integer, Integer> edgeDelta) {
        return Stream.stream(graph.edgeSet())
                .foldLeft((newGraph, oldEdge) -> {
                    int deltaFill = edgeDelta.get(oldEdge.getId()).orSome(0);
                    int newFill = oldEdge.getFill() + deltaFill;
                    MuvtoVertex srcVertex = graph.getEdgeSource(oldEdge);
                    MuvtoVertex tgtVertex = graph.getEdgeTarget(oldEdge);
                    newGraph.addVertex(srcVertex);
                    newGraph.addVertex(tgtVertex);
                    newGraph.addEdge(srcVertex,
                                     tgtVertex,
                                     oldEdge.withFill(newFill));
                    return newGraph;
                }, new MuvtoGraph());
    }

    public TreeMap<Integer, Integer> calculateEdgeDelta(MuvtoGraph graph,
                                                        BinarySolution solution,
                                                        int maxTransfer) {

        F<MuvtoVertex, Stream<P2<MuvtoEdge, Integer>>> boundVertexFlow = v ->
            vertexFlow(graph,
                       v,
                       solution.getVariableValue(v.getId()),
                       maxTransfer);

        return Stream.stream(graph.vertexSet())
                .bind(boundVertexFlow)
                .toList()
                .groupBy(entry -> entry._1().getId())
                .map(deltas -> deltas.map(P2::_2).foldLeft((x,y) -> x+y, 0));
    }

    private F<P2<MuvtoEdge, Integer>, Boolean> isPathEnabled(BitSet setup,
                                                             int inIndex,
                                                             int outDegree) {
        return product -> {
            int outIndex = product._2();
            int bitIndex = inIndex*outDegree + outIndex;
            return setup.get(bitIndex);
        };
    }

    private  F2<P2<List<P2<MuvtoEdge, Integer>>, Integer>, MuvtoEdge,
    P2<List<P2<MuvtoEdge, Integer>>, Integer>> reduceEdge(int totalTransfer,
                                                          double totalAttr) {
        return (product, outEdge) -> {
            int remaining = product._2();
            double factor = outEdge.getBaseAttractiveness() / totalAttr;
            int delta = Math.min(remaining, (int)(factor*totalTransfer));
            return P.p(product._1().cons(P.p(outEdge, delta)),
                                             remaining-delta);
        };
    }

    private Stream<P2<MuvtoEdge, Integer>> vertexFlow(MuvtoGraph graph,
                                                      MuvtoVertex vertex,
                                                      BitSet flowSetup,
                                                      int maxTransfer) {
        
        Collection<MuvtoEdge> input = graph.incomingEdgesOf(vertex).stream()
                .sorted(Comparator.comparing(MuvtoEdge::getId))
                .collect(Collectors.toList());

        Collection<MuvtoEdge> output = graph.outgoingEdgesOf(vertex).stream()
                .sorted(Comparator.comparing(MuvtoEdge::getId))
                .collect(Collectors.toList());

        int outDegree = graph.outDegreeOf(vertex);

        double outAttractiveness = graph.outgoingEdgesOf(vertex).stream()
                .mapToDouble(MuvtoEdge::getEffectiveAttractiveness).sum();

        return Stream.stream(input).zipIndex().bind(inputEntry -> {

            MuvtoEdge inEdge = inputEntry._1();
            int inIndex = inputEntry._2();

            int toTransfer = Math.min(inEdge.getFill(), maxTransfer);

            return Stream.stream(output).zipIndex()
                    .filter(isPathEnabled(flowSetup, inIndex, outDegree))
                    .map(P2::_1)
                    .foldLeft(reduceEdge(toTransfer, outAttractiveness),
                              P.p(List.nil(), toTransfer))
                    .cobind(product -> {
                        List<P2<MuvtoEdge, Integer>> deltaList = product._1();
                        int remaining = product._2();
                        int srcFillDelta = -toTransfer + remaining;
                        return deltaList.cons(P.p(inEdge, srcFillDelta));
                    })._1().toStream();
        });
    }

}
