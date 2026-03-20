package de.cristelknight.cristellib.fabric.mixin;

import de.cristelknight.cristellib.CristelLib;
import net.minecraft.server.Bootstrap;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Bootstrap.class)
public class BootstrapMixin {
    @Inject(method = "bootStrap", at = @At("RETURN"))
    private static void cristellib$bootStrap(CallbackInfo ci) {
        CristelLib.preInit();
    }
}