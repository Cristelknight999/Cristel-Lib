package de.cristelknight.cristellib.neoforge.mixin;

import net.minecraft.server.packs.PathPackResources;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

import java.nio.file.Path;

@Mixin(PathPackResources.class)
public interface PathPackResourcesAccessor {
    @Accessor("root")
    Path cristellib$getRoot();
}