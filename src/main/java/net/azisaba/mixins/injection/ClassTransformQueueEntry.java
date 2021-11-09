package net.azisaba.mixins.injection;

import org.jetbrains.annotations.NotNull;

public class ClassTransformQueueEntry {
    private final String cname;
    private final MixinClassInfo mixinClassInfo;

    public ClassTransformQueueEntry(@NotNull String cname, MixinClassInfo mixinClassInfo) {
        this.cname = cname;
        this.mixinClassInfo = mixinClassInfo;
    }

    @NotNull
    public String getClassName() {
        return cname;
    }

    @NotNull
    public MixinClassInfo getMixinClassInfo() {
        return mixinClassInfo;
    }

    @NotNull
    public static ClassTransformQueueEntry from(@NotNull Class<?> clazz) {
        return new ClassTransformQueueEntry(clazz.getTypeName(), MixinClassInfo.from(clazz));
    }
}
