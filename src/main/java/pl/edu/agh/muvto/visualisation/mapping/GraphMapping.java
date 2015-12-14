package pl.edu.agh.muvto.visualisation.mapping;

import org.graphstream.graph.Edge;
import org.graphstream.graph.Graph;
import org.graphstream.graph.Node;
import org.graphstream.graph.implementations.SingleGraph;
import pl.edu.agh.muvto.model.MuvtoEdge;
import pl.edu.agh.muvto.model.MuvtoGraph;
import pl.edu.agh.muvto.model.MuvtoVertex;

import java.util.HashMap;

public class GraphMapping {
    private HashMap<MuvtoVertex,Node> vertexMap;
    private HashMap<MuvtoEdge,Edge> edgeMap;
    Graph mGraph;

    public GraphMapping(MuvtoGraph graph) {
        mGraph = new SingleGraph(graph.toString());
        vertexMap = new HashMap<>();
        edgeMap = new HashMap<>();

        // Translating vertices to graphstream graph
        for(MuvtoVertex vertex: graph.vertexSet()){
            Node node = mGraph.addNode(vertex.toString());

            //create map for further edition purposes
            vertexMap.put(vertex, node);
        }
        // Translating edges to graphstream graph
        for(MuvtoEdge edge: graph.edgeSet()){
            MuvtoVertex edgeSource = graph.getEdgeSource(edge);
            MuvtoVertex edgeTarget = graph.getEdgeTarget(edge);
            Edge visualisedEdge = mGraph.addEdge(edge.toString(), edgeSource.toString(), edgeTarget.toString(), true);

            //create map for further edition purposes
            edgeMap.put(edge, visualisedEdge);
        }
    }

    public Graph getGraph() {
        return mGraph;
    }

    public Edge getEdge(MuvtoEdge edge){
        return edgeMap.get(edge);
    }

    public Node getVertex(MuvtoVertex vertex){
        return vertexMap.get(vertex);
    }
}
