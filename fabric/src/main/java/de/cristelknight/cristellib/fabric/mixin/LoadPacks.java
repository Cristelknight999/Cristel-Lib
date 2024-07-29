package de.cristelknight.cristellib.fabric.mixin;

import de.cristelknight.cristellib.builtinpacks.BuiltInDataPacks;
import net.fabricmc.fabric.impl.resource.loader.ModResourcePackCreator;
import net.minecraft.server.packs.PackType;
import net.minecraft.server.packs.repository.Pack;
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

    @Inject(method = "loadPacks", at = @At("TAIL"))
    private void loadPacks(Consumer<Pack> consumer, CallbackInfo ci) {
        if(type.equals(PackType.SERVER_DATA)){
            BuiltInDataPacks.getPacks(consumer);
        }
    }

}