package pl.edu.agh.muvto.visualisation;

import org.graphstream.graph.Edge;
import org.graphstream.graph.Graph;
import org.graphstream.graph.Node;
import pl.edu.agh.muvto.model.MuvtoEdge;
import pl.edu.agh.muvto.model.MuvtoGraph;
import pl.edu.agh.muvto.model.MuvtoVertex;
import pl.edu.agh.muvto.solver.MuvtoSolution;
import pl.edu.agh.muvto.visualisation.mapping.GraphMapping;

public class Visualiser{
    private Graph visualisedGraph;
    private MuvtoGraph referenceGraph;
    private GraphMapping mapping;


    public Visualiser(MuvtoGraph graph){
        mapping = new GraphMapping(graph);
        referenceGraph = graph;
        visualisedGraph = mapping.getGraph();
        visualisedGraph.addAttribute("ui.stylesheet", "" +
                "edge {" +
                //"shape: blob;" +
                //"size: 3px;" +
                "fill-mode: dyn-plain;" +
                "fill-color: green, red;" +
                //"arrow-shape: none;" +
                "}" +
                "node {" +
                //"size: 20px;" +
                "}");

        // Translating vertices to graphstream graph
        for(MuvtoVertex vertex: graph.vertexSet()){
            Node node = mapping.getVertex(vertex);
            node.addAttribute("ui.label", vertex.toString());
        }
        // Translating edges to graphstream graph
        for(MuvtoEdge edge: graph.edgeSet()){
            Edge visualisedEdge = mapping.getEdge(edge);
            visualisedEdge.addAttribute("ui.label", edge.toString());
        }
    }

    public void start() {
        // Curving edges with same vertices and opposite directions
        //System.setProperty("gs.ui.renderer", "org.graphstream.ui.j2dviewer.J2DGraphRenderer");
        updateColor();
        visualisedGraph.display();
    }

    public void updateGraph(MuvtoGraph graph, MuvtoSolution solution) {
        //todo jednostopniowe update.. może być ta funkcja
        mapping.updateGraph(graph);
        referenceGraph = graph;
        updateColor();
    }

    private void updateColor() {
        for(MuvtoEdge muvtoEdge: referenceGraph.edgeSet()){
            Edge e = mapping.getEdge(muvtoEdge);
            e.addAttribute("ui.color", muvtoEdge.getWeight());
        }
    }
}
