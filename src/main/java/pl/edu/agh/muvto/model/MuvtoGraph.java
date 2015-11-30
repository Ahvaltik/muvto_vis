package pl.edu.agh.muvto.model;

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
}
