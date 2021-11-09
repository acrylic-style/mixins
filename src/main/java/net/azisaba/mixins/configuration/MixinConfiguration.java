package net.azisaba.mixins.configuration;

import javassist.CtClass;
import javassist.NotFoundException;
import net.azisaba.mixins.Mixins;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

public class MixinConfiguration {
    private final String mixinPackage;
    private final List<String> classes;

    public MixinConfiguration(@NotNull String mixinPackage, @NotNull List<String> classes) {
        this.mixinPackage = mixinPackage;
        this.classes = classes;
    }

    @NotNull
    public List<String> getClassList() {
        return classes.stream().map(it -> mixinPackage + "." + it).collect(Collectors.toList());
    }

    @NotNull
    public List<CtClass> loadClasses() {
        return getClassList().stream().map(it -> {
            Mixins.getLogger().debug("Loading class {}", it);
            try {
                return Mixins.getClassPool().get(it);
            } catch (NotFoundException e) {
                Mixins.getLogger().warn("Could not find " + it, e);
                return null;
            }
        }).filter(Objects::nonNull).collect(Collectors.toList());
    }
}
