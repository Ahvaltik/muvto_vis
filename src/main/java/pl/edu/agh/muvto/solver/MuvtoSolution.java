package pl.edu.agh.muvto.solver;

import org.uma.jmetal.problem.BinaryProblem;
import org.uma.jmetal.solution.impl.DefaultBinarySolution;

/**
 * Muvto solution. Currently does nothing.
 */
public class MuvtoSolution extends DefaultBinarySolution {

    private static final long serialVersionUID = 1L;

    public MuvtoSolution(BinaryProblem problem) {
        super(problem);
    }
}
