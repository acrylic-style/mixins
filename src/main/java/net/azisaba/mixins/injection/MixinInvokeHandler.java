package net.azisaba.mixins.injection;

import net.azisaba.mixins.util.Util;
import net.blueberrymc.native_util.NativeUtil;
import org.jetbrains.annotations.NotNull;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;

public class MixinInvokeHandler {
    protected final List<Method> headAccessors = new ArrayList<>();
    protected final List<Method> tailAccessors = new ArrayList<>();

    @NotNull
    private static CallbackInfo execute(@NotNull List<Method> methods, Object[] args) {
        CallbackInfo ci = new CallbackInfo();
        for (Method executor : methods) {
            Class<?>[] params = executor.getParameterTypes();
            if (params.length >= 1 && params[params.length - 1].equals(CallbackInfo.class)) {
                NativeUtil.invoke(executor, null, Util.concat(args, ci));
            } else {
                NativeUtil.invoke(executor, null, args);
            }
            if (ci.isCancelled()) return ci;
        }
        return ci;
    }

    @NotNull
    public CallbackInfo executeHead(Object... args) {
        return execute(headAccessors, args);
    }

    @NotNull
    public CallbackInfo executeTail(Object... args) {
        return execute(tailAccessors, args);
    }

    public void addHeadAccessor(@NotNull Class<?> accessor) {
        Method m = accessor.getDeclaredMethods()[0];
        headAccessors.add(m);
    }

    public void addTailAccessor(@NotNull Class<?> accessor) {
        Method m = accessor.getDeclaredMethods()[0];
        tailAccessors.add(m);
    }
}
