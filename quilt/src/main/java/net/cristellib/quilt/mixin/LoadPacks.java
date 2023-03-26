package net.cristellib.quilt.mixin;

import net.cristellib.CristelLib;
import net.cristellib.api.builtinpacks.BuiltInDataPacks;
import net.minecraft.server.packs.PackType;
import net.minecraft.server.packs.repository.Pack;
import org.quiltmc.qsl.resource.loader.impl.ResourceLoaderImpl;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.function.Consumer;

@Mixin(ResourceLoaderImpl.class)
public class LoadPacks {

    @Inject(method = "registerBuiltinResourcePacks", at = @At("RETURN"))
    private static void loadPacks(PackType type, Consumer<Pack> profileAdder, CallbackInfo ci) {
        if(type.equals(PackType.SERVER_DATA)){
            BuiltInDataPacks.getPacks(profileAdder);
        }
    }

}