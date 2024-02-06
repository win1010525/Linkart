package com.github.vini2003.linkart.utility;

import me.melontini.dark_matter.api.base.util.Exceptions;

import java.lang.invoke.*;

public class Lambdas {

    public static <T> T handle(MethodHandles.Lookup lookup, Class<T> type, MethodHandle h) {
        CallSite site = Exceptions.supply(() -> LambdaMetafactory.metafactory(lookup, "invoke",
                MethodType.methodType(type), h.type(), h, h.type()));
        return Exceptions.supply(() -> (T) site.getTarget().invoke());
    }
}
