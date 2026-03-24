package de.cristelknight.cristellib.neoforge.mixin;

import net.neoforged.fml.jarcontents.JarContents;
import net.neoforged.neoforge.resource.JarContentsPackResources;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(JarContentsPackResources.class)
public interface JarContentsPackResourcesAccessor {
    @Accessor("prefix")
    String cristellib$getPrefix();

    @Accessor("contents")
    JarContents cristellib$getJarContents();
}