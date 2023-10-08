package net.cristellib.fabric;

import net.cristellib.fabriclike.ModLoadingUtilFabricLike;
import net.fabricmc.loader.api.FabricLoader;

public class ModLoadingUtilImpl {

    public static boolean isModLoaded(String modId) {
        return FabricLoader.getInstance().isModLoaded(modId);
    }

    public static boolean isModLoadedWithVersion(String modid, String minVersion) {
        return ModLoadingUtilFabricLike.isModLoadedWithVersion(modid, minVersion);
    }
}
