package net.azisaba.mixins;

import javassist.CannotCompileException;
import javassist.CtBehavior;
import javassist.CtClass;
import javassist.CtConstructor;
import javassist.CtMethod;
import javassist.Modifier;
import javassist.NotFoundException;
import net.azisaba.mixins.injection.CallbackInfo;
import net.azisaba.mixins.injection.ClassTransformQueueEntry;
import net.azisaba.mixins.injection.Inject;
import net.azisaba.mixins.injection.MixinBehavior;
import net.azisaba.mixins.injection.MixinClassGenerator;
import net.azisaba.mixins.injection.MixinClassInfo;
import net.azisaba.mixins.injection.MixinConstructor;
import net.azisaba.mixins.injection.MixinInvokeHandler;
import net.azisaba.mixins.injection.MixinMethod;
import net.blueberrymc.native_util.NativeUtil;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class MixinClassTransformer {
    static final Map<String, ClassTransformQueueEntry> queue = new HashMap<>();
    private static final List<String> transformed = new ArrayList<>();
    private static final Map<String, MixinInvokeHandler> classes = new HashMap<>();

    public static void setup() {
        NativeUtil.registerClassLoadHook((classLoader, cname, clazz, protectionDomain, bytes) -> {
            if (!queue.containsKey(cname)) return null;
            ClassTransformQueueEntry entry = queue.remove(cname);
            if (entry == null) return null;
            try {
                Mixins.getLogger().debug("Transforming " + entry.getMixinClassInfo().getTarget());
                return transform(entry);
            } catch (Exception | StackOverflowError | LinkageError | AssertionError e) {
                Mixins.getLogger().warn("Failed to transform " + entry.getMixinClassInfo().getTarget(), e);
                return null;
            }
        });
    }

    @NotNull
    public static MixinInvokeHandler getMixinInvokeHandler(@NotNull String key) {
        if (!classes.containsKey(key)) {
            classes.put(key, new MixinInvokeHandler());
        }
        return classes.get(key);
    }

    public static byte[] transform(@NotNull ClassTransformQueueEntry entry) throws Exception {
        String target = entry.getMixinClassInfo().getTarget();
        CtClass cc = Mixins.getClassPool().get(target);
        if (!transformed.contains(target)) {
            transformed.add(target);
            for (CtBehavior behavior : cc.getDeclaredBehaviors()) transformTargetClass1(behavior);
        }
        processMixinClass(cc, entry.getMixinClassInfo());
        Mixins.getLogger().debug("Transformed " + cc.getName());
        return cc.toBytecode();
    }

    private static void transformTargetClass1(@NotNull CtBehavior behavior) throws CannotCompileException {
        if ((behavior.getModifiers() & Modifier.ABSTRACT) != 0 || (behavior.getModifiers() & Modifier.NATIVE) != 0) return;
        String ciCancel = "";
        if (behavior instanceof CtMethod) {
            ciCancel = "if (ci.isCancelled()) return ($r) ci.getReturnValue();\n";
        }
        String headSrc = "{" +
                "try { System.err.println(Class.forName(\"" + MixinClassTransformer.class.getTypeName() + "\")); } catch (Exception e) { e.printStackTrace(); }" +
                CallbackInfo.class.getCanonicalName() + " ci = " + MixinClassTransformer.class.getCanonicalName() + ".getMixinInvokeHandler(\"" + behavior.getLongName() + "\").executeHead($args);\n" +
                ciCancel +
                "}";
        behavior.insertBefore(headSrc);
        /*
        method.instrument(new ExprEditor() {
            @Override
            public void edit(MethodCall m) throws CannotCompileException {
                String src = "{" +
                        "  $_ = $proceed($$);" +
                        "}";
                m.replace(src);
            }
        });
        */
        String tailSrc = "{" +
                CallbackInfo.class.getCanonicalName() + " ci = " + MixinClassTransformer.class.getCanonicalName() + ".getMixinInvokeHandler(\"" + behavior.getLongName() + "\").executeTail($args);\n" +
                ciCancel +
                "}";
        behavior.insertAfter(tailSrc);
        Mixins.getLogger().debug("Processed " + behavior.getLongName());
    }

    private static void processMixinClass(@NotNull CtClass targetClass, @NotNull MixinClassInfo info) throws Exception {
        for (MixinMethod method : info.getMethods()) {
            if (method.isShadow()) continue;
            processInject(targetClass, method);
        }
    }

    private static boolean shouldOverride(@NotNull CtClass targetClass, @NotNull MixinBehavior behavior) {
        if (!behavior.canOverride()) return false;
        try {
            if (behavior.isConstructor()) {
                targetClass.getConstructor(behavior.getBytecodeSignature(true));
            } else {
                targetClass.getMethod(behavior.getName(), behavior.getBytecodeSignature());
            }
            return true;
        } catch (NotFoundException e) {
            return false;
        }
    }

    private static void processInject(@NotNull CtClass targetClass, @NotNull MixinBehavior behavior) throws Exception {
        Inject inject = behavior.getInject();
        if (inject == null) return;
        if (behavior instanceof MixinConstructor) {
            Mixins.getLogger().warn(
                    "Constructor does not support @Inject ({} at {})",
                    behavior.getExecutable().toGenericString(),
                    behavior.getExecutable().getDeclaringClass().getTypeName()
            );
            return;
        }
        MixinInvokeHandler handler;
        String sig = behavior.getBytecodeSignature();
        try {
            if (behavior.isConstructor()) {
                CtConstructor cc = targetClass.getConstructor(sig);
                handler = getMixinInvokeHandler(cc.getLongName());
            } else {
                CtMethod cm = targetClass.getMethod(behavior.getName(), sig);
                handler = getMixinInvokeHandler(cm.getLongName());
            }
        } catch (NotFoundException ex) {
            String type;
            if (behavior.isConstructor()) {
                type = "constructor";
            } else {
                type = "method";
            }
            Mixins.getLogger().warn("Could not find a " + type + " to inject\nTried to find " + targetClass.getName() + "#" + behavior.getName() + sig);
            return;
        }
        MixinMethod method = (MixinMethod) behavior;
        At[] ats = inject.at();
        if (ats.length == 0) {
            Mixins.getLogger().warn(
                    "@Inject(at = ...) is empty ({} at {})",
                    method.getMethod().toGenericString(),
                    method.getMethod().getDeclaringClass().getTypeName()
            );
        } else {
            Class<?> accessor = MixinClassGenerator.generateAccessorFor(method.getMethod());
            for (At at : ats) {
                if (at == At.HEAD) {
                    handler.addHeadAccessor(accessor);
                } else if (at == At.TAIL) {
                    handler.addTailAccessor(accessor);
                }
            }
        }
    }
}
