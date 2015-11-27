package pl.agh.edu.muvto.solver;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.uma.jmetal.algorithm.Algorithm;
import org.uma.jmetal.problem.BinaryProblem;
import org.uma.jmetal.solution.BinarySolution;
import org.uma.jmetal.util.AlgorithmRunner;

import pl.agh.edu.muvto.solver.builder.AlgorithmBuilderExt;

/**
 * Muvto solver.
 */
public class MuvtoSolver {

    private static final Logger logger =
            LoggerFactory.getLogger(MuvtoSolver.class);
    
    private AlgorithmBuilderExt<BinarySolution> algorithmBuilder;

    public MuvtoSolver(AlgorithmBuilderExt<BinarySolution> algorithmBuilder) {
        this.algorithmBuilder = algorithmBuilder;
    }

    public BinarySolution solve(BinaryProblem problem) {

        Algorithm<BinarySolution> algorithm = algorithmBuilder.build(problem);

        AlgorithmRunner algorithmRunner =
                new AlgorithmRunner.Executor(algorithm).execute();

        BinarySolution solution = algorithm.getResult();

        long computingTime = algorithmRunner.getComputingTime();
        logger.debug("computation time: " + computingTime);

        return solution;
    }
}
