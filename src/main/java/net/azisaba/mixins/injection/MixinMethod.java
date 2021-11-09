package net.azisaba.mixins.injection;

import org.jetbrains.annotations.NotNull;

import java.lang.reflect.Method;

public class MixinMethod extends MixinBehavior {
    public MixinMethod(@NotNull Method method) {
        super(method);
    }

    @NotNull
    public Method getMethod() {
        return (Method) getExecutable();
    }
}
