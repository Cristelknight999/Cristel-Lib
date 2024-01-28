package net.cristellib.mixin;

import net.cristellib.util.TerrablenderUtil;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;
import terrablender.config.TerraBlenderConfig;

@Mixin(value = TerraBlenderConfig.class, remap = false)
public class MixinTerrablenderConfig {
    @Redirect(method = "load()V", at = @At(value = "INVOKE", target = "terrablender/config/TerraBlenderConfig.addNumber(Ljava/lang/String;Ljava/lang/Number;Ljava/lang/Number;Ljava/lang/Number;Ljava/lang/String;)Ljava/lang/Number;"))
    private <T extends Number & Comparable<T>> Number create(TerraBlenderConfig instance, String key, T defaultValue, T min, T max, String comment) {
        if (TerrablenderUtil.mixinEnabled() && key.equals("vanilla_overworld_region_weight")) {
            if (defaultValue instanceof Integer) {
                return 0;
            }
        }
        return instance.addNumber(key, defaultValue, min, max, comment);
    }
}
