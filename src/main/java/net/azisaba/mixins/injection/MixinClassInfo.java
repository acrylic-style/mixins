package net.azisaba.mixins.injection;

import net.azisaba.mixins.Mixin;
import net.azisaba.mixins.util.Util;
import org.jetbrains.annotations.NotNull;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public class MixinClassInfo {
    private final Class<?> clazz;
    private final String target;
    private final List<MixinMethod> methods;
    private final List<MixinConstructor> constructors;

    public MixinClassInfo(@NotNull Class<?> clazz, @NotNull List<MixinMethod> methods, @NotNull List<MixinConstructor> constructors) {
        String target1 = null;
        this.clazz = clazz;
        Mixin mixin = clazz.getAnnotation(Mixin.class);
        if (mixin == null) throw new RuntimeException("Not annotated with @Mixin");
        if (mixin.target().length() != 0) target1 = mixin.target();
        if (mixin.value() != Void.class) target1 = mixin.value().getTypeName();
        if (target1 == null) throw new RuntimeException("No target for @Mixin at " + clazz.getTypeName());
        this.target = target1;
        this.methods = methods;
        this.constructors = constructors;
    }

    @NotNull
    public Class<?> getClazz() {
        return clazz;
    }

    @NotNull
    public String getTarget() {
        return target;
    }

    @NotNull
    public List<MixinMethod> getMethods() {
        return methods;
    }

    @NotNull
    public List<MixinConstructor> getConstructors() {
        return constructors;
    }

    @NotNull
    public List<MixinBehavior> getBehaviors() {
        return Util.concat(Util.upcast(methods), Util.upcast(constructors));
    }

    @NotNull
    public static MixinClassInfo from(@NotNull Class<?> clazz) {
        List<MixinMethod> methods = Arrays.stream(clazz.getDeclaredMethods())
                .map(MixinMethod::new)
                .collect(Collectors.toList());
        List<MixinConstructor> constructors = Arrays.stream(clazz.getDeclaredConstructors())
                .map(MixinConstructor::new)
                .collect(Collectors.toList());
        return new MixinClassInfo(clazz, methods, constructors);
    }
}
