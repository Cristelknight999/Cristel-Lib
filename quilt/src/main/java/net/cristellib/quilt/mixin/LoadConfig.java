package net.cristellib.quilt.mixin;

import net.cristellib.CristelLib;
import net.minecraft.server.Bootstrap;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Bootstrap.class)
public class LoadConfig {
    @Inject(method = "bootStrap", at = @At("RETURN"))
    private static void loadPacks(CallbackInfo ci) {
        CristelLib.preInit();
    }
}
