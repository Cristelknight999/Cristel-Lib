package net.cristellib.quilt;

import net.cristellib.CristelLib;
import net.cristellib.fabriclike.CristelLibFabricLike;
import org.quiltmc.loader.api.ModContainer;
import org.quiltmc.qsl.base.api.entrypoint.ModInitializer;

public class CristelLibQuilt implements ModInitializer {
    @Override
    public void onInitialize(ModContainer mod) {
        CristelLibFabricLike.init();
        CristelLib.preInit();
    }
}
