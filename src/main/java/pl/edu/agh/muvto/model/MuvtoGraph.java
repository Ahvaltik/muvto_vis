package pl.edu.agh.muvto.model;

import java.util.stream.Collectors;

import org.jgrapht.EdgeFactory;
import org.jgrapht.graph.DefaultDirectedWeightedGraph;

public class MuvtoGraph
    extends DefaultDirectedWeightedGraph<MuvtoVertex, MuvtoEdge> {

    private static final long serialVersionUID = 1L;

    public MuvtoGraph() {
        this(MuvtoEdge.class);
    }

    public MuvtoGraph(Class<? extends MuvtoEdge> edgeClass) {
        super(edgeClass);
    }

    public MuvtoGraph(EdgeFactory<MuvtoVertex, MuvtoEdge> ef) {
        super(ef);
    }

    public double distanceTo(MuvtoGraph other) {
        return java.util.stream.Stream
            .concat(this.edgeSet().stream(), other.edgeSet().stream())
            .collect(Collectors.groupingBy(MuvtoEdge::getId))
            .values()
            .stream()
            .mapToDouble(edges -> edges.get(0).distanceTo(edges.get(1)))
            .sum() / this.edgeSet().size();
    }
}
