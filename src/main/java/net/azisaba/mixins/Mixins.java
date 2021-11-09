package net.azisaba.mixins;

import javassist.ClassPool;
import net.azisaba.mixins.configuration.MixinConfiguration;
import net.azisaba.mixins.injection.ClassTransformQueueEntry;
import net.azisaba.mixins.util.Util;
import net.blueberrymc.native_util.ClassDefinition;
import net.blueberrymc.native_util.NativeUtil;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jetbrains.annotations.NotNull;

import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class Mixins {
    private static boolean setup = false;
    private static final Logger LOGGER = LogManager.getLogger("mixin");
    private static final List<MixinConfiguration> CONFIGURATIONS = Collections.synchronizedList(new ArrayList<>());
    private static final ClassPool CLASS_POOL = ClassPool.getDefault();

    @NotNull
    public static Logger getLogger() {
        return LOGGER;
    }

    @NotNull
    public static List<MixinConfiguration> getConfigurations() {
        return CONFIGURATIONS;
    }

    @NotNull
    public static ClassPool getClassPool() {
        return CLASS_POOL;
    }

    public static void setup() {
        if (setup) throw new IllegalStateException("mixin is already initialized!");
        LOGGER.info("Loading mixin");
        String cp;
        try {
            cp = Mixins.class.getProtectionDomain().getCodeSource().getLocation().toURI().getPath();
        } catch (URISyntaxException e) {
            throw new RuntimeException(e);
        }
        LOGGER.info("Adding " + cp + " to classpath");
        try {
            NativeUtil.canRedefineClasses();
            CLASS_POOL.appendClassPath(cp);
        } catch (Exception e) {
            LOGGER.warn("Failed to add " + cp + " to classpath", e);
        }
        MixinClassTransformer.setup();
        setup = true;
        LOGGER.info("mixin setup complete");
    }

    public static void addConfiguration(@NotNull MixinConfiguration configuration) {
        try {
            CONFIGURATIONS.add(configuration);
            for (String mixinClassName : configuration.getClassList()) {
                Class<?> clazz = Util.getClass(mixinClassName);
                if (clazz == null) {
                    LOGGER.warn("Could not find class " + mixinClassName);
                } else {
                    ClassTransformQueueEntry entry = ClassTransformQueueEntry.from(clazz);
                    Class<?> target = Util.getClass(entry.getMixinClassInfo().getTarget());
                    if (target != null) {
                        LOGGER.debug("Attempting to redefine the class because " + target.getTypeName() + " is already loaded.");
                        try {
                            NativeUtil.redefineClasses(
                                    new ClassDefinition[]{
                                            new ClassDefinition(
                                                    target,
                                                    MixinClassTransformer.transform(ClassTransformQueueEntry.from(clazz))
                                            )
                                    }
                            );
                        } catch (Exception e) {
                            LOGGER.warn("Failed to redefine class " + target.getTypeName(), e);
                        }
                    } else {
                        MixinClassTransformer.queue.put(entry.getMixinClassInfo().getTarget().replace(".", "/"), entry);
                    }
                }
            }
        } catch (Exception | LinkageError e) {
            LOGGER.error("Failed to add configuration", e);
        }
    }
}
