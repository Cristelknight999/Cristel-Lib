package de.cristelknight.cristellib.fabric;

import de.cristelknight.cristellib.CristelLib;
import de.cristelknight.cristellib.ModLoadingUtil;
import net.fabricmc.loader.api.FabricLoader;
import net.fabricmc.loader.api.Version;
import net.fabricmc.loader.api.VersionParsingException;

import java.util.Optional;

public class ModLoadingUtilImpl {

    public static boolean isModLoaded(String modID) {
        return FabricLoader.getInstance().isModLoaded(modID);
    }

    public static Optional<Integer> compare(String modID, String version) {
        if (ModLoadingUtil.isModLoaded(modID)) {
            Version modVersion = FabricLoader.getInstance().getModContainer(modID).get().getMetadata().getVersion();
            Version min;
            try {
                min = Version.parse(version);
            } catch (VersionParsingException e) {
                CristelLib.LOGGER.error("Couldn't parse version: {}", version);
                return Optional.empty();
            }
            return Optional.of(modVersion.compareTo(min));
        }
        return Optional.empty();
    }
}
