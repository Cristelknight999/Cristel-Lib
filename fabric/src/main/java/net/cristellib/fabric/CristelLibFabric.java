package net.cristellib.fabric;

import net.cristellib.CristelLib;
import net.fabricmc.api.ModInitializer;

public class CristelLibFabric implements ModInitializer {
    @Override
    public void onInitialize() {
        CristelLib.init();
    }
}
