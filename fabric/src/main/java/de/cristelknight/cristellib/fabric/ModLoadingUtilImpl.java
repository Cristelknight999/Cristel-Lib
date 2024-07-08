package de.cristelknight.cristellib.fabric;

import de.cristelknight.cristellib.CristelLib;
import de.cristelknight.cristellib.ModLoadingUtil;
import net.fabricmc.loader.api.FabricLoader;
import net.fabricmc.loader.api.Version;
import net.fabricmc.loader.api.VersionParsingException;

public class ModLoadingUtilImpl {

    public static boolean isModLoaded(String modId) {
        return FabricLoader.getInstance().isModLoaded(modId);
    }

    public static boolean isModLoadedWithVersion(String modid, String minVersion) {
        if (ModLoadingUtil.isModLoaded(modid)) {
            Version version = FabricLoader.getInstance().getModContainer(modid).get().getMetadata().getVersion();
            Version min;
            try {
                min = Version.parse(minVersion);
            } catch (VersionParsingException e) {
                CristelLib.LOGGER.error("Couldn't parse version: " + minVersion);
                return false;
            }
            return version.compareTo(min) >= 0;
        }
        return false;
    }
}
