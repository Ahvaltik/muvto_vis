package pl.agh.edu.muvto.solver.builder;

import org.uma.jmetal.algorithm.singleobjective.geneticalgorithm.GeneticAlgorithmBuilder;
import org.uma.jmetal.operator.CrossoverOperator;
import org.uma.jmetal.operator.MutationOperator;
import org.uma.jmetal.solution.Solution;

public class GeneticAlgorithmBuilderExt<S extends Solution<?>>
    extends GeneticAlgorithmBuilder<S>
    implements AlgorithmBuilderExt<S> {

    public GeneticAlgorithmBuilderExt(CrossoverOperator<S> crossoverOperator,
                                      MutationOperator<S> mutationOperator) {
        super(null, crossoverOperator, mutationOperator);
    }
}
