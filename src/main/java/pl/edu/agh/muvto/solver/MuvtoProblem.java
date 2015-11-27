package pl.edu.agh.muvto.solver;

import org.uma.jmetal.problem.impl.AbstractBinaryProblem;
import org.uma.jmetal.solution.BinarySolution;

import pl.edu.agh.muvto.model.MuvtoGraph;

/**
 * Muvto problem.
 */
public class MuvtoProblem extends AbstractBinaryProblem {

    private static final long serialVersionUID = 1L;

    private MuvtoGraph graph;

    public MuvtoProblem(MuvtoGraph graph) {

        this.graph = graph;

        setNumberOfVariables(graph.vertexSet().size());
        setNumberOfObjectives(1);
        setName("MuvtoProblem");
    }

    @Override
    public void evaluate(BinarySolution solution) {
        // TODO: evaluate solution and set objective
        solution.setObjective(0, 1.0);
    }

    @Override
    protected int getBitsPerVariable(int variableIndex) {
        return graph.vertexSet().stream()
            .filter(v -> v.getId() == variableIndex)
            .findFirst()
            .map(v -> graph.outDegreeOf(v) * graph.inDegreeOf(v))
            .orElseThrow(() ->
                new RuntimeException("Invalid variable number")
            );
    }

}
