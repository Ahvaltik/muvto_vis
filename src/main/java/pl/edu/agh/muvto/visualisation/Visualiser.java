package pl.edu.agh.muvto.visualisation;

import org.graphstream.graph.Edge;
import org.graphstream.graph.Graph;
import org.graphstream.graph.Node;
import org.graphstream.ui.view.View;
import org.graphstream.ui.view.Viewer;
import org.uma.jmetal.solution.BinarySolution;
import pl.edu.agh.muvto.model.MuvtoEdge;
import pl.edu.agh.muvto.model.MuvtoGraph;
import pl.edu.agh.muvto.model.MuvtoVertex;
import pl.edu.agh.muvto.visualisation.mapping.GraphMapping;

import javax.swing.*;
import java.awt.*;

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
                "shape: blob;" +
                "size: 3px;" +
                "fill-mode: dyn-plain;" +
                "fill-color: green, red;" +
                "arrow-size: 40px, 7px;" +
                "text-mode: hidden;" +
                "}" +
                "node {" +
                "size: 10px;" +
                "text-alignment: at-right;" +
                "text-padding: 3px, 2px;" +
                "text-background-mode: rounded-box;" +
                "text-background-color: #EB2;" +
                "text-offset: 5px, 0px;" +
                "text-color: #222;" +
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
        System.setProperty("org.graphstream.ui.renderer", "org.graphstream.ui.j2dviewer.J2DGraphRenderer");
        updateColor();
        //visualisedGraph.display();
        JFrame myJFrame = new JFrame();
        Viewer viewer = new Viewer(visualisedGraph, Viewer.ThreadingModel.GRAPH_IN_ANOTHER_THREAD);
        viewer.enableAutoLayout();
        View view = viewer.addDefaultView(false);   // false indicates "no JFrame".
        myJFrame.setSize(800, 600);
        myJFrame.add((Component) view);
        myJFrame.setVisible(true);
    }

    public void updateGraph(MuvtoGraph graph, BinarySolution solution) {
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
