package org.graalvm.vm.trcview.libtrc;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.FIELD)
public @interface Register {
    public static final int FORMAT_HEX = 0;
    public static final int FORMAT_BIN = 1;
    public static final int FORMAT_OCT = 2;
    public static final int FORMAT_DEC = 3;

    int value() default FORMAT_HEX;
}
