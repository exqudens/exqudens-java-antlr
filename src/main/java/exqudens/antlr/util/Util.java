package exqudens.antlr.util;

import java.util.function.BinaryOperator;

public class Util {

    public static <T> BinaryOperator<T> throwingMerger() {
        return (u,v) -> { throw new IllegalStateException(String.format("Duplicate key %s", u)); };
    }

}
