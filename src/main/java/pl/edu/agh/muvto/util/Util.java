package pl.edu.agh.muvto.util;

import java.util.function.Consumer;

import fj.F;
import fj.Unit;

/**
 * Utility functions.
 */
public class Util {

    /**
     * Transforms void function into function returning Unit.
     * Some functions are designed for side effect only.
     * @param f
     * @return
     */
    public static <A> F<A, Unit> liftVoid(final Consumer<A> f) {
        return a -> {
            f.accept(a);
            return Unit.unit();
        };
    }
}
