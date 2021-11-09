package net.azisaba.mixins.util;

import net.azisaba.mixins.injection.CallbackInfo;
import net.blueberrymc.native_util.NativeUtil;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.lang.reflect.Executable;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class Util {
    /**
     * Checks if the provided class is loaded. <b>Warning: This method is very expensive and should NOT be used
     * frequently.</b>
     * @param clazz class to check
     * @return class if loaded, null otherwise
     */
    @Nullable
    public static Class<?> getClass(@NotNull String clazz) {
        try {
            return Class.forName(clazz);
        } catch (ClassNotFoundException ignore) {}
        return Arrays.stream(NativeUtil.getLoadedClasses())
                .filter(it -> it.getTypeName().equals(clazz))
                .findFirst()
                .orElse(null);
    }

    @NotNull
    public static String toBytecodeSignature(@NotNull Executable executable) {
        return toBytecodeSignature(executable, false);
    }

    @NotNull
    public static String toBytecodeSignature(@NotNull Executable executable, boolean includeCallbackInfo) {
        StringBuilder sig = new StringBuilder("(");
        for (Class<?> clazz : executable.getParameterTypes()) {
            if (includeCallbackInfo || !clazz.equals(CallbackInfo.class)) {
                sig.append(toBytecodeTypeName(clazz));
            }
        }
        sig.append(")");
        if (executable instanceof Method) {
            sig.append(toBytecodeTypeName(((Method) executable).getReturnType()));
        } else {
            sig.append("V");
        }
        return sig.toString();
    }

    @NotNull
    public static String toBytecodeTypeName(@NotNull Class<?> clazz) {
        return toBytecodeTypeName("", clazz);
    }

    @NotNull
    public static String toBytecodeTypeName(@NotNull String prefix, @NotNull Class<?> clazz) {
        if (clazz.isArray()) {
            // resolve array type recursively
            return toBytecodeTypeName(prefix + "[", clazz.getComponentType());
        }
        String name = clazz.getTypeName();
        switch (name) {
            case "boolean": return prefix + "Z";
            case "byte": return prefix + "B";
            case "char": return prefix + "C";
            case "double": return prefix + "D";
            case "float": return prefix + "F";
            case "int": return prefix + "I";
            case "long": return prefix + "J";
            case "short": return prefix + "S";
            case "void": return prefix + "V";
            default: return prefix + "L" + name.replace(".", "/") + ";";
        }
    }

    public static Object[] concat(Object[] arr, Object... objects) {
        List<Object> list = new ArrayList<>(Arrays.asList(arr));
        list.addAll(Arrays.asList(objects));
        return list.toArray();
    }

    @Contract(pure = true)
    @NotNull
    public static <T> List<T> concat(@NotNull List<T> list, @NotNull List<T> another) {
        List<T> newList = new ArrayList<>(list);
        newList.addAll(another);
        return newList;
    }

    @SuppressWarnings("unchecked")
    @Contract(value = "_ -> param1", pure = true)
    public static <T extends U, U> List<U> upcast(@NotNull List<T> list) {
        return (List<U>) list;
    }
}
