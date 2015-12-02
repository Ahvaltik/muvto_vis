package pl.edu.agh.muvto.visualisation;

import com.jgraph.layout.JGraphFacade;
import com.jgraph.layout.graph.JGraphSimpleLayout;
import org.jgraph.JGraph;
import org.jgraph.graph.*;
import org.jgrapht.ext.JGraphModelAdapter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import pl.edu.agh.muvto.model.MuvtoGraph;
import pl.edu.agh.muvto.solver.MuvtoSolution;

import javax.swing.*;
import java.awt.*;
import java.awt.geom.Rectangle2D;
import java.util.HashMap;
import java.util.Map;

public class Visualiser extends JFrame {
    private static final Logger logger = LoggerFactory.getLogger(Visualiser.class);

    private final MuvtoGraph mGraph;
    private JGraphModelAdapter mJgAdapter;
    private static final Color DEFAULT_BG_COLOR = Color.decode( "#FAFBFF" );
    private static final Dimension DEFAULT_SIZE = new Dimension( 530, 320 );

    public Visualiser(MuvtoGraph graph) {
        super();
        mGraph = graph;
    }

    public void start() {
        init();
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        pack();
        setVisible(true);
    }

    public void init() {
        mJgAdapter = new JGraphModelAdapter(mGraph);

        JGraph jgraph = new JGraph(mJgAdapter);
        adjustDisplaySettings( jgraph );
        getContentPane(  ).add( jgraph );
        resize( DEFAULT_SIZE );

        mGraph.vertexSet().stream().forEach(this::resizeVertex);
        mGraph.edgeSet().stream().forEach(this::resizeEdge);

        JGraphSimpleLayout hir = new JGraphSimpleLayout(JGraphSimpleLayout.TYPE_CIRCLE);

        final JGraphFacade graphFacade = new JGraphFacade(jgraph);
        hir.run(graphFacade);
        final Map nestedMap = graphFacade.createNestedMap(true, true);
        jgraph.getGraphLayoutCache().edit(nestedMap);
    }

    public void updateGraph(MuvtoSolution solution) {

    }

    private void adjustDisplaySettings(JGraph jg) {
        jg.setPreferredSize(DEFAULT_SIZE);
        jg.setBackground(DEFAULT_BG_COLOR);
        jg.setFont(jg.getFont().deriveFont(Font.BOLD));
    }

    private void resizeVertex(Object vertex) {
        DefaultGraphCell cell = mJgAdapter.getVertexCell(vertex);
        Map attr = cell.getAttributes();
        Rectangle2D bounds = GraphConstants.getBounds(attr);
        //GraphConstants.setBounds(attr, new Rectangle2D.Double(bounds.getX(), bounds.getY(), 30, 30));
        GraphConstants.setBackground(attr, Color.green);
        Map cellAttr = new HashMap();
        cellAttr.put(cell, attr);
        mJgAdapter.edit(cellAttr, null, null, null);
    }

    private void resizeEdge(Object edge) {
        DefaultGraphCell cell = mJgAdapter.getEdgeCell(edge);
        Map attr = cell.getAttributes();
        //GraphConstants.setLineWidth(attr, 0.5f);
        Map cellAttr = new HashMap();
        cellAttr.put(cell, attr);
        //mJgAdapter.edit(cellAttr, null, null, null); //edge factory fails, possibly due to lack of empty constructor for MuvtoEdge
    }

}
