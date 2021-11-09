package net.azisaba.mixins.injection;

import net.azisaba.mixins.ConstructorCall;
import net.azisaba.mixins.DontOverride;
import net.azisaba.mixins.MixinName;
import net.azisaba.mixins.Shadow;
import net.azisaba.mixins.util.Util;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.lang.reflect.Constructor;
import java.lang.reflect.Executable;
import java.lang.reflect.Method;

public abstract class MixinBehavior {
    private final Executable executable;
    private final boolean shadow;
    private final Inject inject;
    private final boolean constructor;
    private final boolean dontOverride;
    private final String name;
    private final String signature;

    public MixinBehavior(@NotNull Executable executable) {
        this.executable = executable;
        this.shadow = executable.isAnnotationPresent(Shadow.class);
        this.inject = executable.getAnnotation(Inject.class);
        this.constructor = executable instanceof Constructor || executable.isAnnotationPresent(ConstructorCall.class);
        this.dontOverride = executable.isAnnotationPresent(DontOverride.class);
        MixinName mixinName = executable.getAnnotation(MixinName.class);
        if (mixinName == null) {
            this.name = executable.getName();
        } else {
            if (this.constructor) throw new IllegalArgumentException("@MixinName cannot be used with constructor");
            this.name = mixinName.value();
        }
        this.signature = Util.toBytecodeSignature(executable);
    }

    @NotNull
    public Executable getExecutable() {
        return executable;
    }

    @NotNull
    public String getBytecodeSignature() {
        return getBytecodeSignature(false);
    }

    @NotNull
    public String getBytecodeSignature(boolean includeCallbackInfo) {
        return Util.toBytecodeSignature(executable, includeCallbackInfo);
    }

    public boolean isShadow() {
        return shadow;
    }

    @Nullable
    public Inject getInject() {
        return inject;
    }

    public boolean isConstructor() {
        return constructor;
    }

    public boolean canOverride() {
        return !dontOverride && inject == null && !shadow;
    }

    @NotNull
    public static MixinBehavior get(@NotNull Executable executable) {
        if (executable instanceof Method) {
            return new MixinMethod((Method) executable);
        } else if (executable instanceof Constructor) {
            return new MixinConstructor((Constructor<?>) executable);
        } else {
            throw new RuntimeException("Unknown executable type: " + executable.getClass().getTypeName());
        }
    }

    @NotNull
    public String getName() {
        return name;
    }

    @NotNull
    public String getSignature() {
        return signature;
    }
}
