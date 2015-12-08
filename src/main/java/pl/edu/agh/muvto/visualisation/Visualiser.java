package pl.edu.agh.muvto.visualisation;

import org.graphstream.graph.Edge;
import org.graphstream.graph.Graph;
import org.graphstream.graph.Node;
import org.graphstream.graph.implementations.SingleGraph;
import pl.edu.agh.muvto.model.MuvtoEdge;
import pl.edu.agh.muvto.model.MuvtoGraph;
import pl.edu.agh.muvto.model.MuvtoVertex;
import pl.edu.agh.muvto.solver.MuvtoSolution;

import java.util.HashMap;
import java.util.Iterator;

/**
 * Created by Pawel on 2015-12-06.
 */
public class Visualiser{
    private Graph visualisedGraph;
    private MuvtoGraph referenceGraph;
    private HashMap<MuvtoVertex,Node> vertexMap;
    private HashMap<MuvtoEdge,Edge> edgeMap;

    public Visualiser(MuvtoGraph graph){
        referenceGraph = graph;
        visualisedGraph = new SingleGraph("MuvtoGraph");
        vertexMap = new HashMap<MuvtoVertex, Node>();
        edgeMap = new HashMap<MuvtoEdge, Edge>();

        // Translating vertices to graphstream graph
        for(MuvtoVertex vertex: graph.vertexSet()){
            Node node = visualisedGraph.addNode(vertex.toString());
            node.addAttribute("ui.label", vertex.toString());

            //create map for further edition purposes
            vertexMap.put(vertex, node);
        }
        // Translating edges to graphstream graph
        for(MuvtoEdge edge: graph.edgeSet()){
            MuvtoVertex edgeSource = graph.getEdgeSource(edge);
            MuvtoVertex edgeTarget = graph.getEdgeTarget(edge);
            Edge visualisedEdge = visualisedGraph.addEdge(edge.toString(), edgeSource.toString(), edgeTarget.toString(), true);

            //create map for further edition purposes
            edgeMap.put(edge, visualisedEdge);
        }
    }

    public void start() {
        // Curving edges with same vertices and opposite directions
        System.setProperty("org.graphstream.ui.renderer", "org.graphstream.ui.j2dviewer.J2DGraphRenderer");
        Iterator<Edge> edgeIterator = visualisedGraph.getEdgeIterator();
        while(edgeIterator.hasNext()){
            Edge e = edgeIterator.next();
            e.addAttribute("ui.style", "fill-color: rgb(0,100,255);");
        }
        visualisedGraph.display();
    }

    public void updateGraph(MuvtoSolution solution) {

    }
}
