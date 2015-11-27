package pl.edu.agh.muvto.solver.builder;

import java.lang.reflect.Field;

import org.uma.jmetal.algorithm.Algorithm;
import org.uma.jmetal.problem.Problem;
import org.uma.jmetal.solution.Solution;

import fj.Try;
import fj.data.Either;

/**
 * Trait for AlgorithmBuilder-s that allows to set Solution<S> right before
 * instantiating the algorithm. Builder is thus reusable.
 * @param <S>
 */
public interface AlgorithmBuilderExt<S extends Solution<?>> {

    @SuppressWarnings("unchecked")
    default Algorithm<S> build(Problem<S> problem) {

        return (Algorithm<S>) Either.reduce(Try.f(() -> {
            Field field = this.getClass().getSuperclass()
                    .getDeclaredField("problem");
            field.setAccessible(true);
            field.set(this, problem);
            field.setAccessible(false);
            return this.getClass().getMethod("build").invoke(this);
        })._1().toEither().bimap(error -> {
            throw new RuntimeException("Not an AlgorithmBuilder", error);
        }, a -> a));

    }
}
