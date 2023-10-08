package net.cristellib.fabric;

import net.cristellib.fabriclike.ModLoadingUtilFabricLike;
import org.quiltmc.loader.api.QuiltLoader;

public class ModLoadingUtilImpl {

    public static boolean isModLoaded(String modId) {
        return QuiltLoader.isModLoaded(modId);
    }

    public static boolean isModLoadedWithVersion(String modid, String minVersion) {
        return ModLoadingUtilFabricLike.isModLoadedWithVersion(modid, minVersion);
    }
}
