package net.azisaba.mixins.injection;

import org.jetbrains.annotations.NotNull;

import java.lang.reflect.Constructor;

public class MixinConstructor extends MixinBehavior {
    public MixinConstructor(@NotNull Constructor<?> constructor) {
        super(constructor);
    }

    @NotNull
    public Constructor<?> getConstructor() {
        return (Constructor<?>) getExecutable();
    }
}
