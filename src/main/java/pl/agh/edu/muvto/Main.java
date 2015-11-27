package pl.agh.edu.muvto;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import pl.agh.edu.muvto.model.MuvtoEdge;
import pl.agh.edu.muvto.model.MuvtoGraph;
import pl.agh.edu.muvto.model.MuvtoVertex;

/**
 * Main class.
 */
public class Main
{
    private static final Logger logger = LoggerFactory.getLogger(Main.class);

    /**
     * Application entry point.
     * @param args commandline arguments
     */
    public static void main(String[] args)
    {
        logger.info("Hello World!");

        MuvtoGraph graph = new MuvtoGraph();

        MuvtoVertex v1 = new MuvtoVertex(0);
        MuvtoVertex v2 = new MuvtoVertex(1);

        MuvtoEdge e1 = new MuvtoEdge(0, 10, 0, 0.5);

        graph.addVertex(v1);
        graph.addVertex(v2);
        graph.addEdge(v1, v2, e1);

        logger.info(graph.toString());
    }
}
