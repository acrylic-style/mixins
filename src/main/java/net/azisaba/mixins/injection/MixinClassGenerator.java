package net.azisaba.mixins.injection;

import javassist.CannotCompileException;
import javassist.ClassPool;
import javassist.CtClass;
import javassist.CtMethod;
import javassist.Modifier;
import javassist.NotFoundException;
import javassist.expr.MethodCall;
import net.azisaba.mixins.Mixins;
import net.blueberrymc.native_util.NativeUtil;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

public class MixinClassGenerator {
    private static final Map<String, Class<?>> accessors = new HashMap<>();
    public static final Map<String, Object> abstractAccessors = new HashMap<>();
    private static final AtomicInteger generatedMethodAccessorId = new AtomicInteger();
    private static final AtomicInteger generatedAbstractAccessorId = new AtomicInteger();
    private static CtClass ccCallbackInfo;
    private static CtClass ccMethodCall;

    private static void defineTypes() {
        try {
            if (ccCallbackInfo == null) {
                ccCallbackInfo = Mixins.getClassPool().get(CallbackInfo.class.getTypeName());
            }
            if (ccMethodCall == null) {
                ccMethodCall = Mixins.getClassPool().get(MethodCall.class.getTypeName());
            }
        } catch (NotFoundException e) {
            throw new AssertionError(e);
        }
    }

    /**
     * Generates a class.
     *
     * @return generated class
     */
    @NotNull
    public static Class<?> generateAccessorFor(@NotNull Method mixinMethod) throws CannotCompileException, IOException, NotFoundException {
        String mapKey = mixinMethod.getDeclaringClass().getTypeName() + ";" + mixinMethod.toGenericString();
        if (accessors.containsKey(mapKey)) {
            return accessors.get(mapKey);
        }
        defineTypes();
        String fqn = Mixins.class.getPackage().getName() + ".injection.__generated__.MixinGeneratedMethodAccessor" + generatedMethodAccessorId.getAndIncrement();
        ClassPool cp = Mixins.getClassPool();
        CtClass cc = cp.makeClass(fqn);
        CtMethod executeMethod = new CtMethod(
                cp.get(mixinMethod.getReturnType().getTypeName()),
                "execute",
                Arrays.stream(mixinMethod.getParameterTypes()).map(c -> {
                    try {
                        return cp.get(c.getTypeName());
                    } catch (NotFoundException e) {
                        throw new AssertionError(e);
                    }
                }).toArray(CtClass[]::new),
                cc
        );
        executeMethod.setModifiers(Modifier.PUBLIC | Modifier.STATIC);
        Class<?> abstractAccessor = generateAbstractAccessor(mixinMethod.getDeclaringClass()).getClass();
        executeMethod.setBody("return ((" + abstractAccessor.getCanonicalName() + ") " + Mixins.class.getPackage().getName() + ".injection.MixinClassGenerator.abstractAccessors.get(\"" + mixinMethod.getDeclaringClass().getTypeName() + "\"))." + mixinMethod.getName() + "($$);");
        cc.addMethod(executeMethod);
        byte[] bytes = cc.toBytecode();
        Class<?> clazz = NativeUtil.defineClass(fqn.replace(".", "/"), Mixins.class.getClassLoader(), bytes, bytes.length);
        accessors.put(mapKey, clazz);
        return clazz;
    }

    @SuppressWarnings("unchecked")
    @NotNull
    public static <T> T generateAbstractAccessor(@NotNull Class<T> extendClass) throws NotFoundException, IOException, CannotCompileException {
        if (abstractAccessors.containsKey(extendClass.getTypeName())) {
            return (T) abstractAccessors.get(extendClass.getTypeName());
        }
        /*
        if ((extendClass.getModifiers() & java.lang.reflect.Modifier.ABSTRACT) == 0) {
            T t = NativeUtil.allocateInstance(extendClass);
            abstractAccessors.put(extendClass.getTypeName(), t);
            return t;
        }
        */
        String fqn = Mixins.class.getPackage().getName() + ".__generated__.GeneratedAbstractAccessor" + generatedAbstractAccessorId.getAndIncrement();
        CtClass cc = Mixins.getClassPool().makeClass(fqn, Mixins.getClassPool().get(extendClass.getTypeName()));
        byte[] bytes = cc.toBytecode();
        Class<?> clazz = NativeUtil.defineClass(fqn.replace(".", "/"), Mixins.class.getClassLoader(), bytes, bytes.length);
        T t = (T) NativeUtil.allocateInstance(clazz);
        abstractAccessors.put(extendClass.getTypeName(), t);
        return t;
    }
}
