package pl.edu.agh.muvto.visualisation;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.uma.jmetal.solution.BinarySolution;
import pl.edu.agh.muvto.model.MuvtoGraph;

import java.util.ArrayList;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

public class VisualisationController {
    private static final Logger logger = LoggerFactory.getLogger(VisualisationController.class);

    private final Timer mTimer;
    private final List<GraphAndSolution> mGraphList;
    private GRAPH_STATE mState;
    private Visualiser mVisualiser;

    private enum GRAPH_STATE {
        INITIALIZED, UNINITIALIZED;
    }

    private class GraphAndSolution {
        public MuvtoGraph getGraph() {
            return mGraph;
        }

        public BinarySolution getSolution() {
            return mSolution;
        }

        private final MuvtoGraph mGraph;
        private final BinarySolution mSolution;

        public GraphAndSolution(MuvtoGraph graph, BinarySolution solution) {
            mSolution = solution;
            mGraph = graph;
        }
    }

    public VisualisationController() {
        mState = GRAPH_STATE.UNINITIALIZED;
        mGraphList = new ArrayList<>();
        mTimer = new Timer();
    }

    public void initializeGraphDisplay(MuvtoGraph graph) {
        if (mState.equals(GRAPH_STATE.INITIALIZED)) {
            logger.error("Graph already initialized");
            return;
        }
        mVisualiser = new Visualiser(graph);
        mVisualiser.start();
        mState = GRAPH_STATE.INITIALIZED;
        mTimer.scheduleAtFixedRate(new TimerTask() {
            @Override
            public void run() {
                updateDisplay();
            }
        }, 0, 2000);
    }

    public synchronized void updateGraph(MuvtoGraph graph, BinarySolution solution) {
        mGraphList.add(new GraphAndSolution(graph, solution));
    }

    public synchronized void stopVisualisation() {
        mTimer.cancel();
        mGraphList.clear();
        mState = GRAPH_STATE.UNINITIALIZED;
    }

    private synchronized void updateDisplay() {
        if (mState.equals(GRAPH_STATE.UNINITIALIZED)) {
            logger.error("Can't update display, graph was not initialized");
            return;
        }
        if (!mGraphList.isEmpty()) {
            GraphAndSolution graphAndSolution = mGraphList.remove(0);
            mVisualiser.updateGraph(graphAndSolution.getGraph(), graphAndSolution.getSolution());
        }
    }


}
