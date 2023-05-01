package net.cristellib.mixin;

import net.cristellib.CristelLib;
import net.minecraft.server.Bootstrap;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Bootstrap.class)
public  class BootstrapMixin {



    @Inject(method = "bootStrap", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/item/alchemy/PotionBrewing;bootStrap()V"))
    private static void replaceTerraformPlanksDropItem(CallbackInfo ci) {
        CristelLib.preInit();
    }
}
