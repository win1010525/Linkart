package com.github.vini2003.linkart.utility;

import java.lang.invoke.*;

public class Lambdas {

    //TODO maybe use ASM?
    public static <T> T handle(MethodHandles.Lookup lookup, Class<T> type, MethodHandle h) {
        CallSite site = supply(() -> LambdaMetafactory.metafactory(lookup, "invoke",
                MethodType.methodType(type), h.type(), h, h.type()));
        return supply(() -> (T) site.getTarget().invoke());
    }

    public static <R, E extends Throwable> R supply(ThrowingSupplier<R, E> supplier) {
        try {
            return supplier.get();
        } catch (Throwable e) {
            return sneak(e);
        }
    }

    public static <E extends Throwable, R> R sneak(Throwable exception) throws E {
        throw (E) exception;
    }

    @FunctionalInterface
    public interface ThrowingSupplier<T, E extends Throwable> {
        T get() throws E;
    }
}
