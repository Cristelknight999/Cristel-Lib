package de.cristelknight.cristellib.fabric.mixin;

import de.cristelknight.cristellib.builtinpacks.BuiltInDataPackLoader;
import net.fabricmc.fabric.impl.resource.ResourceLoaderImpl;
import net.minecraft.server.packs.PackType;
import net.minecraft.server.packs.repository.Pack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.function.Consumer;

@Mixin(ResourceLoaderImpl.class)
public class ResourceLoaderImplMixin {

    @Inject(method = "registerBuiltinResourcePacks", at = @At("TAIL"))
    private static void cristellib$registerBuiltinResourcePacks(PackType resourceType, Consumer<Pack> consumer, CallbackInfo ci) {
        if(resourceType.equals(PackType.SERVER_DATA)){
            BuiltInDataPackLoader.getPacks(consumer);
        }
    }

}