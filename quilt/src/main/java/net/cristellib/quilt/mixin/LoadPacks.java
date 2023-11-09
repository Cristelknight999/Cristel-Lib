package net.cristellib.quilt.mixin;

import net.cristellib.builtinpacks.BuiltInDataPacks;
import net.fabricmc.fabric.impl.resource.loader.ModResourcePackCreator;
import net.minecraft.server.packs.PackType;
import net.minecraft.server.packs.repository.Pack;
import org.quiltmc.qsl.resource.loader.impl.ResourceLoaderImpl;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.function.Consumer;

@Mixin(ModResourcePackCreator.class)
public class LoadPacks {
    @Shadow
    @Final
    private PackType type;

    @Inject(method = "loadPacks", at = @At("RETURN"))
    private void loadPacks(Consumer<Pack> consumer, CallbackInfo ci) {
        if(type.equals(PackType.SERVER_DATA)){
            BuiltInDataPacks.getPacks(consumer);
        }
    }
}