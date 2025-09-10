package de.cristelknight.cristellib.mixin;

import de.cristelknight.cristellib.builtinpacks.BuiltInPackConfig;
import de.cristelknight.cristellib.builtinpacks.BuiltinResourcePackSource;
import de.cristelknight.cristellib.config.simple.ConfigRegistry;
import net.minecraft.client.gui.screens.packs.PackSelectionModel;
import net.minecraft.server.packs.repository.Pack;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.List;

@Mixin(PackSelectionModel.class)
public class PackSelectionScreenMixin {

    @Shadow @Final
    List<Pack> selected;

    @Shadow @Final
    List<Pack> unselected;

    @Inject(method = "findNewPacks", at = @At(value = "TAIL"))
    private void cristellib$findNewPacks(CallbackInfo ci) {
        if(!ConfigRegistry.get(BuiltInPackConfig.class).hideAllPacksInScreen()) return;
        selected.removeIf(pack -> pack.location().source() instanceof BuiltinResourcePackSource);
        unselected.removeIf(pack -> pack.location().source() instanceof BuiltinResourcePackSource);
    }
}