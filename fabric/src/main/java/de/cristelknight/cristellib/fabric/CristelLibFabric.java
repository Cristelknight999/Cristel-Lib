package de.cristelknight.cristellib.fabric;

import de.cristelknight.cristellib.CristelLib;
import net.fabricmc.api.ModInitializer;

public class CristelLibFabric implements ModInitializer {
    @Override
    public void onInitialize() {
        CristelLib.init();
    }
}
