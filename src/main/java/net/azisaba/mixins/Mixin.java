package net.azisaba.mixins;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

// Either value or target must be specified (you can't specify both)
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
public @interface Mixin {
    Class<?> value() default Void.class;
    String target() default "";
    int priority() default 1000;
}
