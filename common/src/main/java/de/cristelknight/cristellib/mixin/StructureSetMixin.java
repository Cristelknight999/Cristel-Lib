package de.cristelknight.cristellib.mixin;

import net.minecraft.world.level.levelgen.structure.StructureSet;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.List;

@Mixin(StructureSet.class)
public class StructureSetMixin {

    @Shadow
    @Final
    private List<StructureSet.StructureSelectionEntry> structures;

    @Inject(method = "structures", at = @At(value = "RETURN"))
    private void cristellib$findNewPacks(CallbackInfoReturnable<List<StructureSet.StructureSelectionEntry>> cir) {
    }
}