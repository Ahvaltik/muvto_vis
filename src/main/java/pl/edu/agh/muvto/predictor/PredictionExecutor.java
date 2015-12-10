package pl.edu.agh.muvto.predictor;

import org.uma.jmetal.solution.BinarySolution;
import pl.edu.agh.muvto.solver.MuvtoProblem;

/**
 * @author Grzegorz Mirek
 */
public interface PredictionExecutor {

    void submit(MuvtoProblem problem);
    BinarySolution getSolution(MuvtoProblem problem);
}
