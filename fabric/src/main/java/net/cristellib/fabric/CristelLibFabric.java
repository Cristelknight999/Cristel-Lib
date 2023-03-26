package net.cristellib.fabric;

import net.cristellib.fabriclike.CristelLibFabricLike;
import net.fabricmc.api.ModInitializer;

public class CristelLibFabric implements ModInitializer {
    @Override
    public void onInitialize() {
        CristelLibFabricLike.init();
    }
}
