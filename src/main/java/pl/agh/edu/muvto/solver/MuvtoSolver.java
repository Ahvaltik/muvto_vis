package pl.agh.edu.muvto.solver;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.uma.jmetal.algorithm.Algorithm;
import org.uma.jmetal.algorithm.singleobjective.geneticalgorithm.GeneticAlgorithmBuilder;
import org.uma.jmetal.operator.CrossoverOperator;
import org.uma.jmetal.operator.MutationOperator;
import org.uma.jmetal.operator.SelectionOperator;
import org.uma.jmetal.operator.impl.crossover.SinglePointCrossover;
import org.uma.jmetal.operator.impl.mutation.BitFlipMutation;
import org.uma.jmetal.operator.impl.selection.BinaryTournamentSelection;
import org.uma.jmetal.solution.BinarySolution;
import org.uma.jmetal.util.AlgorithmRunner;

/**
 * Muvto solver.
 */
public class MuvtoSolver {

    private static final Logger logger =
            LoggerFactory.getLogger(MuvtoSolver.class);

    // TODO: refactor

    public BinarySolution solve(MuvtoProblem problem) {

        CrossoverOperator<BinarySolution> crossoverOperator =
                new SinglePointCrossover(0.9);

        MutationOperator<BinarySolution> mutationOperator =
                new BitFlipMutation(1.0 / problem.getNumberOfBits(0));

        SelectionOperator<List<BinarySolution>, BinarySolution>
            selectionOperator =
                new BinaryTournamentSelection<BinarySolution>();

        Algorithm<BinarySolution> algorithm
            = new GeneticAlgorithmBuilder<BinarySolution>(problem,
                                                          crossoverOperator,
                                                          mutationOperator)
                .setPopulationSize(100)
                .setMaxEvaluations(25000)
                .setSelectionOperator(selectionOperator)
                .build();

        AlgorithmRunner algorithmRunner =
                new AlgorithmRunner.Executor(algorithm).execute();

        BinarySolution solution = algorithm.getResult();

        long computingTime = algorithmRunner.getComputingTime();
        logger.debug("computation time: " + computingTime);

        return solution;
    }
}
